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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.Random;

import sakkhat.com.p250.R;

/**
 * Created by hp on 05-Oct-18.
 */

public class FloatingWidget extends Service {
    public static final String TAG = "floating_service";

    private WindowManager wManager;
    private WindowManager.LayoutParams params;
    private View floatingView;

    private Random rand;
    public FloatingWidget(){

        rand = new Random();

        Log.d(TAG, "constructor called");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not ready yet");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"on Create called");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_widget, null, false);
        params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        wManager.addView(floatingView,params);

        floatingView.findViewById(R.id.flow_widget).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent("ServiceStopper");
                intent.putExtra("tag",TAG);
                sendBroadcast(intent);
                Log.d(TAG,"long pressed on widget");
                return true;
            }
        });

        floatingView.findViewById(R.id.flow_widget).setLongClickable(true);

        floatingView.findViewById(R.id.flow_widget).setOnTouchListener(new View.OnTouchListener() {
            private float touchX;
            private float touchY;
            private int x = params.x;
            private int y = params.y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        x = params.x;
                        y = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();

                        Log.d(TAG,"widget touch request");

                        return false;
                    }

                    case MotionEvent.ACTION_MOVE:{
                        params.x = x-(int)(touchX-event.getRawX());
                        params.y = y-(int)(touchY-event.getRawY());
                        wManager.updateViewLayout(floatingView,params);

                        return true;
                    }
                    case MotionEvent.ACTION_UP:{
                        if(touchX == event.getRawX() && touchY == event.getRawY()){
                            // on touch triggered
                            int r = rand.nextInt(255);
                            int g = rand.nextInt(255);
                            int b = rand.nextInt(255);
                            floatingView.setBackgroundColor(Color.rgb(r,g,b));

                            return false;
                        }
                        Log.d(TAG,"widget touch released");
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"on destroy called");
        wManager.removeView(floatingView);
        stopForeground(true);
        stopSelf();
    }
}
