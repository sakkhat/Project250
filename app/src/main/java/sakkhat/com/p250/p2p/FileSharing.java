package sakkhat.com.p250.p2p;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import lib.folderpicker.FolderPicker;
import sakkhat.com.p250.R;
import sakkhat.com.p250.adapter.FileIOListAdapter;
import sakkhat.com.p250.helper.FileUtil;
import sakkhat.com.p250.structure.DataItem;

public class FileSharing extends AppCompatActivity implements Handler.Callback{

    public static final String TAG = "p2p_file_sharing";
    private static final int FILE_CHOOSE_REQUEST = 5;
    private static final int FOLDER_SELECTOR = 6;
    private static final int EXTERNAL_STORAGE_READ_PERMISSION = 1001;

    private Socket socket;
    private Handler handler;

    private Queue<File> fileQueue;
    private Queue<Integer> indexQueue;
    private ArrayList<DataItem> itemList;
    private int currentReceiveIndex, currentSentIndex;
    private FileIOListAdapter adapter;

    private TextView remoteDName,selectFile;
    private ListView listView;
    private CardView sentFileCard;

    private View socketDialogView;
    private AlertDialog dialog;

    private String path = Environment.getExternalStorageDirectory()+"/p2p";
    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        host = getIntent().getExtras().getString(TAG, "N/A");
        init();
        socketConnectionDialog();
    }

    private void init(){
        remoteDName = (TextView) findViewById(R.id.file_share_remote_dname);
        listView = (ListView) findViewById(R.id.file_share_list);
        selectFile = (TextView) findViewById(R.id.file_share_select_file);
        sentFileCard = (CardView) findViewById(R.id.file_share_sent_file);

        remoteDName.setSelected(true);

        selectFile.setSelected(true);

        handler = new Handler(this);

        // init events
        remoteDName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    socket.close();

                } catch (IOException e) {

                } catch (NullPointerException e){

                }
                finally {
                    close();
                }

            }
        });

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(
                            FileSharing.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(
                                FileSharing.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                EXTERNAL_STORAGE_READ_PERMISSION);
                    }
                }
                else{
                    openContentManager();
                }
            }
        });

        sentFileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileQueue.isEmpty()){
                    Toast.makeText(FileSharing.this, "No file in fileQueue", Toast.LENGTH_SHORT).show();
                    return;
                }
                new O2O.Sender(handler, socket, fileQueue.peek());
                fileQueue.remove();
                currentSentIndex = indexQueue.peek();
                indexQueue.remove();
            }
        });

        socketDialogView = getLayoutInflater().inflate(R.layout.dialog_socket_establish,null,false);

        fileQueue = new LinkedList<>();
        indexQueue = new LinkedList<>();
        itemList = new ArrayList<>();

        adapter = new FileIOListAdapter(this, R.layout.row_file_io_o2o, itemList);
        listView.setAdapter(adapter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == EXTERNAL_STORAGE_READ_PERMISSION){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // permission granted
                openContentManager();
            }
            else{
                Toast.makeText(FileSharing.this, "Permission denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openContentManager(){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, FILE_CHOOSE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        if(requestCode == FILE_CHOOSE_REQUEST && data.getData()!= null){
            String path = FileUtil.getPath(FileSharing.this, data.getData());
            if(path != null){
                File f = new File(path);
                itemList.add(new DataItem(f.getName(), f.length(), false));
                fileQueue.add(f);
                indexQueue.add(itemList.size()-1);

                adapter.notifyDataSetChanged();
            }
        }
        else if(requestCode == FOLDER_SELECTOR){
            path= data.getExtras().getString("data");
            makeConnection();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case O2O.SOCKET_ESTABLISHED:{
                socket = (Socket) msg.obj;

                if(dialog!= null && dialog.isShowing()){
                    socketDialogView.findViewById(R.id.dialog_p2p_progress).setVisibility(View.GONE);
                    socketDialogView.findViewById(R.id.dialog_p2p_done).setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }

                Log.d(TAG, "socket received by handler");
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                new O2O.Receiver(handler, socket, path);
                return true;
            }
            case O2O.FILE_SENT_CONFIRM:{
                itemList.get(currentSentIndex).setProgress(itemList.get(currentSentIndex).getSize());
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "File Sent", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "file sent confirmed");
                if(!fileQueue.isEmpty()){
                    new O2O.Sender(handler, socket, fileQueue.peek());
                    fileQueue.remove();
                    currentSentIndex = indexQueue.peek();
                    indexQueue.remove();
                }
                return true;
            }
            case O2O.FILE_RECEIVED_CONFIRM:{
                itemList.get(currentReceiveIndex).setProgress(itemList.get(currentReceiveIndex).getSize());
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "File Received", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "file received confirmed");
                return true;
            }
            case O2O.SOCKET_CLOSED:{
                close();
                return true;
            }
            case O2O.FILE_RECEIVE_REQUEST:{
                DataItem item = (DataItem) msg.obj;
                itemList.add(item);
                currentReceiveIndex = itemList.size()-1;
                adapter.notifyDataSetChanged();
            }
            case O2O.FILE_RECEIVE_PROGRESS:{
                long loaded = (long) msg.obj;
                itemList.get(currentReceiveIndex).setProgress(loaded);
                adapter.notifyDataSetChanged();
            }
            case O2O.FILE_SENT_PROGRESS:{
                long available = (long) msg.obj;
                itemList.get(currentSentIndex).setProgress(available);
                adapter.notifyDataSetChanged();
            }
        }
        return false;
    }

    private void close(){
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        this.finish();
    }

    private void socketConnectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(socketDialogView);
        builder.setPositiveButton("Store Path", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(FileSharing.this, FolderPicker.class);
                startActivityForResult(i, FOLDER_SELECTOR);
            }
        });
        builder.setNegativeButton("Default", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeConnection();
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


    }

    private void makeConnection(){
        if(host.equalsIgnoreCase("N/A")){
            // start Server
            new O2O.Server(handler);
            Log.d(TAG, "request for server connection");
        }
        else{
            Log.d(TAG, host);
            // start Socket
            new O2O.Client(handler, host);
            Log.d(TAG, "request for client connection with: "+host);
        }
    }

}
