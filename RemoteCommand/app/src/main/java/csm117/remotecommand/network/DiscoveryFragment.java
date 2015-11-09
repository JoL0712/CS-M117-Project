package csm117.remotecommand.network;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.discovery_list, container, false);
        mListView = (ListView) view.findViewById(R.id.discoveries_list);

        mDiscovery = new Discovery();

        mDevices = new ArrayList<>();
        mIPAddresses = new HashSet<>();

        mAdapter = new DiscoveryListViewAdapter(getActivity(), R.layout.discovery_list_item, mDevices);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_find_devices);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover();
            }
        });
        return view;
    }

    public void discover() {
        if (mDiscovery == null)
            mDiscovery = new Discovery();
        mDiscovery.search(DISCOVER_TIMEOUT, getActivity(), new Discovery.SearchCallback() {
            @Override
            public void callback(final List<InetAddress> addresses) {
                for (InetAddress a : addresses) {
                    DiscoveryItem di = new DiscoveryItem(a.getHostName(), a.getHostAddress());
                    if (!mIPAddresses.contains(di.getIPAddress())) {
                        mIPAddresses.add(di.getIPAddress());
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

    public void clear() {
        mDevices.clear();
        mIPAddresses.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //if pc has command cached then send just the command name
        //check command version (version changes if it is updated)
        //else send the actual command
        Connection.getInstance().connect(mDevices.get(position));
    }

    @Override
    public void onDestroyView() {
        mDiscovery.onDestroy();
        super.onDestroyView();
    }
}
