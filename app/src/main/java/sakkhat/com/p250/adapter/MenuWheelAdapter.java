package sakkhat.com.p250.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import github.hellocsl.cursorwheel.CursorWheelLayout;
import sakkhat.com.p250.R;
import sakkhat.com.p250.structure.MenuItem;

/**
 * Created by hp on 29-Sep-18.
 */

public class MenuWheelAdapter extends CursorWheelLayout.CycleWheelAdapter {

    /*
    * Menu adapter extends Wheel Layout Cycle Wheel Adapter
    * */

    private Context context;
    private List<MenuItem> images;
    private LayoutInflater inflater;

    public MenuWheelAdapter(Context context, List<MenuItem> images){
        this.context = context;
        this.images = images;

        // set the layout inflater using the context arg
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public View getView(View parent, int position) {
        // crete an item view with layout inflater
        View root = inflater.inflate(R.layout.item_menu_wheel,null,false);
        // access the components
        ImageView icon = root.findViewById(R.id.menu_item_icon);
        TextView title = root.findViewById(R.id.menu_item_title);
        // set icon and title
        icon.setImageResource(getItem(position).getId());
        title.setText(getItem(position).getName());

        return root;
    }

    @Override
    public MenuItem getItem(int position) {
        return images.get(position);
    }
}
