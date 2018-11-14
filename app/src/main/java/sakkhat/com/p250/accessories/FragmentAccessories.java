package sakkhat.com.p250.accessories;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.ServiceUpater;
import sakkhat.com.p250.helper.FileUtil;
import sakkhat.com.p250.helper.FragmentListener;
import sakkhat.com.p250.helper.Memory;
import sakkhat.com.p250.services.NightLightService;

public class FragmentAccessories extends Fragment{

    public static final String TAG = "fragment_accessories";// class tag
    private static final int ALERT_WINDOW_PERMISSOIN = 973;

    private View root;// root view by layout inflate
    private Context context;// context of this fragment
    private FragmentListener fragmentListener; // fragment listener for communication with base activity

    private SeekBar nightLightBar;
    private Switch nightLightSwitch;

    private Button btTest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_accessories,null,false);
        context = getContext();
        init(); // initialization of other stuffs

        btTest = (Button)root.findViewById(R.id.bt_test);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, 2);
            }
        });
        return root;
    }

    /*
    * initialize all components and other stuffs
    * */
    private void init(){
        //------------------------ Night Light Seek Bar -------------------------------------------------
        nightLightBar = (SeekBar)root.findViewById(R.id.frag_access_night_light_bar);
        nightLightBar.setMax(100); // set max progress value
        int progressed = Memory.retrieveInt(context,NightLightService.LIGHT_KEY);// fetched previous progressed value
        if(progressed == Memory.DEFAUL_INT){
            // no previous progress data available in shared preference
            nightLightBar.setProgress(50); // set progress 50 by default
        }
        else{
            nightLightBar.setProgress(progressed);// set as previous progressed
        }
        nightLightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!nightLightBar.isActivated()) return;
                Memory.save(context, NightLightService.LIGHT_KEY,progress);//save the current progress

                // send a broadcast message to update in service also
                Intent broadcast = new Intent(context, ServiceUpater.class);
                broadcast.setAction(NightLightService.ACTION_ON_VALUE_CHANGED);
                context.sendBroadcast(broadcast);

                Log.d(TAG, "progress: "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //------------------------------------------------------------------------------------------------

        //-------------------------------- Night Light Switch --------------------------------------------
        nightLightSwitch = (Switch)root.findViewById(R.id.frag_access_night_mood_switch);
        boolean switched = Memory.retrieveBool(context, NightLightService.SWITCH_KEY);// retrieve night mode is on or off
        nightLightSwitch.setChecked(switched); // set switched value
        nightLightBar.setActivated(switched); // set activated value

        nightLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        // request for permission
                        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SYSTEM_ALERT_WINDOW)
                                != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(getActivity(),new String[]
                                    {Manifest.permission.SYSTEM_ALERT_WINDOW},ALERT_WINDOW_PERMISSOIN);
                        }
                    }
                    else{
                        nightLight();
                    }
                }
                else{
                    Memory.save(context,NightLightService.SWITCH_KEY,false);// save the switched off status
                    nightLightBar.setActivated(false);// deactivated the seek bar
                    context.stopService(new Intent(context, NightLightService.class));//stop service
                    Log.d(TAG, "night mode deactivated");
                }
            }
        });
        //------------------------------------------------------------------------------------------------
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == ALERT_WINDOW_PERMISSOIN){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                nightLight();
            }
        }
    }

    private void nightLight(){
        Memory.save(context,NightLightService.SWITCH_KEY,true);// save switched on status
        nightLightBar.setActivated(true);// actiaved the seek bar to perform
        if(Memory.retrieveInt(context,NightLightService.LIGHT_KEY) == Memory.DEFAUL_INT){
            // no night light value set
            Memory.save(context,NightLightService.LIGHT_KEY,50);
        }
        int progressed = Memory.retrieveInt(context,NightLightService.LIGHT_KEY);// fetch progressed value
        nightLightBar.setProgress(progressed); // update in seek bar
        context.startService(new Intent(context, NightLightService.class));//start service
        Log.d(TAG, "night mode activated");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == 2){
                Log.e(TAG, data.getData().toString());
                String s = FileUtil.getPath(context, data.getData());
                if(s != null){
                    File f = new File(s);
                    Log.w(TAG, f.getName());
                    Log.w(TAG, Long.toString(f.length()));

                    try {
                        FileInputStream fis = new FileInputStream(s);
                        fis.close();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, e.toString());
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }

                }

            }
        }
    }
}
