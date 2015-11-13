package csm117.remotecommand.network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import csm117.remotecommand.MainActivity;
import csm117.remotecommand.command.CommandItem;
import csm117.remotecommand.command.CommandsFragment;
import csm117.remotecommand.db.RealmDB;

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
        if (mClient == null || !mClient.send(REQ_OPTION, new String[] { String.valueOf(commandItem.getOption()), String.valueOf(commandItem.getVersion()) })) {
            Toast.makeText(mMainActivity, "Please connect to a device first", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String pass) {
        if (mClient == null || !mClient.send(REQ_PASS, new String[] { pass })) {
            disconnect();
            Toast.makeText(mMainActivity, "Not connected to device", Toast.LENGTH_SHORT).show();
        }
    }

    public void logout() {
        if (mClient == null || !mClient.send(REQ_LOGOUT, new String[] { "" })) {
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
        private ConcurrentLinkedQueue<byte[]> mDataToSend;
        private DiscoveryItem mDiscoveryItem;
        private volatile boolean mConnectionLost;
        private int mSavedSerial;

        public Client(DiscoveryItem discoveryItem) {
            mDiscoveryItem = discoveryItem;
            mDataToSend = new ConcurrentLinkedQueue<>();
            mConnectionLost = false;
            mSavedSerial = new Random().nextInt(13469);
        }

        public DiscoveryItem getDiscoveryItem() {
            return mDiscoveryItem;
        }

        public synchronized boolean send(String request, String[] data) {
            if (mConnectionLost)
                return false;

            mSavedSerial = (mSavedSerial * 13469 + 2671) % 65535;
            String opt = "0", ver = "0";
            String additional = data[0];
            switch (request)
            {
                case REQ_PASS:
                    //hash password
                    int total = 133;
                    String tempStr = "";
                    for (char c : additional.toCharArray()) {
                        total *= (int) c;
                        total += (int) c;
                        total %= 128;
                        tempStr += (char) (total);
                    }
                    additional = tempStr;
                    break;
                case REQ_LOGOUT:
                    break;
                case REQ_OPTION:
                    additional = "";
                case REQ_UPDATE:
                    if (data.length < 2) {
                        Log.e("Connection", "Sending error: both option and version are required");
                        return false;
                    }
                    opt = data[0];
                    ver = data[1];
                    if (data.length > 2)
                        additional = data[2];
                    break;
            }
            byte[] one = String.format("%s %s %s %d ", request, opt, ver, mSavedSerial).getBytes();
            byte[] two = additional.getBytes();
            byte[] combined = new byte[one.length + two.length];
            System.arraycopy(one, 0, combined, 0, one.length);
            System.arraycopy(two, 0, combined, one.length, two.length);
            mDataToSend.add(combined);
            return true;
        }

        private void outputMsg(final String msg) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(mMainActivity)
                            .setTitle("Output")
                            .setMessage(msg)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }

        public void recv(String data) {
            String[] parts = data.split(" ", 2);
            if (parts.length < 2)
                return;
            switch (parts[0]) {
                case "UPDATE_OPT": {
                    int opt = Integer.parseInt(parts[1]);
                    CommandItem ci = CommandsFragment.getCommandItem(opt);
                    if (ci != null)
                        send(REQ_UPDATE, new String[] { String.valueOf(ci.getOption()), String.valueOf(ci.getVersion()), ci.getCommand() });
                    break;
                }
                case "RESULT":
                    if (parts[1].equals("logout")) {
                        outputMsg("Logged out");
                        close();
                    }
                    break;
                case "OUTPUT":
                    outputMsg(parts[1]);
                    break;
            }
        }

        public void close() {
            mConnectionLost = true;
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
                                recv(new String(array, "UTF-8").split("\n")[0]);
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
                    byte[] toSend = mDataToSend.poll();
                    if (toSend != null) {
                        OutputStream writer = mSocket.getOutputStream();
                        writer.write(toSend);
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
