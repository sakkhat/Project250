package sakkhat.com.p250;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import github.hellocsl.cursorwheel.CursorWheelLayout;
import sakkhat.com.p250.adapter.MenuWheelAdapter;
import sakkhat.com.p250.accessories.FragmentAccessories;
import sakkhat.com.p250.jarvis.FragmentJarvis;
import sakkhat.com.p250.helper.FragmentListener;
import sakkhat.com.p250.p2p.FragmentSharing;
import sakkhat.com.p250.structure.MenuItem;

public class Home extends AppCompatActivity
        implements CursorWheelLayout.OnMenuSelectedListener,FragmentListener{

    public static final String TAG = "home_view";

    public static final String FRAGMENT_TAG = "fragment_id";

    public FragmentManager fragmentManager;
    public FrameLayout homeFrame;
    public HashMap<String,String>installedapp;
    private List<MenuItem> menuItems;
    private CursorWheelLayout menuLayout;
    private MenuWheelAdapter menuAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //listingApp();
        initHome();
    }

    public  void listingApp()
    {
        List<PackageInfo>pack=getPackageManager().getInstalledPackages(0);
        for(PackageInfo p:pack)
        {
            String app=p.applicationInfo.loadLabel(getPackageManager()).toString();
            String pack_name=p.packageName;
            installedapp.put(app,pack_name);
        }

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
            case 0: {
                FragmentAccessories accessories = new FragmentAccessories();
                //accessories.setFragmentListener(this);
                replaceFragment(accessories, false);
            }   break;
            case 1: {
                FragmentSharing sharing = new FragmentSharing();
                replaceFragment(sharing, false);
            }   break;
            case 2: {
                FragmentJarvis jarvis = new FragmentJarvis();
                jarvis.setFragmentListener(this);
                replaceFragment(jarvis, false);
            }   break;
        }
        Log.d(TAG,menuItems.get(pos).getName());
    }

    private void replaceFragment(Fragment newFragment, boolean isStacked){
        // take the current fragment
        Fragment oldFragment = fragmentManager.findFragmentById(homeFrame.getId());
        // check whether it request for current fragment -> then return
        if(oldFragment == newFragment){
            // null reference the new fragment for through in garbage collection
            newFragment = null;
            Log.w(TAG,"fragement request is not accepted");
            return;
        }
        // set the new fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(homeFrame.getId(),newFragment);
        // if stacked then fragment will attach a independent layer
        if(isStacked){
            transaction.addToBackStack(newFragment.getClass().getName());
        }
        transaction.commit();

        Log.d(TAG,"fragment replaced from "+oldFragment+" to "+newFragment);
    }

    @Override
    public void onResponse(Bundle bundle) {

        String caller = bundle.getString(FRAGMENT_TAG);

        switch (caller){
            // P2P sharing fragment
            case FragmentSharing.TAG:{

            }   break;
        }
    }

}
