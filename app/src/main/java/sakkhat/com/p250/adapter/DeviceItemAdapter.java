package sakkhat.com.p250.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sakkhat.com.p250.R;


/**
 * Created by Rafiul Islam on 14-Dec-18.
 */

public class DeviceItemAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> deviceList;

    public DeviceItemAdapter(Context context, ArrayList<String> deviceList){
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root = convertView;
        Holder holder = null;
        if(root == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            root = inflater.inflate(R.layout.item_p2p_device, null, false);
            holder = new Holder(root);
            root.setTag(holder);
        }
        else{
            holder = (Holder) root.getTag();
        }

        holder.text.setText(deviceList.get(position));

        return root;
    }

    private static class Holder{
        TextView text;

        public Holder(View v){
            text = v.findViewById(R.id.p2p_device_name);
            text.setSelected(true);
        }
    }
}
