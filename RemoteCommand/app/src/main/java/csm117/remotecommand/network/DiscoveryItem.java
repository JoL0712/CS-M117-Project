package csm117.remotecommand.network;

public class DiscoveryItem {
    private String mHostName, mIPAddress;

    public DiscoveryItem(String hostName, String ipAddr) {
        mHostName = hostName;
        mIPAddress = ipAddr;
    }
    public String getHostName() {return mHostName;}
    public void setHostName(String val) {mHostName = val;}
    public String getIPAddress() {return mIPAddress;}
    public void setIPAddress(String val) {mIPAddress = val;}
}
