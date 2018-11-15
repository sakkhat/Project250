package sakkhat.com.p250.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import sakkhat.com.p250.R;

/**
 * Created by hp on 05-Oct-18.
 */

public class ScreenAssistant extends Service {
    public static final String TAG = "floating_service";

    private WindowManager wManager;
    private WindowManager.LayoutParams params;
    private View floatingView;

    private GestureDetector detector;
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
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"on Create called");
        detector = new GestureDetector(getApplicationContext(), new Detector());

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_widget, null, false);
        params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        wManager.addView(floatingView,params);

        floatingView.findViewById(R.id.flow_widget).setLongClickable(true);


        floatingView.findViewById(R.id.flow_widget).setOnTouchListener(new View.OnTouchListener() {
            private float touchX;
            private float touchY;
            private int x = params.x;
            private int y = params.y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
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
            Log.d(TAG, "on Long Press");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.d(TAG, "On Fling called");
            return false;
        }
    }
}
