package sakkhat.com.p250.helper;

/**
 * Created by Rafiul Islam on 14-Nov-18.
 */

public class Sizer {

    private static final int MB = 1048576;
    private static final int GB = 1073741824;

    public static int maxFor(long bytes){
        if(bytes >= GB){
            return (int)bytes/GB;
        }
        else if(bytes <GB && bytes > MB){
            return (int)bytes/MB;
        }
        return (int)bytes;
    }
}
