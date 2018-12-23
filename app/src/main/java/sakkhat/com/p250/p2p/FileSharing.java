package sakkhat.com.p250.p2p;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;
import sakkhat.com.p250.R;
import sakkhat.com.p250.adapter.DeviceListAdapter;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;
import sakkhat.com.p250.helper.FileUtil;
import sakkhat.com.p250.structure.IOItem;

public class FileSharing extends AppCompatActivity
        implements Handler.Callback,
        WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener,
        WifiP2pManager.ActionListener{

    public static final String TAG = "p2p_file_sharing";
    private static final int FILE_CHOOSE_REQUEST = 5;
    private static final int FOLDER_SELECTOR = 6;
    private static final int PERMISSION_WIFI = 1001;
    private static final int PERMISSION_FILE = 1002;

    private Socket socket;
    private Handler handler;

    private WifiManager wifiManager; // wifi functionality access
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;

    private IntentFilter intentFilter;
    private WiFiStateReceiver wiFiStateReceiver;

    private RelativeLayout queryLayout, p2pLayout;
    private FloatingActionButton btWifiState, btDeviceSearch, btPathLoc; // query components
    private FloatingActionButton btPickFile, btDisconnect, btSent; // p2p components
    private ListView deviceListView;
    private List<WifiP2pDevice> discoverPeers;
    private ArrayList<String> deviceNameList;
    private DeviceListAdapter deviceListAdapter;

    private String path = Environment.getExternalStorageDirectory()+"/p2p";
    private File selectedFile;

    private TextView sendingName, receivingName;
    private ProgressBar sendingProgress, receivingProgress;
    private boolean isSending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        initConnectionInterface();
        initQueryLayout();
        initP2pLayout();
        permissionRequest(PERMISSION_WIFI);
        permissionRequest(PERMISSION_FILE);
    }

    /**
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     * ::::::::::::::::::::::::::: Defined methods  ::::::::::::::::::::::::::::::
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     * */
    private void initQueryLayout(){
        queryLayout = (RelativeLayout) findViewById(R.id.file_share_query_layout);

        deviceListView = (ListView) findViewById(R.id.file_share_device_list);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = discoverPeers.get(position).deviceAddress;
                p2pManager.connect(p2pChannel,config,FileSharing.this);
                Toast.makeText(FileSharing.this, "connecting", Toast.LENGTH_SHORT).show();
            }
        });

        btWifiState = (FloatingActionButton) findViewById(R.id.file_share_query_wifi_state);
        if(wifiManager.isWifiEnabled()){
            btWifiState.setImageResource(R.drawable.ic_wifi_on);
        } else{
            btWifiState.setImageResource(R.drawable.ic_wifi_off);
        }
        btWifiState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    permissionRequest(PERMISSION_WIFI);
                    return;
                }
                else{
                    changeWifiState();
                }

            }
        });

        btDeviceSearch = (FloatingActionButton) findViewById(R.id.file_share_query_search_device);
        btDeviceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(p2pManager == null) return;
                p2pManager.discoverPeers(p2pChannel, FileSharing.this);
                Toast.makeText(FileSharing.this, "searching", Toast.LENGTH_LONG).show();
            }
        });

        btPathLoc = (FloatingActionButton) findViewById(R.id.file_share_query_default_loc);
        btPathLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    permissionRequest(PERMISSION_FILE);
                    return;
                }
                else{
                    pathChooser();
                }
            }
        });


    }

    private void initP2pLayout(){
        p2pLayout = (RelativeLayout) findViewById(R.id.file_share_p2p_layout);


        btPickFile = (FloatingActionButton) findViewById(R.id.file_share_p2p_file_pick);
        btPickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, FILE_CHOOSE_REQUEST);
            }
        });

        btDisconnect = (FloatingActionButton) findViewById(R.id.file_share_p2p_disconnect);
        btDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p2pManager.cancelConnect(p2pChannel, FileSharing.this);
                p2pManager.removeGroup(p2pChannel, FileSharing.this);
                Toast.makeText(FileSharing.this, "disconnecting", Toast.LENGTH_SHORT).show();
                switchToQuery();
            }
        });

        btSent = (FloatingActionButton) findViewById(R.id.file_share_p2p_sent);
        btSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFile == null){
                    Toast.makeText(FileSharing.this, "No file found", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isSending){
                    Toast.makeText(FileSharing.this, "Sending in progress", Toast.LENGTH_LONG).show();
                    return;
                }
                IOItem ii = new IOItem(selectedFile.getName(), selectedFile.length(), false);
                sendingName.setText(ii.getName());
                sendingProgress.setMax(ii.getMax());
                new O2O.Sender(handler, socket, selectedFile);
                isSending = true;
                Toast.makeText(FileSharing.this, "sending", Toast.LENGTH_SHORT).show();
            }
        });

        sendingName = (TextView) findViewById(R.id.file_share_p2p_sending_fname);
        sendingName.setSelected(true);
        receivingName = (TextView) findViewById(R.id.file_share_p2p_receiving_fname);
        receivingName.setSelected(true);

        sendingProgress = (ProgressBar) findViewById(R.id.file_share_p2p_sending_progress);
        receivingProgress = (ProgressBar) findViewById(R.id.file_share_p2p_receiving_progress);
    }

    private void initConnectionInterface(){
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = p2pManager.initialize(this,getMainLooper(),null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wiFiStateReceiver = new WiFiStateReceiver(p2pManager, p2pChannel, TAG);
        wiFiStateReceiver.setListeners(this, this);

        discoverPeers = new ArrayList<>();
        deviceNameList = new ArrayList<>();

        deviceListAdapter = new DeviceListAdapter(this, R.layout.row_device_list, deviceNameList);
    }

    private void initSocketInterface(InetAddress address, boolean isOwner){

        handler = new Handler(this);
        if(isOwner){
            new O2O.Server(handler);
        }
        else{
            new O2O.Client(handler, address);
        }
    }

    private void permissionRequest(int request){
        switch (request){
            case PERMISSION_WIFI:{
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                        != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.CHANGE_WIFI_STATE},PERMISSION_WIFI);
                }
            } break;


            case PERMISSION_FILE:{
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_FILE);
                }
            } break;
        }

    }

    private void changeWifiState(){
        if (wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            btWifiState.setImageResource(R.drawable.ic_wifi_off);
        }
        else{
            wifiManager.setWifiEnabled(true);
            btWifiState.setImageResource(R.drawable.ic_wifi_on);
        }
    }

    private void pathChooser(){
        Intent i = new Intent(this, FolderPicker.class);
        startActivityForResult(i, FOLDER_SELECTOR);
    }

    private void switchToQuery(){

        socket = null;
        p2pLayout.setVisibility(View.GONE);
        sendingName.setText("None");
        receivingName.setText("None");
        sendingProgress.setProgress(0);
        receivingProgress.setProgress(0);

        queryLayout.setVisibility(View.VISIBLE);
        deviceNameList.clear();
        discoverPeers.clear();
        deviceListAdapter.notifyDataSetChanged();
    }


    /**
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     * :::::::::::::::::::::::::::::Override methods :::::::::::::::::::::::::::::
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     * */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wiFiStateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wiFiStateReceiver, intentFilter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_WIFI){
            if(grantResults.length==0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                changeWifiState();
            }
            else{

            }
        }
        if(requestCode == PERMISSION_FILE){
            if(grantResults.length==0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                pathChooser();
            }
            else{

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        if(requestCode == FILE_CHOOSE_REQUEST && data.getData()!= null){
            String path = FileUtil.getPath(FileSharing.this, data.getData());
            if(path != null){
                selectedFile = new File(path);
                Toast.makeText(this, "selected: "+selectedFile.getName(), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == FOLDER_SELECTOR){
            path= data.getExtras().getString("data");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case O2O.SOCKET_ESTABLISHED:{
                socket = (Socket) msg.obj;
                queryLayout.setVisibility(View.GONE);
                p2pLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "socket received by handler");
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                new O2O.Receiver(handler, socket, path);
                return true;
            }
            case O2O.FILE_SENT_CONFIRM:{
                sendingName.append(": completed");
                sendingProgress.setProgress(sendingProgress.getMax());
                isSending = false;
                return true;
            }
            case O2O.FILE_RECEIVED_CONFIRM:{
                receivingName.append(": completed");
                receivingProgress.setProgress(receivingProgress.getMax());
                Toast.makeText(this, "File Received", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "file received confirmed");
                return true;
            }
            case O2O.SOCKET_CLOSED:{
                switchToQuery();
                return true;
            }
            case O2O.FILE_RECEIVE_REQUEST:{
                IOItem di = (IOItem) msg.obj;
                receivingName.setText(di.getName());
                receivingProgress.setMax(di.getMax());
            }
            case O2O.FILE_RECEIVE_PROGRESS:{

            }
            case O2O.FILE_SENT_PROGRESS:{

            }
        }
        return false;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(info.isGroupOwner && info.groupFormed){
            initSocketInterface(info.groupOwnerAddress, true);
        }
        else {
            initSocketInterface(info.groupOwnerAddress, false);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if(discoverPeers.isEmpty() || !discoverPeers.equals(peers)){
            discoverPeers.clear();
            deviceNameList.clear();
            deviceListAdapter.notifyDataSetChanged();

            discoverPeers.addAll(peers.getDeviceList());
            for(WifiP2pDevice device : discoverPeers){
                deviceNameList.add(device.deviceName);
            }
            deviceListAdapter.notifyDataSetChanged();

            Log.d(TAG, "peer list collected");
        }
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "request successfully posted");
    }

    @Override
    public void onFailure(int reason) {
        switch (reason){
            case WifiP2pManager.BUSY:{

            } break;

            case WifiP2pManager.ERROR:{

            } break;

            case WifiP2pManager.P2P_UNSUPPORTED: {

            } break;

            case WifiP2pManager.NO_SERVICE_REQUESTS: {

            } break;
        }
    }
}
