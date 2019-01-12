package sakkhat.com.p250.p2p;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import sakkhat.com.p250.R;

public class PCSharing extends AppCompatActivity implements Handler.Callback{

    private static final String TAG = "pc_share";

    private Button ipMessenger, hostRequest;
    private EditText ipAddress, ipPort;
    private TextView ipInfo, sendFName, receiveFName;
    private ProgressBar sendingProgress, receivingProgress;

    private FloatingActionButton btDisconnect, btFilePick, btSend;

    private LinearLayout beforeView;
    private RelativeLayout afterView;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcsharing);

        init();
    }

    private void init(){

        handler = new Handler(this);

        ipMessenger = (Button) findViewById(R.id.pc_share_ip_messenger);
        hostRequest = (Button) findViewById(R.id.pc_share_request);
        ipAddress = (EditText) findViewById(R.id.pc_share_ip_address);
        ipPort = (EditText) findViewById(R.id.pc_share_ip_port);
        ipInfo = (TextView) findViewById(R.id.pc_share_ip_info);

        sendFName = (TextView) findViewById(R.id.pc_share_sending_fname);
        receiveFName = (TextView) findViewById(R.id.pc_share_receiving_fname);
        sendingProgress = (ProgressBar) findViewById(R.id.pc_share_sending_progress);
        receivingProgress = (ProgressBar) findViewById(R.id.pc_share_receiving_progress);

        btDisconnect = (FloatingActionButton) findViewById(R.id.pc_share_disconnect);
        btFilePick = (FloatingActionButton) findViewById(R.id.pc_share_file_pick);
        btSend = (FloatingActionButton) findViewById(R.id.pc_share_sent);

        beforeView = (LinearLayout) findViewById(R.id.pc_share_before_view);
        afterView = (RelativeLayout) findViewById(R.id.pc_share_after_view);


        ipMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/rafiulgits/IP-Messenger"));
                startActivity(Intent.createChooser(i, "Action chooser"));
            }
        });

        hostRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ipAddress.getText().toString().isEmpty()){
                    ipAddress.setError("");
                }
                else if(ipPort.getText().toString().isEmpty()){
                    ipPort.setError("");
                }
                else{
                    new O2O.Client(handler, ipAddress.getText().toString().trim(),
                            Integer.parseInt(ipPort.getText().toString().trim()));

                    Toast.makeText(PCSharing.this, "trying to connecting", Toast.LENGTH_LONG).show();
                }
            }
        });

        try {
            ipInfo.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            Log.e(TAG, e.toString());
            ipInfo.setText("unable to fetch");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
