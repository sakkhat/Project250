package sakkhat.com.p250.p2p;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sakkhat.com.p250.Home;
import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;
import sakkhat.com.p250.fragments.FragmentListener;

public class FragmentSharing extends Fragment {

    public static final String TAG = "fragment_sharing";
    public static final String CONNECTION_INFO = "$c-$s";

    public static final int ID = 1000;

    private View root;
    private Context context;

    /*
    *  XML View stuffs
    * */
    private ListView peerListView;
    private TextView peerListStatus;
    private ProgressBar peerListLoading;
    private FloatingActionButton peerListRefresh;

    /*
    *  WiFi P2P connectivity stuffs
    * */
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WiFiStateReceiver wifiStateReceiver;
    private IntentFilter p2pIntentFilter;

    private List<WifiP2pDevice> scannedPeers;
    private ArrayList<String> availableDevicesNames;
    private ArrayAdapter<String> peersAdapter;

    private FragmentListener fragmentListener;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_sharing,null,false);

        init();
        initP2P();

        return root;
    }

    public void setFragmentListener(FragmentListener fragmentListener){
        this.fragmentListener = fragmentListener;
    }
    private void init(){
        /*
        *  instance of context and view stuff
        * */
        context = getContext();

        // access the peer list view from xml
        peerListView = root.findViewById(R.id.frag_share_peer_list);
        peerListStatus = root.findViewById(R.id.frag_share_peer_list_status);
        peerListLoading = root.findViewById(R.id.frag_share_list_loading);
        peerListRefresh = root.findViewById(R.id.frag_share_peer_refresh);

        // initialize the device array
        availableDevicesNames = new ArrayList<>();

        // array adapter for device list view
        peersAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,availableDevicesNames);
        // set the adapter in list view
        peerListView.setAdapter(peersAdapter);

        // set click listener on refresh button
        peerListRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                availableDevicesNames.clear();
                peersAdapter.notifyDataSetChanged();
                peerListLoading.setVisibility(View.VISIBLE);
                p2pManager.discoverPeers(p2pChannel, p2pActionListener);
            }
        });

        // set item click listener on device list view
        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = scannedPeers.get(position).deviceAddress;
                p2pManager.connect(p2pChannel,config,p2pActionListener);
            }
        });

    }
    private void initP2P(){
        /*
        *  initialize WiFi P2P manager, channel, receiver, intenet filter for receiver
        *  and other related stuffs
        * */

        // get the WiFi P2P service provider from the system
        p2pManager = (WifiP2pManager)context.getSystemService(Context.WIFI_P2P_SERVICE);
        // get the P2P channel by initialize the P2P manager using main looper
        p2pChannel = p2pManager.initialize(context,context.getMainLooper(),null);

        // intialize intent filter for wifi state broadcast receiver
        p2pIntentFilter = new IntentFilter();

        /*
        *   WIFI_P2P_STATE_CHANGED_ACTION = indicate wifi status on/off
        *   WIFI_P2P_PEER_CHANGED_ACTION = indicate peer list change
        *   WIFI_P2P_CONNECTION_CHANGED_ACTION = indicate whether connected or disconnected
        *   WIFI_P2P_THIS_DEVICE_CHANGED_ACTION =
        * */
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // initialize the wifi state broadcaster receiver
        wifiStateReceiver = new WiFiStateReceiver(p2pManager,p2pChannel,this);
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(wifiStateReceiver, p2pIntentFilter);
        p2pManager.discoverPeers(p2pChannel,p2pActionListener);
        peerListLoading.setVisibility(View.VISIBLE);
        Log.d(TAG,"WiFi state receiver registered");
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(wifiStateReceiver);
        // clear previous peers
        availableDevicesNames.clear();
        Log.d(TAG,"WiFi state receiver unregistered");
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList currentPeers) {

            // check whether scanned first time or not
            if(scannedPeers == null){
                // initialize the scanned device list
                scannedPeers = new ArrayList<>();
                // add all peers into scanned list
                scannedPeers.addAll(currentPeers.getDeviceList());
                // add items from scanned list to available device list for list view
                for(WifiP2pDevice device: scannedPeers){
                    availableDevicesNames.add(device.deviceName);
                }
                // notify the list adapter for update peer list visible
                peersAdapter.notifyDataSetChanged();

                Log.d(TAG,"new peer list created");
            }
            else if(! currentPeers.getDeviceList().equals(scannedPeers)){
                // clear the old scanned list
                scannedPeers.clear();
                // update the scanned list with new peers
                scannedPeers.addAll(currentPeers.getDeviceList());
                // clear the available device list
                availableDevicesNames.clear();
                // notify the adapter
                peersAdapter.notifyDataSetChanged();
                // add new peers for list view items
                for(WifiP2pDevice device: scannedPeers){
                    availableDevicesNames.add(device.deviceName);
                }
                // notify the adapter
                peersAdapter.notifyDataSetChanged();

                Log.d(TAG, "peer list updated");
            }
            // progress bar removed
            peerListLoading.setVisibility(View.GONE);

        }
    };

    private WifiP2pManager.ActionListener p2pActionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG,"p2p listener success");
        }

        @Override
        public void onFailure(int reason) {
            if (reason == WifiP2pManager.BUSY){
                Log.w(TAG,"p2p listener failed: system busy");
            }
            else if(reason == WifiP2pManager.ERROR){
                Log.e(TAG,"p2p listener failed: error");
            }
            else {
                Log.e(TAG,"p2p listener failed");
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            // create a bundle for data package
            Bundle bundle = new Bundle();
            // set bundle tag
            bundle.putString(Home.FRAGMENT_TAG,TAG);
            // set config info for P2P sharing
            bundle.putParcelable(CONNECTION_INFO,info);
            // communicate with activity
            fragmentListener.onResponse(bundle);
        }
    };
}
