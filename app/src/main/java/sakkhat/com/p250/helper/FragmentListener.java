package sakkhat.com.p250.helper;

import android.os.Bundle;

/**
 * Created by Rafiul Islam on 03-Oct-18.
 */

public interface FragmentListener {
    /*
    * FragmentListener establish a communication between fragments and activity
    * by passing bundle from fragment to activity.
    * Interface is implemented in activity and set the reference to the fragment class.
    * For this when fragment call the onResponse with  a bundle this actually call the
    * activity's implemented onResponse method and do a particular task.
    * */

    /**
     * @param bundle Data bundle from fragment to activity
     * */
    public void onResponse(Bundle bundle);
}
