package csm117.remotecommand.command;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class CommandItem extends RealmObject {
    @PrimaryKey
    private int option;
    private int version;
    private int position;
    private String commandName;
    private String command;

    public CommandItem() {
        option = -1;
        version = 0;
        position = 0;
        commandName = "";
        command = "";
    }

    public CommandItem(CommandItem item) {
        option = item.getOption();
        version = item.getVersion();
        position = item.getPosition();
        commandName = item.getCommandName();
        command = item.getCommand();
    }

    public String getCommandName() {return commandName;}
    public void setCommandName(String val) {commandName = val;}
    public String getCommand() {return command;}
    public void setCommand(String val) {command = val;}
    public int getOption() {return option;}
    public void setOption(int val) {option = val;}
    public int getVersion() {return version;}
    public void setVersion(int val) {version = val;}
    public int getPosition() {return position;}
    public void setPosition(int val) {position = val;}
}
