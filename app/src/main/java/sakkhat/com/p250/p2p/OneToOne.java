package sakkhat.com.p250.p2p;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;
import sakkhat.com.p250.helper.FileUtil;

public class OneToOne extends AppCompatActivity
        implements View.OnClickListener,WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener,
                    AdapterView.OnItemClickListener{

    public static final String TAG = "one_to_one_sharing";
    private static final int FILE_TAKE_REQUEST = 2;


    private ListView availableDeviceListView;
    private TextView txConnectionStatus;
    private Button btSwitchConnectionStatus;

    private ListView fileIOHistoryListView;
    private TextView txSelectedFile;
    private CircleImageView btFileBrowse;
    private CircleImageView btFileSend;

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

    /*
    * Socket, Background Threads, Files
    * */
    private boolean connected;

    private ExecutorService execService; // socket reading and writing thread service
    private volatile Socket socket;

    private Queue<File> fileQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one);

        connected = false;// initially device is not connected

        init();
        syncPeerList(); // searching available device all time
    }

    /*
    * instance of all stuffs
    * */
    private void init(){
        // ---------------------- List View ------------------------------------
        availableDeviceListView = (ListView) findViewById(R.id.o2o_device_list);
        availableDeviceListView.setOnItemClickListener(this);

        fileIOHistoryListView = (ListView) findViewById(R.id.o2o_dataIO_history);
        fileIOHistoryListView.setOnItemClickListener(this);
        //  -------------------------------------------------------------------------------------

        //------------------------------ ALL Text View ------------------------------
        txConnectionStatus = (TextView)findViewById(R.id.o2o_connection_status);
        txSelectedFile = (TextView) findViewById(R.id.o2o_selected_file_name);
        //---------------------------------------------------------------------------------------

        // --------------------- All Buttons-----------------------------------------------
        btSwitchConnectionStatus = (Button) findViewById(R.id.o2o_bt_disconnect);
        btSwitchConnectionStatus.setOnClickListener(this);
        btSwitchConnectionStatus.setEnabled(false);

        btFileBrowse = (CircleImageView) findViewById(R.id.o2o_bt_file_browse);
        btFileBrowse.setOnClickListener(this);

        btFileSend = (CircleImageView) findViewById(R.id.o2o_bt_file_send);
        btFileSend.setOnClickListener(this);

        //------------------------------------------------------------------------------------

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
        availableDeviceListView.setAdapter(arrayAdapter);
        //---------------------------------------------------------------------------------------------------

        // file queue
        fileQueue = new LinkedList<>();
    }

    /**
     * Button click functionality
     * */
    @Override
    public void onClick(View view){
        switch (view.getId()){

            // disconnect button
            case R.id.o2o_bt_disconnect:{
                try {
                    socket.close();
                    execService.shutdown();
                    p2pManager.cancelConnect(p2pChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            connected = false;
                            btSwitchConnectionStatus.setEnabled(false);
                            txConnectionStatus.setText("Not Connected");
                        }

                        @Override
                        public void onFailure(int reason) {

                        }
                    });
                } catch (IOException ex){

                }
            } break;

            // file browse button
            case R.id.o2o_bt_file_browse:{
                if(!connected){
                    Toast.makeText(this,"Device is not connected", Toast.LENGTH_SHORT);
                }
                else{
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent,FILE_TAKE_REQUEST);
                }
            } break;

            // file send button
            case R.id.o2o_bt_file_send:{
                sendQueuedFiles();
            } break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if(view.getId() == R.id.o2o_device_list){
            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = deviceList.get(position).deviceAddress;
            p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // device is connected
                    connected = true;
                    txConnectionStatus.setText(deviceList.get(position).deviceName);
                    btSwitchConnectionStatus.setEnabled(true);
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
    }

    /**
     * Read the file that take from the storage and request to sent on socket stream
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // obtain the preferred conditions
        if(resultCode != RESULT_OK) return;
        if(requestCode != FILE_TAKE_REQUEST) return;

        String path = FileUtil.getPath(this, data.getData());
        if(path != null){
            File file = new File(path);
            txSelectedFile.setText(file.getName());
            fileQueue.add(file);
        }
        Log.w(TAG, data.getData().toString());

        
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

    @Override
    protected void onStop() {
        super.onStop();
        if(execService != null){
            execService.shutdown();
        }
    }

    /*
        * This method will fired up when broadcaster will get an action of WIFI_P2P_PEERS_CHANGED_ACTION.
        * Then this method will be called with a list available peers.
        * */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
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
         * 2 fixed thread for socket connection established, socket reading and socket writing.
         * thread-1 : socket established -> socket reading
         * thread-2 : socket writing
         * */
        execService = Executors.newFixedThreadPool(2);

        /**
         * Condition for server device.
         * @param handler pass the handler that instanced in this activity to make possible that
         *                dataIO an external thread can communication with this activity and it's UI.
         * */
        if(info.isGroupOwner && info.groupFormed){
            execService.execute(new O2O.Server(handler));
        }
        /**
         * Condition for client device.
         * @param info.groupOwnerAddress is the device InetAddress that is provided to make a socket
         *                               connection.
         * @param handler pass the handler that is instanced in this activity to make possible that
         *                dataIO an external thread can communication with this activity and it's UI.
         * */
        else if(info.groupFormed){
            execService.execute(new O2O.Client(info.groupOwnerAddress, handler));
        }
    }

    private void syncPeerList(){
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

                Toast.makeText(OneToOne.this, file.getName()+" received", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == O2O.MESSAGE_FILE_SENT){
                Toast.makeText(OneToOne.this, "File Sent", Toast.LENGTH_LONG).show();
                if(!fileQueue.isEmpty()){
                    sendQueuedFiles();
                }
            }
            else if(msg.what == O2O.MESSAGE_SOCKET_CONNECTED){
                socket = (Socket)msg.obj;
                execService.execute(new O2O.Receiver(socket,handler));
            }
            else if(msg.what == O2O.MESSAGE_IO_ERROR){
                // handle the error
            }
            return false;
        }
    });

    private void sendQueuedFiles(){
        if(!connected) return;
        execService.execute(new O2O.Sender(socket, handler, fileQueue.peek()));
        fileQueue.remove();
    }
}
