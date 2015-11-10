package csm117.remotecommand.db;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import csm117.remotecommand.command.CommandItem;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by John Lee on 11/9/2015.
 */
public class RealmDB {
    private static int mOptionCounter = 0;
    private Context mContext;
    private static boolean mFirstTime = false;

    private static RealmDB mInstance = null;
    private RealmDB(Context context) {
        mContext = context;
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(context)
                .name("remote_command.realm")
                .schemaVersion(42)
                .deleteRealmIfMigrationNeeded()
                .build());
        begin();
        RealmResults<CommandItem> results = mRealm.allObjects(CommandItem.class);
        if (results.size() == 0) {
            mOptionCounter = 0;
        }
        else {
            mOptionCounter = results.max("option").intValue() + 1;
        }
        mFirstTime = mOptionCounter == 0;
        close();
    }
    public static RealmDB getInstance() { return mInstance; }
    public static void initInstance(Context context) { if (mInstance == null) mInstance = new RealmDB(context); }
    public boolean isFirstTime() { return mFirstTime; }

    private Realm mRealm = null;

    private void begin() {
        if (mRealm != null) //cannot begin if last transaction has not been committed and closed
            return;
        mRealm = Realm.getDefaultInstance();
        mRealm.beginTransaction();
    }

    public void close() {
        if (mRealm == null) //cannot close if begin() has not been called
            return;
        mRealm.commitTransaction();
        mRealm.close();
        mRealm = null;
    }

    public void create(String commandName, String command, List<CommandItem> items) {
        begin();
        //create a new command
        CommandItem ci = new CommandItem();
        ci.setCommandName(commandName);
        ci.setCommand(command);
        ci.setOption(mOptionCounter);
        ci.setVersion(0);
        ci.setPosition(items.size());
        mRealm.copyToRealmOrUpdate(ci);
        items.add(ci);
        //increment the option counter
        mOptionCounter++;
    }

    //for script or name changes only
    //not for position changes
    public void update(CommandItem ci) {
        begin();
        ci.setVersion(ci.getVersion() + 1);
        mRealm.copyToRealmOrUpdate(ci);
    }

    public void updatePositions(final List<CommandItem> items) {
        begin();
        for (int i = 0; i < items.size(); i++) {
            CommandItem ci = items.get(i);
            ci.setPosition(i);
            mRealm.copyToRealmOrUpdate(ci);
        }
    }

    public void delete(List<CommandItem> items, int position) {
        begin();
        CommandItem toDelete = items.get(position);
        toDelete = mRealm.where(CommandItem.class).equalTo("option", toDelete.getOption()).findFirst();
        if (toDelete != null) {
            toDelete.removeFromRealm();
            items.remove(position);
            updatePositions(items);
        }
    }

    public List<CommandItem> selectAll() {
        begin();
        RealmResults<CommandItem> results = mRealm.allObjectsSorted(CommandItem.class, "position", true);
        List<CommandItem> list = new ArrayList<>();
        for (CommandItem ci : results) {
            list.add(new CommandItem(ci));
        }
        return list;
    }
}
