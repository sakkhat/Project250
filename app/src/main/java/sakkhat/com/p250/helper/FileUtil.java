package sakkhat.com.p250.helper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
/**
 * Created by Rafiul Islam on 01-Dec-18.
 */

public class FileUtil {

    public static String getPath(final Context context, final Uri uri){

        if(DocumentsContract.isDocumentUri(context, uri)){
            if(isExternalStorageDoc(uri)){
                final String docID = DocumentsContract.getDocumentId(uri);
                final String[] split = docID.split(":");
                final String type = split[0];

                if(type.equalsIgnoreCase("primary")){
                    return Environment.getExternalStorageDirectory()+"/"+split[1];
                }
            }
            else if(isDownloadDoc(uri)){
                final String docID = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docID));

                return getDataColumn(context, contentUri, null, null);
            }
            else if(isMediaDoc(uri)){
                final String docID = DocumentsContract.getDocumentId(uri);
                final String[] split = docID.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if(type.equalsIgnoreCase("image")){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if(type.equalsIgnoreCase("video")){
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if(type.equalsIgnoreCase("audio")){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                // return getDataCol(context, contentUri, selection, selectionArgs)
                return getDataColumn(context, uri, selection, selectionArgs);
            }
        }
        else if(uri.getScheme().equalsIgnoreCase("content")){
            if(isGooglePhotosUri(uri)){
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }

        else if(uri.getScheme().equalsIgnoreCase("file")){
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selction, String[] selectionArgs){
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection  = { column };

        try{
            cursor = context.getContentResolver().query(uri, projection,selction,selectionArgs,null);
            if(cursor != null && cursor.getColumnCount() > 0){
                final int index = cursor.getColumnIndexOrThrow(column);
                if(cursor.moveToFirst()){
                    return cursor.getString(index);
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return null;
    }
    private static boolean isExternalStorageDoc(Uri uri){
        return uri.getAuthority().equals("com.android.externalstorage.documents");
    }
    private static boolean isDownloadDoc(Uri uri){
        return uri.getAuthority().equals("com.android.providers.downloads.documents");
    }
    private static boolean isMediaDoc(Uri uri){
        return uri.getAuthority().equals("com.android.providers.media.documents");
    }
    private static boolean isGooglePhotosUri(Uri uri){
        return uri.getAuthority().equals("com.google.android.apps.photos.content");
    }
}
