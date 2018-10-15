package sakkhat.com.p250.broadcaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import sakkhat.com.p250.services.NightLightService;

/**
 * Created by Rafiul Islam on 15-Oct-18.
 */

public class ServiceUpater extends BroadcastReceiver {

    public static final String TAG = "service_updater_broadcaster";

    public ServiceUpater(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(NightLightService.ACTION_ON_VALUE_CHANGED)){
            Intent service = new Intent(context, NightLightService.class);
            service.putExtra(NightLightService.ACTION_ON_VALUE_CHANGED,true);
            context.startService(service);
        }
    }
}
