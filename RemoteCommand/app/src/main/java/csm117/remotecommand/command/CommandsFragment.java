package csm117.remotecommand.command;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import csm117.remotecommand.R;
import csm117.remotecommand.network.Connection;

public class CommandsFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView mListView = null;
    private static List<CommandItem> mCommands;
    private static CommandListViewAdapter mAdapter = null;

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
        mListView.setOnItemLongClickListener(this);

        return view;
    }

    public void loadCommands() {
        //Preset Commands
        mCommands.add(new CommandItem("Logout Windows", "shutdown -l"));
        mCommands.add(new CommandItem("Restart Windows", "shutdown -r"));
        mCommands.add(new CommandItem("Shutdown Windows", "shutdown -s"));
        //TODO: loading commands
    }

    public void saveCommands() {
        //TODO: saving commands
    }

    //TODO: adding commands

    //TODO: editing commands

    //TODO: removing commands

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //if pc has command cached then send just the command name
            //check command version (version changes if it is updated)
        //else send the actual command
        Connection.getInstance().sendCommand((CommandItem) mListView.getItemAtPosition(position));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        CommandItem ci = (CommandItem) mListView.getItemAtPosition(position);

        Intent intent = new Intent();
        intent.putExtra("CommandIndex", position);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        EditorFragment cb = new EditorFragment();
        cb.setArguments(intent.getExtras());
        ft.add(R.id.main_layout, cb);
        ft.addToBackStack("CommandEditorLayout");
        ft.commit();
        return true;
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
                    mAdapter.notifyDataSetChanged();
                }
            });
            view.findViewById(R.id.command_editor_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
            return view;
        }
    }
}
