package sakkhat.com.p250.p2p;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;

import sakkhat.com.p250.R;
import sakkhat.com.p250.adapter.FileIOListAdapter;
import sakkhat.com.p250.helper.FileUtil;

public class FileSharing extends AppCompatActivity implements Handler.Callback{

    public static final String TAG = "p2p_file_sharing";
    private static final int FILE_CHOOSE_REQUEST = 5;
    private static final int EXTERNAL_STORAGE_READ_PERMISSION = 1001;

    private Socket socket;
    private Handler handler;

    private Queue<File> queue;
    private boolean isConnected;
    private String connected;

    private TextView remoteDName,selectFile;
    private ListView listView;
    private CardView sentFileCard;

    private View socketDialogView;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        init();

        String address = getIntent().getExtras().getString(TAG, "N/A");
        if(address.equalsIgnoreCase("N/A")){
            // start Server
            new O2O.Server(handler);
            Log.d(TAG, "request for server connection");
        }
        else{
            Log.d(TAG, address);
            // start Socket
            new O2O.Client(handler, address);
            Log.d(TAG, "request for client connection with: "+address);
        }

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

            }
        });

        socketDialogView = getLayoutInflater().inflate(R.layout.dialog_socket_establish,null,false);
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
                new O2O.Sender(handler, socket, f);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case O2O.SOCKET_ESTABLISHED:{
                socket = (Socket) msg.obj;
                socketDialogView.findViewById(R.id.dialog_p2p_progress).setVisibility(View.GONE);
                socketDialogView.findViewById(R.id.dialog_p2p_done).setVisibility(View.VISIBLE);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
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
                Log.d(TAG, "file sent confirmed");
                return true;
            }
            case O2O.FILE_RECEIVED_CONFIRM:{
                Toast.makeText(this, "File Received", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "file received confirmed");
                return true;
            }
        }
        return false;
    }

    private void socketConnectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(socketDialogView);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
