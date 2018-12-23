package sakkhat.com.p250.p2p;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import sakkhat.com.p250.R;

public class FragmentSharing extends Fragment{

    public static final String TAG = "fragment_sharing";// class tag

    private View root; // fragment root view
    private Context context; // base context

    private Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_sharing,null,false);
        context = getContext(); // get the base context

        init();

        return root;
    }

    private void init(){
        button = root.findViewById(R.id.frag_share_gotoShare);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,FileSharing.class));
            }
        });
    }
}
