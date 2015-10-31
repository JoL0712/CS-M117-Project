package csm117.remotecommand.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by John Lee on 10/26/2015.
 */
public class Discovery {
    private Receiver mRcvr;
    private List<InetAddress> mAddresses;
    public Discovery() {
        mAddresses = new ArrayList<>();
    }

    interface SearchCallback {
        void callback(List<InetAddress> addresses);
    }

    public void search(int timeout, Activity activity, SearchCallback callback){
        mAddresses.clear();
        ProgressDialog pd = ProgressDialog.show(activity,
                "Searching...", "Attempting to Find Connectable Device", true, false);
        mRcvr = new Receiver(timeout, callback, pd);
        mRcvr.start();
    }

    public class Receiver extends Thread {
        private DatagramSocket mSocket = null;
        private static final String CONFIRM_DATA = "RemoteCommand";
        private SearchCallback mCallback;
        private ProgressDialog mProgressDialog;
        private int mTimeout = 5000, mMaxLoop = 5;

        public Receiver(int timeout, SearchCallback callback, ProgressDialog pd) {
            mTimeout = timeout;
            mCallback = callback;
            mProgressDialog = pd;
        }

        @Override
        public void run() {
            try {
                mSocket = new DatagramSocket(Connection.PORT);
                mSocket.setBroadcast(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("Discovery", "Listening...");
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] recvBuf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    Log.d("Discovery", "Receiving...");
                    mSocket.setSoTimeout(mTimeout);
                    mSocket.receive(packet);
                    Log.d("Discovery", "Success!");

                    String message = new String(packet.getData()).trim();
                    Log.d("Discovery", "Packet message: " + message);
                    if (message.equals(CONFIRM_DATA) && !mAddresses.contains(packet.getAddress())) {
                        Log.d("Discovery", "Adding address...");
                        mAddresses.add(packet.getAddress());
                    }
                    if (mMaxLoop-- < 0)
                        break;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            if (mSocket != null)
                mSocket.close();
            mCallback.callback(mAddresses);
            mProgressDialog.dismiss();
        }
    }

    public void onDestroy() {
        mRcvr.interrupt();
    }
}
