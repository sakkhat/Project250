package sakkhat.com.p250.p2p;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;


import sakkhat.com.p250.R;

public class FragmentSharing extends Fragment {

    public static final String TAG = "fragment_sharing";// class tag

    private View root; // fragment root view
    private Context context; // base context
    private WifiManager wifiManager; // wifi functionality access
    /*
    *  XML View stuffs
    * */
    private Switch wifiSwitch;
    private ImageButton btSingleShare, btGroupShare;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_sharing,null,false);// inflate the xml root view
        context = getContext(); // get the base context

        init();

        return root;
    }
    /*
    * Instance of widget stuffs
    * */
    private void init(){

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        wifiSwitch = root.findViewById(R.id.frag_share_wifi_switch);
        wifiSwitch.setChecked(wifiManager.isWifiEnabled()); // set switch as wifi state

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wifiManager.setWifiEnabled(isChecked); // set wifi state as switch state
            }
        });

        // option buttons
        btSingleShare = root.findViewById(R.id.frag_share_bt_single_share);
        btSingleShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiManager.isWifiEnabled()){
                    // start activity
                    startActivity(new Intent(context,OneToOne.class));
                }
                else{
                    Toast.makeText(context,"Turn On Wifi",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btGroupShare = root.findViewById(R.id.frag_share_bt_group_share);
        btGroupShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiManager.isWifiEnabled()){
                    // start activity
                }
            }
        });

    }
}
