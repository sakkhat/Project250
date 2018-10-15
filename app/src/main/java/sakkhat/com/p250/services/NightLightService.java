package sakkhat.com.p250.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.ServiceUpater;
import sakkhat.com.p250.helper.Memory;
import sakkhat.com.p250.helper.ScreenSurface;


/**
 * Created by Rafiul Islam on 29-Sep-18.
 */

public class NightLightService extends Service {
    /**
     * Night Light Service provide the blue filter light on display
     * @param TAG class and log tag
     * @param LIGHT_KEY key for night light data
     * @param SWITCH_KEY key for night light switch
     * @param ACTION_ON_VALUE_CHANGFD intent filter tag for broadcaster
     * */

    public static final String TAG = "night_light_service";
    public static final String LIGHT_KEY = "light_key";
    public static final String SWITCH_KEY = "light_switch";
    public static final String ACTION_ON_VALUE_CHANGED = "night_light_value_changed";

    private ScreenSurface screenAlpha;
    private ScreenSurface screenFilter;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private ServiceUpater receiver;
    private IntentFilter intentFilter;

    /*
    * Default constructor for Night Light Service
    * */
    public NightLightService(){
        Log.d(TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init(); // night filter color
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(intent.getExtras() == null){
                // no extras from intent
                return START_STICKY;
            }
            // update the changes from UI to service
            int alpha = Memory.retrieveInt(getApplicationContext(),LIGHT_KEY);
            screenAlpha.setBackgroundColor(Color.argb(alpha*2,0,0,0));
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("not ready yet");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);// broadcaster turned off
        windowManager.removeView(screenFilter);// screen filter view removed
        windowManager.removeView(screenAlpha);// screen light view removed
        Memory.save(getApplicationContext(),SWITCH_KEY,false);// switch turned off
        stopSelf();
        Log.d(TAG, "onDestroy called");
    }

    /*
    * initialization of components and other stuffs
    * */
    private void init(){
        //--------------------------------- Screen Alpha ---------------------------------------
        screenAlpha = new ScreenSurface(this);
        int alpha = Memory.retrieveInt(getApplicationContext(),LIGHT_KEY);
        screenAlpha.setBackgroundColor(Color.argb(alpha*2,0,0,0));
        //--------------------------------------------------------------------------------------

        //-------------------------------- Screen Filter ----------------------------------------
        screenFilter = new ScreenSurface(this);
        screenFilter.setBackgroundColor(this.getResources().getColor(R.color.nightFilter));
        //---------------------------------------------------------------------------------------

        //--------------------- Window Manager and Layout Paramas-----------------------------
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;// flag layout in screen

        windowManager.addView(screenFilter, params);// set filter view on screen
        windowManager.addView(screenAlpha, params);// set alpha view on screen
        //--------------------------------------------------------------------------------------

        //----------------- register broadcaster receiver for value updater ---------------------
        receiver = new ServiceUpater();
        intentFilter = new IntentFilter(ACTION_ON_VALUE_CHANGED);
        registerReceiver(receiver, intentFilter);
        //---------------------------------------------------------------------------------------
    }
}
