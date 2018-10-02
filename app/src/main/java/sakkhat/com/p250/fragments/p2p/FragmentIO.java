package sakkhat.com.p250.fragments.p2p;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import sakkhat.com.p250.R;

public class FragmentIO extends Fragment {

    public static final String TAG = "fragment_io";
    public static final int FILE_PICK_REQUEST = 13;

    private View root;
    private Context context;

    private CircleImageView imgUpload;
    private TextView textPeer;

    private WifiP2pInfo info;
    private FragmentListener fragmentListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_io,null,false);
        context = getContext();
        init();
        return root;
    }

    public void setInfo(WifiP2pInfo info){
        this.info = info;
    }

    public void setFragmentListener(FragmentListener fragmentListener){
        this.fragmentListener = fragmentListener;
    }

    private void init(){
        imgUpload = root.findViewById(R.id.frag_io_upload);
        imgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // file chooser intent create
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_PICK_REQUEST);
            }
        });

        textPeer = (TextView) root.findViewById(R.id.frag_io_peer_name);
        textPeer.setText(info.groupOwnerAddress.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == FILE_PICK_REQUEST){
                Log.d(TAG,"result ok, request ok");
                Uri uri = data.getData();
                Log.d(TAG,uri.toString());
            }
        }
    }
}
