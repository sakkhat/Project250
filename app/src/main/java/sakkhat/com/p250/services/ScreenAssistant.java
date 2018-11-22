package sakkhat.com.p250.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.Locale;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIButton;
import sakkhat.com.p250.R;
import sakkhat.com.p250.broadcaster.ServiceSwitcher;
import sakkhat.com.p250.accessories.FragmentAccessories;
import sakkhat.com.p250.jarvis.Jarvis;

/**
 * Created by hp on 05-Oct-18.
 */

public class ScreenAssistant extends Service implements AIButton.AIButtonListener{
    public static final String TAG = "floating_service";

    public static final String ACTION_NIGHT_ON = "s_a_n_off";
    public static final String ACTION_NIGHT_OFF = "s_a_n_on";
    public static final String ACTION_ASSIST_OFF = "s_a_off";

    private WindowManager wManager;
    private WindowManager.LayoutParams params;
    private View floatingView;
    private GestureDetector detector;
    private View popView;

    private ServiceSwitcher switcherBroadcaster;
    private IntentFilter intentFilter;

    private CardView btNight, btSwitch, btBase, btEmergency, btMinimize;
    private boolean isNightOn;


    // Jarvis
    private AIButton ai;
    private TextToSpeech tts;
    private Result result;

    public ScreenAssistant(){
        Log.d(TAG, "constructor called");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not ready yet");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(intent.getExtras() == null){
                return START_STICKY;
            }
            switch (intent.getAction()){
                case FragmentAccessories.TAG:{
                    isNightOn = intent.getBooleanExtra(NightLightService.SWITCH_KEY,false);
                    if(isNightOn){
                        btNight.setCardBackgroundColor(intent.getIntExtra(ACTION_NIGHT_ON, Color.parseColor("#000000")));
                    }
                    else{
                        btNight.setCardBackgroundColor(intent.getIntExtra(ACTION_NIGHT_OFF, Color.parseColor("#000000")));
                    }

                } break;
                case ACTION_NIGHT_OFF:{
                    btNight.setCardBackgroundColor(intent.getIntExtra(ACTION_NIGHT_OFF, Color.parseColor("#000000")));
                } break;
                case ACTION_NIGHT_ON:{
                    btNight.setCardBackgroundColor(intent.getIntExtra(ACTION_NIGHT_ON, Color.parseColor("#000000")));
                } break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"on Create called");

        detector = new GestureDetector(getApplicationContext(), new Detector());
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initFloatingView(inflater);
        initPopWindowView(inflater);

        wManager.addView(floatingView,params);
        wManager.addView(popView, params);

        initBroadcaster();
        initAI();
    }

    private void initFloatingView(LayoutInflater inflater){
        floatingView = inflater.inflate(R.layout.floating_widget, null, false);
        params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        floatingView.findViewById(R.id.flow_widget).setLongClickable(true);


        floatingView.findViewById(R.id.flow_widget).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
        floatingView.setFocusable(true);
    }

    private void initPopWindowView(LayoutInflater inflater){
        popView = inflater.inflate(R.layout.assist_pop_window, null, false);

        btBase = popView.findViewById(R.id.assist_pop_base);
        btBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btEmergency = popView.findViewById(R.id.assist_pop_emergency);
        btEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btMinimize = popView.findViewById(R.id.assist_pop_minimize);
        btMinimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popView.setEnabled(false);
                popView.setVisibility(View.GONE);

                floatingView.setVisibility(View.VISIBLE);
                floatingView.setEnabled(true);

                wManager.updateViewLayout(floatingView, params);
            }
        });


        btNight = popView.findViewById(R.id.assist_pop_night);
        btNight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ServiceSwitcher.class);
                if(isNightOn){
                    i.setAction(ACTION_NIGHT_OFF);
                    sendBroadcast(i);
                    isNightOn = false;
                }
                else{
                    i.setAction(ACTION_NIGHT_ON);
                    sendBroadcast(i);
                    isNightOn = true;
                }

            }
        });

        btSwitch = popView.findViewById(R.id.assist_pop_switch);
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ServiceSwitcher.class);
                i.setAction(ACTION_ASSIST_OFF);
                sendBroadcast(i);
            }
        });

        popView.setEnabled(false);
        popView.setVisibility(View.GONE);
    }

    private void initBroadcaster(){
        switcherBroadcaster = new ServiceSwitcher();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ASSIST_OFF);
        intentFilter.addAction(ACTION_NIGHT_OFF);
        intentFilter.addAction(ACTION_NIGHT_ON);

        registerReceiver(switcherBroadcaster, intentFilter);
    }

    private void initAI(){
        final AIConfiguration config=new AIConfiguration(Jarvis.TOKEN,
                AIConfiguration.SupportedLanguages.English,AIConfiguration.RecognitionEngine.System);

        ai = new AIButton(getBaseContext());
        ai.initialize(config);
        ai.setResultsListener(this);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"on destroy called");
        wManager.removeView(floatingView);
        wManager.removeView(popView);
        unregisterReceiver(switcherBroadcaster);

        stopForeground(true);
        stopSelf();
    }

    private class Detector extends GestureDetector.SimpleOnGestureListener{

        private int x;
        private int y;

        @Override
        public boolean onDown(MotionEvent e) {
            x = params.x;
            y = params.y;

            Log.d(TAG, "onDown called");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            floatingView.setEnabled(false);
            floatingView.setVisibility(View.GONE);

            popView.setEnabled(true);
            popView.setVisibility(View.VISIBLE);

            wManager.updateViewLayout(popView,params);
            Log.d(TAG, "on single tap called");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            params.x = x - (int)(e1.getRawX()-e2.getRawX());
            params.y = y - (int)(e1.getRawY()-e2.getRawY());

            wManager.updateViewLayout(floatingView,params);

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            ai.startListening();
            Log.d(TAG, "on Long Press");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }


    @Override
    public void onResult(AIResponse aiResult) {
        result = aiResult.getResult();
        switch (result.getAction().trim()){
            case Jarvis.Actions.JOKE:{
                // nothing
            } break;
            default:Jarvis.query(getApplicationContext(), result);
        }
        tts.speak(result.getFulfillment().getSpeech(),TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onCancelled() {

    }
}
