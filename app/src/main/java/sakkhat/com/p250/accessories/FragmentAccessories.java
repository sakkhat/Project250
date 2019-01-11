package sakkhat.com.p250.accessories;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import android.widget.TextView;


import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.ServiceUpater;
import sakkhat.com.p250.helper.FragmentListener;
import sakkhat.com.p250.helper.Memory;
import sakkhat.com.p250.services.ScreenAssistant;
import sakkhat.com.p250.services.NightLightService;


public class FragmentAccessories extends Fragment{

    public static final String TAG = "fragment_accessories";// class tag
    private static final int NIGHT_LIGHT_PERMISSION = 973;
    private static final int SCREEN_ASSIST_PERMISSION = 974;

    private View root;// root view by layout inflate
    private Context context;// context of this fragment
    private FragmentListener fragmentListener; // fragment listener for communication with base activity

    private SeekBar nightLightBar;
    private Switch nightLightSwitch;
    private Switch screenAssistSwitch;
    private TextView aboutView;
    private Button buttonLicense;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_accessories,null,false);
        context = getContext();
        init(); // initialization of other stuffss
        initEvents();
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
        //------------------------------------------------------------------------------------------------

        //-------------------------------- Night Light Switch --------------------------------------------
        nightLightSwitch = (Switch)root.findViewById(R.id.frag_access_night_mood_switch);
        boolean switched = Memory.retrieveBool(context, NightLightService.SWITCH_KEY);// retrieve night mode is on or off
        nightLightSwitch.setChecked(switched); // set switched value
        nightLightBar.setActivated(switched); // set activated value
        //------------------------------------------------------------------------------------------------


        //----------------------------------- Screen Assistant Switch ------------------------------------------------
        screenAssistSwitch = root.findViewById(R.id.frag_access_screen_assist_switch);
        screenAssistSwitch.setChecked(Memory.retrieveBool(context, ScreenAssistant.TAG));
        //------------------------------------------------------------------------------------------------------------

        aboutView = root.findViewById(R.id.frag_access_aboout);
        buttonLicense=root.findViewById(R.id.buttonLicense);
    }

    private void initEvents(){
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

        screenAssistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        // request for permission
                        checkPermission(SCREEN_ASSIST_PERMISSION);
                    }
                    else{
                        turnOnScreenAssist();
                        Log.d(TAG,"screen assist permission already allowed");
                    }
                }
                else{
                    context.stopService(new Intent(context, ScreenAssistant.class));
                    Memory.save(context, ScreenAssistant.TAG, false);
                }
            }
        });

        nightLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        // request for permission
                        checkPermission(NIGHT_LIGHT_PERMISSION);
                    }
                    else{
                        turnNightLightOn();
                        Log.d(TAG,"night light permission already allowed");
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

        aboutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, About.class));
            }
        });

        buttonLicense.setOnClickListener(new  View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent =new Intent(context,About.class);
                startActivity(intent);
            }
        });
    }




    private void turnNightLightOn(){
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

    private void turnOnScreenAssist(){
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission(int permissionCode){
        if(!Settings.canDrawOverlays(context)){
            if(permissionCode == NIGHT_LIGHT_PERMISSION){
                Log.w(TAG,"night light request for permission");
            }
            else if(permissionCode == SCREEN_ASSIST_PERMISSION){
                Log.w(TAG,"screen assist request for permission");
            }
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+ context.getPackageName()));
            startActivityForResult(i, permissionCode);
        }
        else{
            if(permissionCode == SCREEN_ASSIST_PERMISSION){
                turnOnScreenAssist();
            }
            else if(permissionCode == NIGHT_LIGHT_PERMISSION){
                turnNightLightOn();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == SCREEN_ASSIST_PERMISSION){
                if(!Settings.canDrawOverlays(context)){
                    checkPermission(SCREEN_ASSIST_PERMISSION);
                }
                else{
                    turnOnScreenAssist();
                    Log.d(TAG, "screen assist on");
                }
            }
            else if(requestCode == NIGHT_LIGHT_PERMISSION){
                if(!Settings.canDrawOverlays(context)){
                    checkPermission(NIGHT_LIGHT_PERMISSION);
                }
                else{
                    turnNightLightOn();
                    Log.d(TAG,"night light on");
                }
            }
        }
    }
}
