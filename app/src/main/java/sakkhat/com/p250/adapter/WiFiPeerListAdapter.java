package sakkhat.com.p250.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by hp on 29-Sep-18.
 */

public class WiFiPeerListAdapter extends ArrayAdapter{
    private Context context;
    private int resource;
    public WiFiPeerListAdapter(Context context, int resource){
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if(row == null){

        }

        return row;
    }

    private static class Items{

    }
}
