package sakkhat.com.p250.structure;

import sakkhat.com.p250.helper.Sizer;

/**
 * Created by Rafiul Islam on 14-Nov-18.
 */

public class DataItem {
    private String type;
    private String name;
    private boolean completed;
    private boolean remoteFile;

    private int sizer;
    private long size;
    private int progress;

    public DataItem(String name, long size, boolean remoteFile){
        this.name = name;
        this.size = size;
        this.remoteFile = remoteFile;

        sizer = Sizer.maxFor(size);

        int x = name.lastIndexOf('.');
        if(x != -1){
            type = name.substring(x);
        }
        else {
            type = "N/A";
        }

        progress = 0;
        completed = false;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setProgress(long bytes) {
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
}
