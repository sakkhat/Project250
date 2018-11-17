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
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;


import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.ServiceUpater;
import sakkhat.com.p250.helper.FragmentListener;
import sakkhat.com.p250.helper.Memory;
import sakkhat.com.p250.services.ScreenAssistant;
import sakkhat.com.p250.services.NightLightService;

public class FragmentAccessories extends Fragment{

    public static final String TAG = "fragment_accessories";// class tag
    private static final int NIGHT_MODE_PERMISSION = 973;
    private static final int SCREEN_ASSIST_PERMISSION = 375;

    private View root;// root view by layout inflate
    private Context context;// context of this fragment
    private FragmentListener fragmentListener; // fragment listener for communication with base activity

    private SeekBar nightLightBar;
    private Switch nightLightSwitch;
    private Switch screenAssistSwitch;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_accessories,null,false);
        context = getContext();
        init(); // initialization of other stuffs



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
                                    {Manifest.permission.SYSTEM_ALERT_WINDOW},NIGHT_MODE_PERMISSION);
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


        //----------------------------------- Screen Assistant Switch ------------------------------------------------
        screenAssistSwitch = root.findViewById(R.id.frag_access_screen_assist_switch);
        screenAssistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        // request for permission
                        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SYSTEM_ALERT_WINDOW)
                                != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(getActivity(),new String[]
                                    {Manifest.permission.SYSTEM_ALERT_WINDOW},SCREEN_ASSIST_PERMISSION);
                        }
                    }
                    else{
                        Intent i = new Intent(context, ScreenAssistant.class);
                        i.setAction(TAG);
                        boolean isNightOn = Memory.retrieveBool(context, NightLightService.SWITCH_KEY);
                        i.putExtra(NightLightService.SWITCH_KEY, isNightOn);
                        if(isNightOn){
                            i.putExtra(ScreenAssistant.ACTION_NIGHT_OFF, ContextCompat.getColor(context, R.color.white));
                        }
                        else{
                            i.putExtra(ScreenAssistant.ACTION_NIGHT_ON, ContextCompat.getColor(context, R.color.bg_wheel));
                        }
                        context.startService(i);
                        Memory.save(context, ScreenAssistant.TAG, true);
                    }
                }
                else{
                    context.stopService(new Intent(context, ScreenAssistant.class));
                    Memory.save(context, ScreenAssistant.TAG, false);
                }
            }
        });
        screenAssistSwitch.setChecked(Memory.retrieveBool(context, ScreenAssistant.TAG));
        //------------------------------------------------------------------------------------------------------------
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == NIGHT_MODE_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                nightLight();
            }
        }
        else if(requestCode == SCREEN_ASSIST_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent i = new Intent(context, ScreenAssistant.class);
                i.setAction(TAG);
                boolean isNightOn = Memory.retrieveBool(context, NightLightService.SWITCH_KEY);
                i.putExtra(NightLightService.SWITCH_KEY, isNightOn);
                if(isNightOn){
                    i.putExtra(ScreenAssistant.ACTION_NIGHT_OFF, ContextCompat.getColor(context, R.color.white));
                }
                else{
                    i.putExtra(ScreenAssistant.ACTION_NIGHT_ON, ContextCompat.getColor(context, R.color.bg_wheel));
                }
                context.startService(i);
                Memory.save(context, ScreenAssistant.TAG, true);
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
    
}
