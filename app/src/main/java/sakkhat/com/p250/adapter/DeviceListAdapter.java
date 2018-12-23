package sakkhat.com.p250.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sakkhat.com.p250.R;

/**
 * Created by Rafiul Islam on 21-Dec-18.
 */

public class DeviceListAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private ArrayList<String> list;

    public DeviceListAdapter(Context context, int resource, ArrayList<String> list) {
        super(context, resource, list);
        this.context = context;
        this.resource = resource;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(resource, null, false);

            holder = new Holder(row);
            row.setTag(holder);
        }
        else{
            holder = (Holder) row.getTag();
            holder.textView.setText(list.get(position));
        }

        return row;
    }

    private static class Holder {
        TextView textView;

        public Holder(View view){
            textView = view.findViewById(R.id.row_device_name);
        }
    }
}
