package csm117.remotecommand.network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
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

    public static void setMainActivity(MainActivity mainActivity) { mMainActivity = mainActivity; }

    public void sendCommand(CommandItem commandItem) {
        //TODO
        if (mClient == null || !mClient.send(commandItem.getCommand())) {
            Toast.makeText(mMainActivity, "Please connect to a device first", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(final DiscoveryItem discoveryItem) {
        //TODO
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
                if (mClient != null) {
                    mClient.close();
                    try {
                        mClient.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mClient = new Client(discoveryItem);
                mClient.start();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pd.dismiss();
            }
        }.execute();
    }

    private static class Client extends Thread {
        private Socket mSocket = null;
        private ConcurrentLinkedQueue<String> mDataToSend;
        private DiscoveryItem mDiscoveryItem;
        private volatile boolean mConnectionLost;

        public Client(DiscoveryItem discoveryItem) {
            mDiscoveryItem = discoveryItem;
            mDataToSend = new ConcurrentLinkedQueue<>();
            mConnectionLost = false;
        }

        public DiscoveryItem getDiscoveryItem() {
            return mDiscoveryItem;
        }

        public synchronized boolean send(String data) {
            if (mConnectionLost)
                return false;
            mDataToSend.add(data);
            return true;
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
                                mMainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            new AlertDialog.Builder(mMainActivity)
                                                    .setTitle("Output")
                                                    .setMessage(new String(array, "UTF-8"))
                                                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    })
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .show();
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
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
