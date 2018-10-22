package sakkhat.com.p250.broadcaster;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import sakkhat.com.p250.p2p.FragmentSharing;

/**
 * Created by Rafiul Islam on 29-Sep-18.
 */

public class WiFiStateReceiver extends BroadcastReceiver {

    /*
    *  This broadcast receiver will fired up when sharing fragment draw over home screen
    *  and onResume method called from this fragment.
    *
    * */

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private String TAG;

    private WifiP2pManager.PeerListListener pll;
    private WifiP2pManager.ConnectionInfoListener cil;

    public WiFiStateReceiver(WifiP2pManager p2pManager, WifiP2pManager.Channel p2pChannel, String TAG){
        this.p2pManager = p2pManager;
        this.p2pChannel = p2pChannel;
        this.TAG = TAG;
    }

    public void setListeners(WifiP2pManager.PeerListListener pll, WifiP2pManager.ConnectionInfoListener cil){
        this.pll = pll;
        this.cil = cil;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if(action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)){
            // access the WiFi state whether on or off by passing a default value
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                Toast.makeText(context, "Turn On WiFi", Toast.LENGTH_LONG).show();
            }
        }
        else if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
            Log.d(TAG,"peer list available");
            p2pManager.requestPeers(p2pChannel,pll);
        }
        else if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            if(p2pManager == null){
                Log.w(TAG, "null channel");
                return;
            }
            // network established
            NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(netInfo.isConnected()){
                Log.d(TAG,"device connected");
                p2pManager.requestConnectionInfo(p2pChannel,cil);
            }
        }
        else if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){

        }
    }
}
