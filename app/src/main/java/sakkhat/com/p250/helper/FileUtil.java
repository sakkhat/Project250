package sakkhat.com.p250.helper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

/**
 * Created by Rafiul Islam on 03-Nov-18.
 */

public class FileUtil {

    /**
     * Selected files from content provider android return an uri of content table.
     * This uri make a file unique to address on an android device. But for stream a file
     * there need the absolute storage location of the selected file.
     *
     * FileUtil class defined some static method to retrieve the real path location from uri data.
     * */


    /**
     * static class to find out the real path of a file from its uri data.
     * @param context context from where this static method called.
     * @param uri file uri address
     * @return real path of the file or null
     * */
    public static String getPath(Context context, Uri uri){
        /**
         * ensure that the uri in a content uri
         * */
        if(uri.getScheme().equalsIgnoreCase("content")) {
            /**
             * for download contents transform the uri to download uri
             * */
            if(uri.getAuthority().equalsIgnoreCase("com.android.providers.downloads.documents")){
                uri = converUri(uri);
            }
            String[] projection = {"_data"};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if(cursor!= null && cursor.getColumnCount() > 0){
                int index = cursor.getColumnIndexOrThrow("_data");
                if(cursor.moveToFirst()){
                    return  cursor.getString(index);
                }
            }
        }
        /**
        * ensure the the uri is a file actual location uri
        * */
        else if(uri.getScheme().equalsIgnoreCase("file")){
            return uri.getPath();
        }

        // return null otherwise
        return null;
    }

    /**
     * convert the uri from media location to download
     * @param uri base uri
     * @return converted uri
     * */
    private static Uri converUri(Uri uri){
        final String docID = DocumentsContract.getDocumentId(uri);
        uri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.valueOf(docID));
        return uri;
    }
}
