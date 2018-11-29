package sakkhat.com.p250.p2p;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import sakkhat.com.p250.R;
import sakkhat.com.p250.adapter.FileIOListAdapter;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;
import sakkhat.com.p250.helper.FileUtil;

public class FileSharing extends AppCompatActivity
        implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener,
        WifiP2pManager.ActionListener, Handler.Callback{

    private static final String TAG = "p2p_file_sharing";
    private static final int FILE_CHOOSE_REQUEST = 5;

    private ListView deviceNameView, fileListView;
    private TextView connectedDeviceView,selectFileView;
    private ImageButton fileSentView;

    private ArrayList<String> deviceName;
    private List<WifiP2pDevice> p2pDevices;

    private ArrayAdapter<String> deviceListAdapter;
    private FileIOListAdapter fileIOListAdapter;

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;

    private IntentFilter intentFilter;
    private WiFiStateReceiver wiFiStateReceiver;

    private Socket socket;
    private Handler handler;

    private Queue<File> queue;
    private boolean isConnected;
    private String connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        initObjects();
        initEvents();
    }

    private void initObjects(){
        deviceNameView = (ListView) findViewById(R.id.fshare_device_list_view);
        fileListView = (ListView) findViewById(R.id.fshare_file_queue_list_view);
        connectedDeviceView = (TextView) findViewById(R.id.fshare_connect_device);
        selectFileView = (TextView) findViewById(R.id.fshare_select_file);
        fileSentView = (ImageButton) findViewById(R.id.fshare_file_sent);

        deviceName = new ArrayList<>();
        p2pDevices = new ArrayList<>();
        queue = new LinkedList<>();

        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceName);

        p2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        p2pChannel = p2pManager.initialize(this, getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wiFiStateReceiver = new WiFiStateReceiver(p2pManager, p2pChannel, TAG);
    }

    private void initEvents(){
        deviceNameView.setAdapter(deviceListAdapter);
        wiFiStateReceiver.setListeners(this, this);

        deviceNameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = p2pDevices.get(position).deviceAddress;
                p2pManager.connect(p2pChannel, config, FileSharing.this);
                connected = p2pDevices.get(position).deviceName;
            }
        });

        connectedDeviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        selectFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, FILE_CHOOSE_REQUEST);
            }
        });

        fileSentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected||queue.isEmpty()) return;
                new O2O.Sender(handler, socket, queue.peek());
                queue.remove();
            }
        });

        handler = new Handler(this);
        p2pManager.requestPeers(p2pChannel, this);
        p2pManager.discoverPeers(p2pChannel, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wiFiStateReceiver);
        deviceName.clear();
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wiFiStateReceiver, intentFilter);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(info.groupFormed && info.isGroupOwner){
            new O2O.Server(handler);
            isConnected = true;
            connectedDeviceView.setText(connected);
        }
        else if(info.groupFormed){
            new O2O.Client(handler, info.groupOwnerAddress);
            isConnected = true;
            connectedDeviceView.setText(connected);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        if(requestCode == FILE_CHOOSE_REQUEST){
            String path = FileUtil.getPath(this, data.getData());
            File file = new File(path);
            selectFileView.setText(file.getName());
            queue.add(file);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if(peers.getDeviceList().equals(p2pDevices)){
            return;
        }
        p2pDevices.clear();
        deviceName.clear();
        p2pDevices.addAll(peers.getDeviceList());
        for(WifiP2pDevice device : p2pDevices){
            deviceName.add(device.deviceName);
        }
        deviceListAdapter.notifyDataSetChanged();
        Log.d(TAG, "peer list available: "+deviceName.size());
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "success");
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "reasong : "+reason);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case O2O.SOCKET_ESTABLISHED:{
                socket = (Socket) msg.obj;
                Log.d(TAG, "socket received by handler");
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                new O2O.Receiver(handler, socket);
                return true;
            }
            case O2O.FILE_NAME:{
                Toast.makeText(this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                return true;
            }
            case O2O.FILE_SENT_CONFIRM:{
                Toast.makeText(this, "File Sent", Toast.LENGTH_SHORT).show();
                return true;
            }
            case O2O.FILE_RECEIVED_CONFIRM:{
                Toast.makeText(this, "File Received", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
}
