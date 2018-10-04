package sakkhat.com.p250.jarvis;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sakkhat.com.p250.R;
import sakkhat.com.p250.fragments.FragmentListener;

public class FragmentJarvis extends Fragment {

    private View root;
    private Context context;

    private FragmentListener fragmentListener;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_jarvis,null, false);
        init();
        return root;
    }

    public void setFragmentListener(FragmentListener fragmentListener){
        this.fragmentListener = fragmentListener;
    }
    private void init(){
        /*
        * instance and initialize
        * */

        // instance of base context;
        context = getContext();
    }
}
