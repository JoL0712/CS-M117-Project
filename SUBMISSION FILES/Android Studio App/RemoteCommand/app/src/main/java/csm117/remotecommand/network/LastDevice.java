package csm117.remotecommand.network;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by John Lee on 11/13/2015.
 */
public class LastDevice extends RealmObject {
    @PrimaryKey
    private int key;
    private String ipAddress;

    public LastDevice() {
        key = 0;
        ipAddress = null;
    }

    public LastDevice(String ip) {
        key = 0;
        ipAddress = ip;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}