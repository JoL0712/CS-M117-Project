package csm117.remotecommand.network;

import csm117.remotecommand.command.CommandItem;

/**
 * Created by John Lee on 10/26/2015.
 */
public class Connection {
    private static Connection mInstance = new Connection();
    private Connection() {}
    public static Connection getInstance() { return mInstance; }

    public void sendCommand(CommandItem commandItem) {
        //TODO
    }

    public void connect(DiscoveryItem discoveryItem) {
        //TODO
    }
}
