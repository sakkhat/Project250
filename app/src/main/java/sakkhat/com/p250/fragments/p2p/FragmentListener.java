package sakkhat.com.p250.fragments.p2p;

import android.os.Bundle;

/**
 * Created by hp on 03-Oct-18.
 */

public interface FragmentListener {
    /*
    * FragmentListener establish a communication between fragments and activity
    * by passing bundle from fragment to activity.
    * Interface is implemented in activity and set the reference to the fragment class.
    * For this when fragment call the onResponse with  a bundle this actually call the
    * activity's implemented onResponse method and do a particular task.
    * */
    public void onResponse(Bundle bundle);
}
