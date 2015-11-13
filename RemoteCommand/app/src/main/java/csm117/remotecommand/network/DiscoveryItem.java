package csm117.remotecommand.network;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DiscoveryItem extends RealmObject{
    @PrimaryKey
    private String ipAddress;
    private String hostName;
    private String passwordHash;

    public DiscoveryItem() {
        ipAddress = "";
        hostName = "";
        passwordHash = null;
    }
    public DiscoveryItem(String hostName, String ipAddress) {
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        passwordHash = null;
    }
    public DiscoveryItem(DiscoveryItem di) {
        hostName = di.getHostName();
        ipAddress = di.getIpAddress();
        passwordHash = di.getPasswordHash();
    }
    public String getHostName() {return hostName;}
    public void setHostName(String val) {hostName = val;}
    public String getIpAddress() {return ipAddress;}
    public void setIpAddress(String val) {ipAddress = val;}

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
