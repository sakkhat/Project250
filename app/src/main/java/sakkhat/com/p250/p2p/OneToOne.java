package sakkhat.com.p250.p2p;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;

public class OneToOne extends AppCompatActivity
        implements View.OnClickListener,WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener{

    public static final String TAG = "one_to_one_sharing";
    private static final int FILE_TAKE_REQUEST = 2;
    /*
    * Activity XML stuffs
    * */
    private CircleImageView btFileSend;// send file to connected device
    private Button btConStatus;// connection status : refresh peer list and disconnect
    private boolean connected;// flag for check device is connected or not

    /*
    * Alert Dialog stuffs
    * */
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private ListView peerList;
    private ProgressBar peerListProgress;
    private View dialogView;

    /*
    * WiFi P2P stuffs
    * */
    private WifiManager wifiManager;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private IntentFilter intentFilter;
    private WiFiStateReceiver stateReceiver;

    private List<String> deviceNameList;
    private List<WifiP2pDevice> deviceList;
    private ArrayAdapter<String> arrayAdapter;

    private O2O.DataIO dataIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one);

        connected = false;// initially device is not connected

        init();
    }

    /*
    * instance of all stuffs
    * */
    private void init(){
        //------------------------ File Send Button ----------------------------------------
        btFileSend = (CircleImageView)findViewById(R.id.o2o_bt_send);
        btFileSend.setOnClickListener(this);
        // ---------------------------------------------------------------------------------

        // --------------------- Button Connection Status -----------------------------------
        btConStatus = (Button) findViewById(R.id.o2o_connection_status);
        btConStatus.setOnClickListener(this);
        //------------------------------------------------------------------------------------

        // --------------------------- Alert Dialog and its View -----------------------------

        dialogView = getLayoutInflater().inflate(R.layout.list_wifi_peer,null,false);
        peerList = dialogView.findViewById(R.id.list_wifi_peer_list_view);
        peerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /*
            * After clicking one peer item from peer list device address will be accessed
            * and p2pManager will try to connect with this device by using it's address.
            * ActionListener will confirm whether this request is success or not. If success
            * actionListener will fired up it's onSuccess method; that means request is successfully
            * submitted.
            *
            * Then broadcaster receiver will fired up onConnectionInfoAvailable method after
            * getting WIFI_P2P_CONNECTION_CHANGED_ACTION result and send an information about
            * the connected device
            * */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = deviceList.get(position).deviceAddress;
                p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // device is connected
                        connected = true;
                        btConStatus.setText("Disconnect");
                        dialog.dismiss();
                        Log.d(TAG, "device is connected");
                    }

                    @Override
                    public void onFailure(int reason) {
                        // connection request failed
                        Log.e(TAG, "device couldn't connected");
                        Toast.makeText(OneToOne.this, "Something goes error",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        peerListProgress = dialogView.findViewById(R.id.list_wifi_peer_progress_bar);

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialog.dismiss();
                p2pManager.stopPeerDiscovery(p2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });
            }
        });
        dialogBuilder.setCancelable(false);
        dialog = dialogBuilder.create();
        dialog.show();
        //------------------------------------------------------------------------------------

        //----------------------------------- WiFi P2P functionality ----------------------------
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        p2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        p2pChannel = p2pManager.initialize(this, getMainLooper(), null);

        /**
         * WIFI_P2P_STATE_CHANGED_ACTION : network status changed
         * WIFI_P2P_PEERS_CHANGED_ACTION : wifi available device list changed
         * WIFI_P2P_CONNECTION_CHANGED_ACTION : device is recently connected or disconnect
         * WIFI_P2P_THIS_DEVICE_CHANGED_ACTION : running device force another action
         * @see WiFiStateReceiver
        * */
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        stateReceiver = new WiFiStateReceiver(p2pManager,p2pChannel,TAG);
        stateReceiver.setListeners(this, this);// set listeners

        deviceList = new ArrayList<>();
        deviceNameList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,deviceNameList);
        peerList.setAdapter(arrayAdapter);
        //---------------------------------------------------------------------------------------------------
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.o2o_connection_status : {
                if(!connected){
                    // search peer list
                    peerListProgress.setVisibility(View.VISIBLE);
                    dialog.show();
                    p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "peer list request successfully submitted");
                        }

                        @Override
                        public void onFailure(int reason) {
                            if(reason == WifiP2pManager.ERROR){
                                Toast.makeText(OneToOne.this, "error", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,"peer list discover error");
                            }
                            else if(reason == WifiP2pManager.BUSY){
                                Toast.makeText(OneToOne.this, "service is busy", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "service is busy to discover peer list");
                            }
                        }
                    });
                }
                else{
                    // disconnect the connected device
                    wifiManager.setWifiEnabled(false);
                    connected = false;
                    wifiManager.setWifiEnabled(true);
                    btConStatus.setText("Refresh List");
                    deviceList.clear();
                    deviceNameList.clear();
                    Log.d(TAG,"device disconnected");
                }
            } break;

            case R.id.o2o_bt_send:{
                if(!connected){
                    Toast.makeText(this,"Device is not connected", Toast.LENGTH_SHORT);
                }
                else{
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent,FILE_TAKE_REQUEST);
                }
            } break;
        }
    }

    /**
     * Read the file that take from the storage and request to sent on socket stream
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // obtain the preferred conditions
        if(resultCode != RESULT_OK) return;
        if(requestCode != FILE_TAKE_REQUEST) return;
        Log.d(TAG, "selected file: "+data.getData().getPath());
        // get the data and request to sent
        //dataIO.send(data.getData());

        Log.w(TAG, data.getData().toString());

        File f = new File(data.getData().getPath());

        Log.w(TAG, f.getName());
        Log.w(TAG, f.getAbsolutePath());

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(stateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(stateReceiver);
    }

    /*
    * This method will fired up when broadcaster will get an action of WIFI_P2P_PEERS_CHANGED_ACTION.
    * Then this method will be called with a list available peers.
    * */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        peerListProgress.setVisibility(View.GONE);
        if(peers.getDeviceList().equals(deviceList)){
            return;
        }
        deviceList.clear();
        deviceNameList.clear();
        arrayAdapter.notifyDataSetChanged();

        deviceList.addAll(peers.getDeviceList());
        for( WifiP2pDevice d : deviceList){
            deviceNameList.add(d.deviceName);
        }
        arrayAdapter.notifyDataSetChanged();
    }

    /**
    * onConnectionInfoAvailable will fired up when broadcaster receive an action of
    * WIFI_P2P_CONNECTION_CHANGED_ACTION. Then this method will called with connected
    * device information package.
    * */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        /**
         * Condition for server device.
         * @param dataIO pass the reference of dataIO to server thread. Server thread will set this
         *               dataIO to start the I/O communication between I/O stream and UI using DataIO thread.
         * @param handler pass the handler that instanced in this activity to make possible that
         *                dataIO an external thread can communication with this activity and it's UI.
         * */
        if(info.isGroupOwner && info.groupFormed){
            dataIO = new O2O.DataIO(handler);
            O2O.Server server = new O2O.Server(dataIO);

        }
        /**
         * Condition for client device.
         * @param info.groupOwnerAddress is the device InetAddress that is provided to make a socket
         *                               connection.
         * @param dataIO pass the reference of dataIO to server thread. Client thread will set this
         *               dataIO to start the I/O communication between I/O stream and UI using DataIO thread.
         * @param handler pass the handler that is instanced in this activity to make possible that
         *                dataIO an external thread can communication with this activity and it's UI.
         * */
        else if(info.groupFormed){
            dataIO = new O2O.DataIO(handler);
            O2O.Client client = new O2O.Client(info.groupOwnerAddress,dataIO);
        }
    }
    /**
    * Handler will make a communication line between android system main looper or more precisely
    * UI and external threads. DataIO thread will send message request to handler, and handler will
    * pass it to main lopper message queue for available the message on UI.
     *
    * @param msg the requested message from external thread.
    * */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == O2O.MESSAGE_FILE_RECEIVED){
                File file = (File)msg.obj;
                Log.d(TAG, file.getName());
            }
            return false;
        }
    });
}
