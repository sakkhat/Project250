package sakkhat.com.p250.jarvis;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;

import ai.api.model.Result;

/**
 * Created by Rafiul Islam on 19-Nov-18.
 */

public class Jarvis {
    public static final String TOKEN = "c93846a85d5044d09e0db0efc99108ff";
    public static final String TAG = "jarvis_base";
    public static final String JARVIS_SCREEN = "jarvis_screen";

    public static class Actions{
        public static final String APP_SWITCHER = "app_switch";
        public static final String RING_MODE_CHANGE = "mode_change";
        public static final String JOKE = "joke";
        public static final String CONTACT_FNF = "contact_fnf";
    }

    public static class Params{
        public static final String MODE = "mode";
        public static final String PERIOD = "period";
        public static final String APP_NAME = "app_name";
        public static final String FNF = "fnf";
    }

    public static void query(Context context, Result result, String callLocation){
        switch (result.getAction().trim()){
            case Jarvis.Actions.APP_SWITCHER:{
                switch (result.getStringParameter(Jarvis.Params.APP_NAME).toLowerCase()){
                    case "browser": swicthToBrowser(context); break;
                    //case "music": switchToMusicPlayer(context); break;
                    //case "youtube": switchToYoutube(context, "Search");break;
                    default:break; // general application opener
                }
            } break;

            case Jarvis.Actions.RING_MODE_CHANGE:{
                String mode = result.getStringParameter(Jarvis.Params.MODE);
                String period = result.getStringParameter(Jarvis.Params.PERIOD, null);

                Jarvis.ringModeChange(context, mode, period);
            } break;

            case Jarvis.Actions.CONTACT_FNF:{
                String fnf = result.getStringParameter(Jarvis.Params.FNF);
                String method = result.getStringParameter(Jarvis.Params.APP_NAME);
            } break;
        }
    }
    private static void swicthToBrowser(Context context){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://google.com"));
        context.startActivity(i);
    }

    private static void switchToMusicPlayer(Context context){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setType("music/*");
        context.startActivity(i);
    }

    private static void switchToYoutube(Context context, String query){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.google.android.youtube","com.google.android.youtube.WatchActivity");
        if(query != null){
            i.setData(Uri.parse("https://www.youtube.com/results?search_query="+query));
        }
        context.startActivity(i);
    }

    private static void ringModeChange(Context context, String mode, String period){
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(mode.equalsIgnoreCase("silent")){
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else if (mode.equalsIgnoreCase("ring")) {
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }
}
