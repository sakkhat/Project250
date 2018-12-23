package sakkhat.com.p250.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import sakkhat.com.p250.R;
import sakkhat.com.p250.structure.IOItem;

/**
 * Created by Rafiul Islam on 14-Nov-18.
 */

public class FileIOListAdapter extends ArrayAdapter<IOItem> {

    private Context context;
    private int resource;
    private ArrayList<IOItem> list;

    public FileIOListAdapter(Context context, int resource, ArrayList<IOItem> list) {
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
        }

        holder.name.setText(list.get(position).getName());
        if(list.get(position).isCompleted()){
            holder.status.setProgress(holder.status.getMax());
        }
        else{
            holder.status.setMax(list.get(position).getMax());
            holder.status.setProgress(list.get(position).getProgress());
        }

        if(list.get(position).isRemoteFile()){
            holder.ioType.setImageResource(R.drawable.ic_file_download);
        }

        return row;
    }

    private static class Holder{
        TextView name;
        ProgressBar status;
        ImageView ioType;

        public Holder(View row){
            name = row.findViewById(R.id.row_file_io_fname);
            status = row.findViewById(R.id.row_file_io_progress);
            ioType = row.findViewById(R.id.row_file_io_img);

            name.setSelected(true);
        }
    }

}
