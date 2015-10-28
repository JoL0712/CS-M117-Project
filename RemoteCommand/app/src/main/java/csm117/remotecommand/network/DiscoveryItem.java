package csm117.remotecommand.network;

public class DiscoveryItem implements Comparable<DiscoveryItem>{
    private String mHostName, mIPAddress;

    public DiscoveryItem(String hostName, String ipAddr) {
        mHostName = hostName;
        mIPAddress = ipAddr;
    }
    public String getHostName() {return mHostName;}
    public void setHostName(String val) {mHostName = val;}
    public String getIPAddress() {return mIPAddress;}
    public void setIPAddress(String val) {mIPAddress = val;}

    @Override
    public int compareTo(DiscoveryItem another) {
        int val = mHostName.compareTo(another.getHostName());
        if (val == 0)
            val = mIPAddress.compareTo(another.getIPAddress());
        return val;
    }
}
