package sakkhat.com.p250.p2p;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.hdodenhof.circleimageview.CircleImageView;
import sakkhat.com.p250.R;

public class DataSharing extends AppCompatActivity {

    public static final String TAG = "data_sharing";
    private static final int PORT = 15151;
    private static final int MESSAGE_READ_REQUST = 1010;
    private static final int FILE_PICK_REQUEST = 1011;

    private ReadWrite readWrite;
    private boolean pageOnDisplay;

    private WifiP2pInfo p2pInfo;

    private Button btSend;
    private TextView mess;
    private CircleImageView imgUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sharing);

        // flag for display sharing process
        pageOnDisplay = true;

        // get the bundle from intent and take the p2p info
        Bundle intentBundle = getIntent().getExtras();
        p2pInfo = intentBundle.getParcelable(FragmentSharing.CONNECTION_INFO);

        // checkout this device is whether the host or client
        if(p2pInfo.groupFormed && p2pInfo.isGroupOwner){
            // start server socket thread for host
            ServerRunner host = new ServerRunner();
        }
        else if(p2pInfo.groupFormed){
            // start client socket thread for connect with host
            ClientRunner client = new ClientRunner(p2pInfo.groupOwnerAddress);
        }

        // initialize the UI components from XML
        init();
    }

    private void init(){
        btSend = (Button)findViewById(R.id.send);
        mess = (TextView)findViewById(R.id.socket_message);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initial test for send button, that will send the current time to another device
                String s = Long.toString(System.currentTimeMillis());
                readWrite.send(s.getBytes());
            }
        });

        imgUpload = (CircleImageView) findViewById(R.id.data_sharing_upload);
        imgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // file chooser implicit intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,FILE_PICK_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == FILE_PICK_REQUEST){
                // take uri
                Uri uri = data.getData();
                // make a file
                File file = new File(uri.getPath());

                Log.d(TAG,file.getAbsolutePath());
            }
        }
    }

    // handler for communicate with main looper for UI change with external thread
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READ_REQUST:{
                    // download notification for read a message from another device on UI
                    byte[] data = (byte[]) msg.obj;
                    // obtain the string from byte
                    String s = new String(data,0,msg.arg2);
                    // set the message on display
                    mess.setText(s);
                }   break;
            }
            return true;
        }
    });

    private class ServerRunner extends Thread {
        /*
        * Server Runner will create a server socket and bound with the defined @param PORT
        * constructor will start the thread itself and waiting for client request inside
        * run method
        * */
        private ServerSocket serverSocket;
        public ServerRunner(){
            // start the thread itself
            this.start();
        }

        @Override
        public void run() {
            Log.d(TAG,"server runner started");
            try{
                // bind the server socket with the port
                serverSocket = new ServerSocket(PORT);
                // request from client and accept
                Socket socket = serverSocket.accept();
                Log.d(TAG,"server socket connected: "+socket.getRemoteSocketAddress());

                // create a read write thread for host device
                readWrite = new ReadWrite(socket);
                // start the thread
                readWrite.start();

            } catch (IOException ex){
                Log.e(TAG,ex.toString());
            }
        }

    }

    private class ClientRunner extends Thread {
        /*
        * Client Runner will create a socket with group owner or more precisely host address
        * and port and request fot socket connection.
        * */
        private InetAddress host;
        public ClientRunner(InetAddress host){
            this.host = host;
            this.start();
        }

        @Override
        public void run() {
            Log.d(TAG, "client runner started");
            try{
                // bind a socket with host and port
                Socket socket = new Socket(host,PORT);
                Log.d(TAG, "client socket connected: "+socket.getInetAddress());

                // start the read-write thread for client device
                readWrite = new ReadWrite(socket);
                // start the thread
                readWrite.start();
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }

    private class ReadWrite extends Thread {
        /*
        * Read-Write thread will initialize with a socket; then this will create
        * buffered input and output stream with socket input and output stream.
        * Then run method always read the input stream and for any buffer data
        * come through input stream this will pass it to handler to make visible on UI.
        *
        * send method is called whenever this socket send any buffer data to connected
        * device
        * */
        private Socket socket;
        private BufferedInputStream bis;
        private BufferedOutputStream bos;

        public ReadWrite(Socket socket){
            this.socket = socket;

            try{
                // create buffered input stream
                bis = new BufferedInputStream(socket.getInputStream());
                // create buffered output stream
                bos = new BufferedOutputStream(socket.getOutputStream());

            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }

        @Override
        public void run() {
            Log.d(TAG,"reader thread started");
            // byte array of 1KB
            byte[] bufferData = new byte[1024];
            // indicate current buffer length
            int length;
            while(socket!= null || pageOnDisplay){
                try{
                    // read with byte array and get the length of buffer
                    length = bis.read(bufferData);
                    // check whether any data is available in the buffer
                    if(length > 0){
                        // send the buffer data to handler to handle with main looper
                        handler.obtainMessage(MESSAGE_READ_REQUST,-1,length,bufferData).sendToTarget();
                    }
                } catch (IOException ex){
                    Log.e(TAG, ex.toString());
                }
            }
        }

        public void send(byte[] data){
            try{
                // write the data on output stream
                bos.write(data);
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }
}
