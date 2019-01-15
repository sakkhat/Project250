package sakkhat.com.p250.p2p;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import sakkhat.com.p250.R;
import sakkhat.com.p250.helper.FileUtil;
import sakkhat.com.p250.jarvis.Jarvis;
import sakkhat.com.p250.structure.IOItem;

public class PCSharing extends AppCompatActivity implements Handler.Callback{

    private static final String TAG = "pc_share";
    private static final int FILE_CHOOSE_REQUEST = 5;
    private static final int STORAGE_READ_PERMISSION = 6;

    private String path = Environment.getExternalStorageDirectory()+"/p2p";

    private EditText ipAddress, ipPort;
    private Button ipMessenger, request, pcShutdown;
    private FloatingActionButton btSend, btPick, btDisconnect;
    private TextView sendingFileName, receivingFileName;
    private ProgressBar sendingProgress, receivingProgress;
    private LinearLayout beforeView;
    private RelativeLayout afterView;

    private Handler handler;
    private Socket socket;
    private boolean isSending = false;
    private File selectedFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcsharing);

        handler = new Handler(this);

        initUI();
        setEvents();
        checkPermission();
    }

    private void initUI(){
        ipAddress = (EditText) findViewById(R.id.pc_share_ip_address);
        ipPort = (EditText) findViewById(R.id.pc_share_ip_port);
        ipMessenger = (Button) findViewById(R.id.pc_share_ip_messenger);
        request = (Button) findViewById(R.id.pc_share_request);
        pcShutdown = (Button) findViewById(R.id.pc_share_pc_shutdown);
        btSend = (FloatingActionButton) findViewById(R.id.pc_share_sent);
        btPick = (FloatingActionButton) findViewById(R.id.pc_share_file_pick);
        btDisconnect = (FloatingActionButton) findViewById(R.id.pc_share_disconnect);
        sendingFileName = (TextView) findViewById(R.id.pc_share_sending_fname);
        receivingFileName = (TextView) findViewById(R.id.pc_share_receiving_fname);
        sendingProgress = (ProgressBar) findViewById(R.id.pc_share_sending_progress);
        receivingProgress = (ProgressBar) findViewById(R.id.pc_share_receiving_progress);

        beforeView = (LinearLayout) findViewById(R.id.pc_share_before_view);
        afterView = (RelativeLayout) findViewById(R.id.pc_share_after_view);
    }

    private void setEvents(){
        ipMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/rafiulgits/IP-Messenger"));
                Intent.createChooser(intent, "action chooser");
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ipAddress.getText().toString().isEmpty()){
                    ipAddress.setError("");
                }
                else if(ipPort.getText().toString().isEmpty()){
                    ipPort.setError("");
                }
                else{
                    String host = ipAddress.getText().toString().trim();
                    int port = Integer.parseInt(ipPort.getText().toString().trim());
                    new O2OPC.RequestToPC(host, port, handler);
                }
            }
        });

        pcShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new O2OPC.Sender(socket, handler, O2OPC.PC_SHUTDOWN);
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFile == null){
                    Toast.makeText(PCSharing.this, "No file found", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isSending){
                    Toast.makeText(PCSharing.this, "Sending in progress", Toast.LENGTH_LONG).show();
                    return;
                }
                IOItem ii = new IOItem(selectedFile.getName(), selectedFile.length(), false);
                sendingFileName.setText(ii.getName());
                sendingProgress.setMax(ii.getMax());
                new O2OPC.Sender(socket, handler, selectedFile);
                isSending = true;
                Toast.makeText(PCSharing.this, "sending", Toast.LENGTH_SHORT).show();
            }
        });

        btPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, FILE_CHOOSE_REQUEST);
            }
        });

        btDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    socket.close();
                } catch (IOException ex){
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_READ_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == FILE_CHOOSE_REQUEST){
                String path = FileUtil.getPath(PCSharing.this, data.getData());
                if(path != null){
                    selectedFile = new File(path);
                    Toast.makeText(this, "selected: "+selectedFile.getName(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_READ_PERMISSION){
            if(grantResults.length==0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
            }
            else{

            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case O2OPC.SOCKET_CONNECTED:{
                socket = (Socket) msg.obj;
                beforeView.setVisibility(View.GONE);
                afterView.setVisibility(View.VISIBLE);
                new O2OPC.Receiver(socket, handler);
                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
                return true;
            }

            case O2OPC.SOCKET_ERROR:{
                afterView.setVisibility(View.GONE);
                beforeView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "disconnected", Toast.LENGTH_SHORT).show();
                return true;
            }
            case O2OPC.FILE_RECEIVE_REQUEST:{
                IOItem ioItem = (IOItem) msg.obj;
                receivingFileName.setText(ioItem.getName());
                receivingProgress.setProgress(ioItem.getMax());
                return true;
            }
            case O2OPC.FILE_RECEIVED:{
                receivingProgress.setMax(receivingProgress.getMax());
                Log.d(TAG, "file received");
                Toast.makeText(this, "File received", Toast.LENGTH_SHORT).show();
                return true;
            }
            case O2OPC.FILE_SENT:{
                isSending = false;
                sendingProgress.setProgress(sendingProgress.getMax());
                Log.d(TAG, "file sent");
                Toast.makeText(this, "file sent", Toast.LENGTH_SHORT).show();
            }
            case O2OPC.RING_MODE:{
                Jarvis.ringModeChange(this,Jarvis.Mode.RING_MODE);
                return true;
            }
            case O2OPC.SILENT_MODE:{
                Jarvis.ringModeChange(this, Jarvis.Mode.SILENT_MODE);
                return true;
            }
            case O2OPC.VIBRATE_MODE:{
                Jarvis.ringModeChange(this, Jarvis.Mode.VIBRATE_MODE);
                return true;
            }
            default:return false;
        }
    }
}
