package csm117.remotecommand.network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import csm117.remotecommand.MainActivity;
import csm117.remotecommand.command.CommandItem;

/**
 * Created by John Lee on 10/26/2015.
 */
public class Connection {
    public static final int PORT = 2000;
    private static Connection mInstance = new Connection();
    private Connection() {}
    public static Connection getInstance() { return mInstance; }
    private static volatile MainActivity mMainActivity = null;
    private Client mClient = null;
    private static final String REQ_PASS = "PASS", REQ_LOGOUT = "LOGOUT", REQ_OPTION = "OPTION", REQ_UPDATE = "UPDATE";

    public static void setMainActivity(MainActivity mainActivity) { mMainActivity = mainActivity; }

    public void sendCommand(CommandItem commandItem) {
        //TODO: send name, option, version
        if (mClient == null || !mClient.send(REQ_OPTION, commandItem.getCommand())) {
            Toast.makeText(mMainActivity, "Please connect to a device first", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String pass) {
        if (mClient == null || !mClient.send(REQ_PASS, pass)) {
            disconnect();
            Toast.makeText(mMainActivity, "Not connected to device", Toast.LENGTH_SHORT).show();
        }
    }

    public void logout() {
        if (mClient == null || !mClient.send(REQ_LOGOUT, "")) {
            disconnect();
            Toast.makeText(mMainActivity, "Not connected to device", Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnect() {
        if (mClient != null) {
            mClient.close();
            try {
                mClient.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mClient = null;
        }
    }

    public void connect(final DiscoveryItem discoveryItem) {
        if (mClient != null && mClient.connected() && mClient.getDiscoveryItem().getIPAddress().equals(discoveryItem.getIPAddress()))
            return;
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = ProgressDialog.show(mMainActivity, "Connecting...", "Attempting to connect to " + discoveryItem.getHostName(), true, false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                disconnect();
                mClient = new Client(discoveryItem);
                mClient.start();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pd.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
                builder.setTitle("Password");
                final EditText input = new EditText(mMainActivity);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        login(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disconnect();
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        }.execute();
    }

    private static class Client extends Thread {
        private Socket mSocket = null;
        private ConcurrentLinkedQueue<String> mDataToSend;
        private DiscoveryItem mDiscoveryItem;
        private volatile boolean mConnectionLost;
        private int mSavedSerial;

        enum Action { NONE, LOGOUT, OPTION }
        private Action mAction;

        public Client(DiscoveryItem discoveryItem) {
            mDiscoveryItem = discoveryItem;
            mDataToSend = new ConcurrentLinkedQueue<>();
            mConnectionLost = false;
            mSavedSerial = new Random().nextInt(13469);
            mAction = Action.NONE;
        }

        public DiscoveryItem getDiscoveryItem() {
            return mDiscoveryItem;
        }

        public synchronized boolean send(String request, String data) {
            if (mConnectionLost)
                return false;

            mSavedSerial = (mSavedSerial * 13469 + 2671) % 65535;
            int opt = 0, ver = 0;
            switch (request)
            {
                case REQ_LOGOUT:
                    mAction = Action.LOGOUT;
                    break;
                case REQ_OPTION:
                    mAction = Action.OPTION;
                case REQ_UPDATE:
                    opt = 0;
                    ver = 0;
                    break;
            }
            mDataToSend.add(String.format("%s %d %d %d %s", request, opt, ver, mSavedSerial, data));
            return true;
        }

        public void recv(final String data) {
            if (mAction == Action.OPTION) {
                //TODO: send update
            }
            else {
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(mMainActivity)
                                .setTitle("Output")
                                .setMessage(data)
                                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
                if (mAction == Action.LOGOUT && data.contains("Logged out"))
                    close();
            }
        }

        public void close() {
            mConnectionLost = true;
            mAction = Action.NONE;
        }

        public boolean connected() {
            return !mConnectionLost;
        }

        @Override
        public void run() {
            try {
                mSocket = new Socket(mDiscoveryItem.getIPAddress(), PORT);
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMainActivity.setTitle("Connected to " + mDiscoveryItem.getHostName());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread rcvr = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            InputStream reader = mSocket.getInputStream();
                            final byte array[] = new byte[4096];
                            int i = reader.read(array);
                            if (i > 0) {
                                recv(new String(array, "UTF-8"));
                            }
                            else if (i == -1) {
                                mConnectionLost = true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            rcvr.start();
            while (!Thread.currentThread().isInterrupted() && !mConnectionLost) {
                try {
                    String toSend = mDataToSend.poll();
                    if (toSend != null) {
                        OutputStream writer = mSocket.getOutputStream();
                        writer.write(toSend.getBytes());
                        writer.flush();
                        mMainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mMainActivity, "Command Sent!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mConnectionLost = true;
            rcvr.interrupt();
            mDataToSend.clear();
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainActivity.setTitle(MainActivity.NO_CONNECTION);
                }
            });
        }
    }
}
