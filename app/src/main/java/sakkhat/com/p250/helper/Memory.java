package sakkhat.com.p250.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rafiul Islam on 15-Oct-18.
 */

public class Memory {
    /**
     * Memory class handle the shared preference data to store and retrieve.
     * @param NAME shared preference file name.
     * @param DEFAULT_INT default integer value for retrieve an integer.
     * @param DEFAULT_STRING default string value for retrieve the string value.
     * */
    private static final String NAME = "my_data";
    public static final String DEFAULT_STRING = "N/A";
    public static final int DEFAUL_INT = -90000;
    public static final boolean DEFAULT_BOOL = false;

   /**
    * To save an integer value in shared preference
    * @param context view context
    * @param key key of the data
    * @param data integer value to save
    * @return confirmation message of commitment of shared preference editor
    * */
    public static boolean save(Context context, String key, int data){
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, data);
        return editor.commit();
    }

    /**
     * To save a string value in shared preference
     * @param context view context
     * @param key key of the data
     * @param data string value to save
     * @return confirmation message of commitment of shared preference editor
     * */
    public static boolean save(Context context, String key, String data){
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, data);
        return editor.commit();
    }

    /**
     * To save a boolean value in shared preference
     * @param context view context
     * @param key key of the data
     * @param data string value to save
     * @return confirmation message of commitment of shared preference editor
     * */
    public static boolean save(Context context, String key, boolean data){
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, data);
        return editor.commit();
    }

    /**
     * To retrieve an integer value from shared preference
     * @param context view context
     * @param key key of the data
     * @return whether the fetched integer value or DEFAULT_INT value
     * */
    public static int retrieveInt(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, DEFAUL_INT);
    }

    /**
     * To retrieve a string value from shared preference
     * @param context view context
     * @param key key of the data
     * @return whether the fetched string value or DEFAULT_STRING value
     * */
    public static String retrieveString(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getString(key, DEFAULT_STRING);
    }

    /**
     * To retrieve a boolean value from shared preference
     * @param context view context
     * @param key key of the data
     * @return whether the fetched string value or DEFAULT_STRING value
     * */
    public static boolean retrieveBool(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, DEFAULT_BOOL);
    }
}
