package csm117.remotecommand.command;

public class CommandItem {
    private String mCommandName, mCommand;

    public CommandItem(String name, String command) {
        mCommandName = name;
        mCommand = command;
    }
    public String getCommandName() {return mCommandName;}
    public void setCommandName(String val) {mCommandName = val;}
    public String getCommand() {return mCommand;}
    public void setCommand(String val) {mCommand = val;}
}
