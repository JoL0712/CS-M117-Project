package csm117.remotecommand.command;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import csm117.remotecommand.R;
import csm117.remotecommand.db.RealmDB;
import csm117.remotecommand.network.Connection;
import io.realm.Realm;

public class CommandsFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView mListView = null;
    private static List<CommandItem> mCommands;
    private static CommandListViewAdapter mAdapter = null;
    private final static int CONTEXT_MENU_EDIT_ITEM = 0, CONTEXT_MENU_DELETE_ITEM = 1, CONTEXT_MENU_SWITCH_POSITION = 2;
    private int mSwitchCommand = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_list, container, false);
        mListView = (ListView) view.findViewById(R.id.commands_list);

        mCommands = new ArrayList<>();
        loadCommands();

        mAdapter = new CommandListViewAdapter(getActivity(), R.layout.command_list_item, mCommands);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        registerForContextMenu(mListView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add_command);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RealmDB.getInstance().create("Command Name", "", mCommands);
                RealmDB.getInstance().close();
                editCommand(mCommands.size() - 1);
            }
        });
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_EDIT_ITEM, Menu.NONE, "Edit");
        menu.add(0, CONTEXT_MENU_DELETE_ITEM, Menu.NONE, "Delete");
        menu.add(0, CONTEXT_MENU_SWITCH_POSITION, Menu.NONE, "Switch");
    }

    private void editCommand(int position) {
        CommandItem ci = (CommandItem) mListView.getItemAtPosition(position);

        Intent intent = new Intent();
        intent.putExtra("CommandIndex", position);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        EditorFragment cb = new EditorFragment();
        cb.setArguments(intent.getExtras());
        ft.add(R.id.main_layout, cb);
        ft.addToBackStack("CommandEditorLayout");
        ft.commit();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() != 0)
            return false;
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case CONTEXT_MENU_EDIT_ITEM:
                editCommand(info.position);
                return true;
            case CONTEXT_MENU_DELETE_ITEM:
                mSwitchCommand = -1;
                RealmDB.getInstance().delete(mCommands, info.position);
                RealmDB.getInstance().close();
                mAdapter.notifyDataSetChanged();
                return true;
            case CONTEXT_MENU_SWITCH_POSITION:
                mSwitchCommand = info.position;
                return true;
        }
        return false;
    }

    public void loadCommands() {
        //load commands from database
        mCommands = RealmDB.getInstance().selectCommands();
        if (RealmDB.getInstance().isFirstTime() && mCommands.isEmpty()) { //when user installs app and database does not exist yet then load the preset commands
            //Preset Commands
            RealmDB.getInstance().create("Left", "\"./presetCmd/key-press.bat\" {LEFT}", mCommands);
            RealmDB.getInstance().create("Right", "\"./presetCmd/key-press.bat\" {RIGHT}", mCommands);
            RealmDB.getInstance().create("Up", "\"./presetCmd/key-press.bat\" {UP}", mCommands);
            RealmDB.getInstance().create("Down", "\"./presetCmd/key-press.bat\" {DOWN}", mCommands);
            RealmDB.getInstance().create("Esc", "\"./presetCmd/key-press.bat\" {ESC}", mCommands);
            RealmDB.getInstance().create("Space", "\"./presetCmd/key-press.bat\" \" \"", mCommands);
            RealmDB.getInstance().create("Enter", "\"./presetCmd/key-press.bat\" {ENTER}", mCommands);
            RealmDB.getInstance().create("Alt+Tab", "\"./presetCmd/key-press.bat\" %{TAB}", mCommands);
            RealmDB.getInstance().create("Close Program", "\"./presetCmd/key-press.bat\" %{F4}", mCommands);
            RealmDB.getInstance().create("Volume Up", "\"./presetCmd/incVolume.bat\"", mCommands);
            RealmDB.getInstance().create("Volume Down", "\"./presetCmd/decVolume.bat\"", mCommands);
            RealmDB.getInstance().create("Mute", "\"./presetCmd/mute.bat\"", mCommands);
            RealmDB.getInstance().create("Open Google", "start \"\" \"www.google.com\"", mCommands);
            RealmDB.getInstance().create("New Tab", "\"./presetCmd/key-press.bat\" \"^t\"", mCommands);
            RealmDB.getInstance().create("Close Tab", "\"./presetCmd/key-press.bat\" \"^w\"", mCommands);
            RealmDB.getInstance().create("Open Notepad", "start notepad", mCommands);
            RealmDB.getInstance().create("Open Excel", "start excel", mCommands);
            RealmDB.getInstance().create("Logout Windows", "shutdown -l", mCommands);
            RealmDB.getInstance().create("Restart Windows", "shutdown -r", mCommands);
            RealmDB.getInstance().create("Shutdown Windows", "shutdown -s", mCommands);
        }
        RealmDB.getInstance().close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSwitchCommand >= 0) {
            CommandItem temp = mCommands.get(mSwitchCommand);
            mCommands.set(mSwitchCommand, mCommands.get(position));
            mCommands.set(position, temp);
            RealmDB.getInstance().updatePositions(mCommands);
            RealmDB.getInstance().close();
            mAdapter.notifyDataSetChanged();
            mSwitchCommand = -1;
            return;
        }
        //send the command to device
        Connection.getInstance().sendCommand((CommandItem) mListView.getItemAtPosition(position));
    }

    public static CommandItem getCommandItem(int option) {
        //find command item based on option
        for (CommandItem ci : mCommands) {
            if (ci.getOption() == option)
                return ci;
        }
        return null;
    }

    public static class EditorFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.command_editor, container, false);
            Bundle args = getArguments();
            final CommandItem ci = mCommands.get(args.getInt("CommandIndex"));
            final EditText name = ((EditText) view.findViewById(R.id.command_editor_name)), script = ((EditText) view.findViewById(R.id.command_editor_script));
            name.setText(ci.getCommandName());
            script.setText(ci.getCommand());
            view.findViewById(R.id.command_editor_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ci.setCommandName(name.getText().toString());
                    ci.setCommand(script.getText().toString());
                    RealmDB.getInstance().update(ci);
                    RealmDB.getInstance().close();
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Command Saved", Toast.LENGTH_SHORT).show();
                }
            });
            view.findViewById(R.id.command_editor_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    mAdapter.notifyDataSetChanged();
                }
            });
            return view;
        }
    }
}
