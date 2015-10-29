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

/**
 * Created by John Lee on 10/26/2015.
 */
public class Discovery {
    private Receiver mRcvr;
    private List<InetAddress> mAddresses;
    public Discovery() {
        mRcvr = new Receiver();
        mAddresses = new ArrayList<>();
    }

    interface SearchCallback {
        void callback(List<InetAddress> addresses);
    }

    public void search(int timeout, Activity activity, final SearchCallback callback){
        mAddresses.clear();
        final ProgressDialog pd = ProgressDialog.show(activity,
                "Searching...", "Attempting to Find Connectable Device", true, false);
        final Timer listenTimer = new Timer();
        mRcvr.start();
        listenTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mRcvr.interrupt();
                try {
                    mRcvr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRcvr = new Receiver();
                callback.callback(mAddresses);
                pd.dismiss();
                listenTimer.cancel();
            }
        }, timeout, 1);
    }

    public class Receiver extends Thread {
        private DatagramSocket mSocket = null;
        private static final String CONFIRM_DATA = "RemoteCommand";

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
                    mSocket.receive(packet);
                    Log.d("Discovery", "Success!");

                    String message = new String(packet.getData()).trim();
                    Log.d("Discovery", "Packet message: " + message);
                    if (message.equals(CONFIRM_DATA)) {
                        Log.d("Discovery", "Adding address...");
                        mAddresses.add(packet.getAddress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mSocket != null)
                mSocket.close();
        }
    }

    public void onDestroy() {
        mRcvr.interrupt();
    }
}
