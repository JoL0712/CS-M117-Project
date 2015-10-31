package csm117.remotecommand.command;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import csm117.remotecommand.R;
import csm117.remotecommand.network.Connection;

public class CommandsFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView mListView = null;
    private List<CommandItem> mCommands;
    private CommandListViewAdapter mAdapter = null;

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
}
