package sakkhat.com.p250.structure;

import sakkhat.com.p250.helper.Sizer;

/**
 * Created by Rafiul Islam on 14-Nov-18.
 */

public class IOItem {

    private String name;
    private boolean completed;
    private boolean remoteFile;

    private int sizer;
    private long size;
    private int progress;

    public IOItem(String name, long size, boolean remoteFile){
        this.name = name;
        this.size = size;
        this.remoteFile = remoteFile;

        sizer = Sizer.maxFor(size);


        progress = 0;
        completed = false;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setProgress(long bytes) {
        if(!remoteFile){
            bytes = size - bytes;
        }
        if(bytes == size){
            completed = true;
        }
        else{
            progress = (int)bytes/sizer;
        }
    }

    public int getMax(){
        return sizer;
    }

    public int getProgress(){
        return progress;
    }

    public long getSize(){
        return size;
    }

    public boolean isRemoteFile(){
        return remoteFile;
    }
}
