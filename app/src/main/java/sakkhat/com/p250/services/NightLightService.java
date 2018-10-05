package sakkhat.com.p250.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by hp on 29-Sep-18.
 */

public class NightLightService extends Service {

    public static final String TAG = "night_light_service";

    public NightLightService(){
        Log.d(TAG, "constructor called");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"on create called");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not ready yet");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            //intent.getExtras();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on destroy called");
        stopSelf();
    }
}
