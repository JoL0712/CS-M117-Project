package csm117.remotecommand.network;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import csm117.remotecommand.R;

/**
 * Created by John Lee on 2015-06-18.
 */
public class DiscoveryListViewAdapter extends ArrayAdapter<DiscoveryItem> {
    Context context;

    public DiscoveryListViewAdapter(Context context, int resourceId, List<DiscoveryItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView hostName, ipAddr;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DiscoveryItem discoveryItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.discovery_list_item, null);
			holder = new ViewHolder();
            holder.hostName = (TextView) convertView.findViewById(R.id.host_name);
            holder.ipAddr = (TextView) convertView.findViewById(R.id.ip_addr);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

        holder.hostName.setText(discoveryItem.getHostName());
        holder.ipAddr.setText(discoveryItem.getIpAddress());

		return convertView;
	}
}
