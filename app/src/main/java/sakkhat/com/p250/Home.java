package sakkhat.com.p250;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import github.hellocsl.cursorwheel.CursorWheelLayout;
import sakkhat.com.p250.adapter.MenuWheelAdapter;
import sakkhat.com.p250.broadcaster.WiFiStateReceiver;
import sakkhat.com.p250.fragments.FragmentAccessories;
import sakkhat.com.p250.fragments.FragmentJarvis;
import sakkhat.com.p250.fragments.FragmentSharing;
import sakkhat.com.p250.structure.MenuItem;

public class Home extends AppCompatActivity implements CursorWheelLayout.OnMenuSelectedListener{

    public static final String TAG = "home_view";

    public FragmentManager fragmentManager;
    public FrameLayout homeFrame;

    private List<MenuItem> menuItems;
    private CursorWheelLayout menuLayout;
    private MenuWheelAdapter menuAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initHome();
    }

    private void initHome(){
        /*
        * initialize all fixed home contents and providers
        * */


        // initialize the menu list
        menuItems = new ArrayList<>();
        // add menu options
        // first item is accessories
        menuItems.add(new MenuItem("Accessory",R.drawable.ic_settings));
        menuItems.add(new MenuItem("Sharing",R.drawable.ic_share));
        menuItems.add(new MenuItem("Jarvis",R.drawable.ic_jarvis));

        // instance of a menu wheel adapter
        menuAdapter = new MenuWheelAdapter(this, menuItems);

        // connect menu whhel with xml
        menuLayout = (CursorWheelLayout) findViewById(R.id.menu_wheel);
        // set the adapter with the wheel layout
        menuLayout.setAdapter(menuAdapter);
        // set menu selected listener
        menuLayout.setOnMenuSelectedListener(this);

        // access the home frame from XML
        homeFrame = (FrameLayout) findViewById(R.id.home_frame);

        // instance of fragment manager
        fragmentManager = getSupportFragmentManager();
        // use transection for add accessories fragment set on home frame
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(homeFrame.getId(),new FragmentAccessories());
        transaction.commit();

    }

    @Override
    public void onItemSelected(CursorWheelLayout parent, View view, int pos) {
        /*
        * Wheel Menu item selection listener interface implementation
        *
        *  pos 0: Accessory : FragmentAccessories
        *  pos 1: Sharing : FragmentSharing
        *  pos 2: Jarvis : FragmentJarvis
        * */
        switch (pos){
            case 0: replaceFragment(new FragmentAccessories());
                break;
            case 1: replaceFragment(new FragmentSharing());
                break;
            case 2: replaceFragment(new FragmentJarvis());
                break;
        }
        Log.d(TAG,menuItems.get(pos).getName());
    }

    private void replaceFragment(Fragment newFragment){
        // take the current fragment
        Fragment oldFragment = fragmentManager.findFragmentById(homeFrame.getId());
        // check whether it request for current fragment -> then return
        if(oldFragment == newFragment){
            Log.w(TAG,"fragement request is not accepted");
            return;
        }
        // set the new fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(homeFrame.getId(),newFragment);
        transaction.commit();

        Log.d(TAG,"fragment replaced from "+oldFragment+" to "+newFragment);
    }
}
