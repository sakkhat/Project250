package sakkhat.com.p250.fragments;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import sakkhat.com.p250.R;

public class FragmentAccessories extends Fragment {

    private View root;
    private Context context;

    private Switch nightLightSwitch;
    private SeekBar nightLighBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_accessories,null,false);
        init();
        return root;
    }

    private void init(){

        // set the context from base context
        context = getContext();

        // access widgets from XML
        nightLightSwitch = root.findViewById(R.id.frag_access_night_mood_switch);
        nightLighBar = root.findViewById(R.id.frag_access_night_mood_bar);

        // set listener for night mood switch
        nightLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    nightLighBar.setVisibility(View.VISIBLE);
                    Toast.makeText(context, "Night Mood On", Toast.LENGTH_SHORT).show();
                }
                else{
                    nightLighBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Night Mood Off", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
