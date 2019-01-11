package sakkhat.com.p250.accessories;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import sakkhat.com.p250.R;

public class About extends AppCompatActivity {

    private static final String TAG = "about_p250";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final Button btHome = (Button) findViewById(R.id.about_bt_home);
        final Button btBase = (Button) findViewById(R.id.about_bt_base);

        btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHome();
            }
        });

        btBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBase();
            }
        });
    }

    private void launchBase(){
        Intent i = getPackageManager().getLaunchIntentForPackage("sakkhat.com.p250");
        if(i != null){
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        else{
            Log.e(TAG, "project package not found");
        }
    }

    private void launchHome(){
        Intent home = new Intent(Intent.ACTION_MAIN, null);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(home);
    }
}
