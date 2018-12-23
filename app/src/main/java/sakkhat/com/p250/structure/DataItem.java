package sakkhat.com.p250.structure;

import java.io.File;

/**
 * Created by Rafiul Islam on 21-Dec-18.
 */

public class DataItem {
    private File file;
    private int index;

    public DataItem(File file, int index){
        this.file = file;
        this.index = index;
    }

    public File getFile() {
        return file;
    }

    public int getIndex() {
        return index;
    }
}
