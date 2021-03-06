package csm117.remotecommand.network;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import csm117.remotecommand.R;
import csm117.remotecommand.command.CommandItem;
import csm117.remotecommand.command.CommandListViewAdapter;
import csm117.remotecommand.db.RealmDB;

/**
 * Created by John Lee on 10/26/2015.
 */
public class DiscoveryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Discovery mDiscovery = null;
    private ListView mListView = null;
    private List<DiscoveryItem> mDevices;
    private Set<String> mIPAddresses;
    private DiscoveryListViewAdapter mAdapter = null;
    private static final int DISCOVER_TIMEOUT = 5000;
    private final static int CONTEXT_MENU_DELETE_ITEM = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.discovery_list, container, false);
        mListView = (ListView) view.findViewById(R.id.discoveries_list);

        mDiscovery = new Discovery();

        mDevices = new ArrayList<>();
        mIPAddresses = new HashSet<>();
        loadDevices();

        mAdapter = new DiscoveryListViewAdapter(getActivity(), R.layout.discovery_list_item, mDevices);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        registerForContextMenu(mListView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_find_devices);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover();
            }
        });

        DiscoveryItem di = RealmDB.getInstance().selectLastDevice();
        if (di != null)
            Connection.getInstance().connect(new DiscoveryItem(di));
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(1, CONTEXT_MENU_DELETE_ITEM, Menu.NONE, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() != 1)
            return false;
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE_ITEM:
                RealmDB.getInstance().delete(mDevices.get(info.position).getIpAddress());
                RealmDB.getInstance().close();
                mIPAddresses.remove(mDevices.get(info.position).getIpAddress());
                mDevices.remove(info.position);
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

    private void loadDevices() {
        mDevices = RealmDB.getInstance().selectDevices();
        for (DiscoveryItem di : mDevices) {
            mIPAddresses.add(di.getIpAddress());
        }
        RealmDB.getInstance().close();
    }

    public void discover() {
        if (mDiscovery == null)
            mDiscovery = new Discovery();
        mDiscovery.search(DISCOVER_TIMEOUT, getActivity(), new Discovery.SearchCallback() {
            @Override
            public void callback(final List<InetAddress> addresses) {
                for (InetAddress a : addresses) {
                    DiscoveryItem di = new DiscoveryItem(a.getHostName(), a.getHostAddress());
                    if (!mIPAddresses.contains(di.getIpAddress())) {
                        mIPAddresses.add(di.getIpAddress());
                        mDevices.add(di);
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Connection.getInstance().connect(mDevices.get(position));
    }

    @Override
    public void onDestroyView() {
        mDiscovery.onDestroy();
        super.onDestroyView();
    }
}
