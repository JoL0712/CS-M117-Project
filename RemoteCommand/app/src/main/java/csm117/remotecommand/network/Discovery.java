package csm117.remotecommand.network;

import android.app.Activity;
import android.app.ProgressDialog;

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

    public List<InetAddress> search(int timeout, Activity activity){
        mAddresses.clear();
        mRcvr.start();

        final ProgressDialog pd = ProgressDialog.show(activity,
                "Listening...", "Attempting to Find Connectable Device");
        final Timer listenTimer = new Timer();
        listenTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                pd.dismiss();
                mRcvr.kill();
                listenTimer.cancel();
            }
        }, timeout, 1);

        return mAddresses;
    }

    public class Receiver extends Thread {
        private DatagramSocket mSocket = null;
        private static final String CONFIRM_DATA = "RemoteCommand";
        private static final int PORT = 2020;
        private boolean mRunning;

        @Override
        public void run() {
            mRunning = true;
            try {
                mSocket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
                mSocket.setBroadcast(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mSocket == null)
                kill();
            while(mRunning) {
                try {
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    mSocket.receive(packet);

                    String message = new String(packet.getData()).trim();
                    if (message.equals(CONFIRM_DATA)) {
                        mAddresses.add(packet.getAddress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            kill();
        }

        public void kill() {
            mRunning = false;
            if (mSocket != null)
                mSocket.close();
        }
    }

    public void onDestroy() {
        mRcvr.kill();
    }
}
