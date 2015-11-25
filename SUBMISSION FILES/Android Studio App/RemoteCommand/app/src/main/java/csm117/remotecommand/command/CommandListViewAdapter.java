package csm117.remotecommand.command;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import csm117.remotecommand.R;

public class CommandListViewAdapter extends ArrayAdapter<CommandItem> {
    Context context;

    public CommandListViewAdapter(Context context, int resourceId, List<CommandItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView name;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        CommandItem commandItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.command_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.command_name);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

        holder.name.setText(commandItem.getCommandName());

		return convertView;
	}
}
