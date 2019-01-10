package sakkhat.com.p250.broadcaster;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.sql.Time;

import sakkhat.com.p250.jarvis.Jarvis;

public class JarvisScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "on :");
        wl.acquire();

        switch (intent.getAction()){
            case Jarvis.Mode.RING_MODE:
                Jarvis.ringModeChange(context, Jarvis.Mode.RING_MODE); break;
            case Jarvis.Mode.SILENT_MODE:
                Jarvis.ringModeChange(context, Jarvis.Mode.SILENT_MODE); break;
            case Jarvis.Mode.VIBRATE_MODE:
                Jarvis.ringModeChange(context, Jarvis.Mode.VIBRATE_MODE); break;
        }

        wl.release();
    }

    public void setSchedule(Context context, Long millisec, String action) {
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, JarvisScheduler.class);
        i.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), millisec, pi);
    }

    public void cancelSchedule(Context context){
        Intent intent = new Intent(context, JarvisScheduler.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
