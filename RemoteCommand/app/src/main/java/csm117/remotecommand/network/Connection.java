package csm117.remotecommand.network;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
        mClient.send(commandItem.getCommand());
    }

    public void connect(DiscoveryItem discoveryItem) {
        //TODO
        if (mClient != null) {
            mClient.interrupt();
            try {
                mClient.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mClient = new Client(discoveryItem.getIPAddress());
        mClient.start();
        mMainActivity.setTitle("Connected to " + discoveryItem.getHostName());
    }

    private static class Client extends Thread {
        private Socket mSocket = null;
        private ConcurrentLinkedQueue<String> mDataToSend;
        private String mIP;

        public Client(String ip) {
            mDataToSend = new ConcurrentLinkedQueue<>();
            mIP = ip;
        }

        public synchronized void send(String data) {
            mDataToSend.add(data);
        }

        @Override
        public void run() {
            try {
                mSocket = new Socket(mIP, PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
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

                /*InputStream reader = mSocket.getInputStream();
                byte array[] = new byte[1024];
                int i = reader.read(array);*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mDataToSend.clear();
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
