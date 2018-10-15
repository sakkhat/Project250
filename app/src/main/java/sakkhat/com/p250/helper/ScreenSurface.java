package sakkhat.com.p250.helper;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by Rafiul Islam on 15-Oct-18.
 */

public class ScreenSurface extends ViewGroup {
    /**
     * To make a overlap view on screen.
     * This surface color make an abstraction of night mode color on display screen.
     *
     * Default constructor
     * @param context base context
     * */
    public ScreenSurface(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
