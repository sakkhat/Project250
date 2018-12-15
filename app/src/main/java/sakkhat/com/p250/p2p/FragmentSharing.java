package sakkhat.com.p250.p2p;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import sakkhat.com.p250.R;
import sakkhat.com.p250.adapter.DeviceItemAdapter;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;

public class FragmentSharing extends Fragment implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener,
        WifiP2pManager.ActionListener{

    public static final String TAG = "fragment_sharing";// class tag

    private View root; // fragment root view
    private Context context; // base context
    private WifiManager wifiManager; // wifi functionality access
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;

    private IntentFilter intentFilter;
    private WiFiStateReceiver wiFiStateReceiver;

    /*
    *  XML View stuffs
    * */
    private Switch wifiSwitch;
    private CardView search;
    private GridView gridView;
    private ProgressBar progressBar;

    private ArrayList<String> deviceList;
    private List<WifiP2pDevice> p2pDevices;

    private DeviceItemAdapter adapter;
    private String selectedDeviceName;

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
        search = root.findViewById(R.id.frag_share_search);
        gridView = root.findViewById(R.id.frag_share_grid);
        progressBar = root.findViewById(R.id.frag_share_search_progress);

        p2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = p2pManager.initialize(context, context.getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wiFiStateReceiver = new WiFiStateReceiver(p2pManager, p2pChannel, TAG);


        deviceList = new ArrayList<>();
        p2pDevices = new ArrayList<>();

        adapter = new DeviceItemAdapter(context, deviceList);

        // check all permissions

        setters();
    }

    private void setters(){
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = p2pDevices.get(position).deviceAddress;
                p2pManager.connect(p2pChannel, config, FragmentSharing.this);
                selectedDeviceName = deviceList.get(position);
                Log.d(TAG, "clicked: "+position);
            }
        });

        wifiSwitch.setChecked(wifiManager.isWifiEnabled());
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wifiManager.setWifiEnabled(isChecked);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p2pManager.discoverPeers(p2pChannel,FragmentSharing.this);
            }
        });

        wiFiStateReceiver.setListeners(this, this);
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "success");
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "reasong : "+reason);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, info.groupOwnerAddress.getHostAddress());
        if(info.isGroupOwner && info.groupFormed){
            Intent i = new Intent(context, FileSharing.class);
            i.putExtra(FileSharing.TAG, "N/A");
            context.startActivity(i);
        }
        else if(info.groupFormed){
            Intent i = new Intent(context, FileSharing.class);
            i.putExtra(FileSharing.TAG, info.groupOwnerAddress.getHostAddress());
            context.startActivity(i);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if(peers.getDeviceList().equals(p2pDevices)){
            return;
        }
        p2pDevices.clear();
        deviceList.clear();
        p2pDevices.addAll(peers.getDeviceList());
        for(WifiP2pDevice device : p2pDevices){
            deviceList.add(device.deviceName);
            Log.w(TAG, device.deviceName);
        }
        adapter.notifyDataSetChanged();
        Log.d(TAG, "peer list available: "+deviceList.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(wiFiStateReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(wiFiStateReceiver);
        deviceList.clear();
        p2pDevices.clear();
        adapter.notifyDataSetChanged();
    }
}
