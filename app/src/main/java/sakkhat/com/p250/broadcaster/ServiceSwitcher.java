package sakkhat.com.p250.broadcaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import sakkhat.com.p250.R;
import sakkhat.com.p250.helper.Memory;
import sakkhat.com.p250.services.NightLightService;
import sakkhat.com.p250.services.ScreenAssistant;

/**
 * Created by Rafiul Islam on 05-Oct-18.
 */

public class ServiceSwitcher extends BroadcastReceiver {

    public ServiceSwitcher(){

    }
    public void onReceive(Context context, Intent intent){
        switch (intent.getAction()){
            case ScreenAssistant.ACTION_ASSIST_OFF:{
                context.stopService(new Intent(context, ScreenAssistant.class));
                Memory.save(context, ScreenAssistant.TAG, false);
            } break;

            case ScreenAssistant.ACTION_NIGHT_OFF:{
                Intent i = new Intent(context, NightLightService.class);
                context.stopService(i);
                Memory.save(context,NightLightService.SWITCH_KEY,false);// save switched on status

                i = new Intent(context, ScreenAssistant.class);
                i.setAction(ScreenAssistant.ACTION_NIGHT_OFF);
                i.putExtra(ScreenAssistant.ACTION_NIGHT_OFF, ContextCompat.getColor(context, R.color.white));
                context.startService(i);

            } break;

            case ScreenAssistant.ACTION_NIGHT_ON:{
                Intent i = new Intent(context, NightLightService.class);
                context.startService(i);
                Memory.save(context,NightLightService.SWITCH_KEY,true);// save switched on status

                i = new Intent(context, ScreenAssistant.class);
                i.setAction(ScreenAssistant.ACTION_NIGHT_ON);
                i.putExtra(ScreenAssistant.ACTION_NIGHT_ON, ContextCompat.getColor(context, R.color.bg_wheel));
                context.startService(i);

            } break;
        }
    }
}
