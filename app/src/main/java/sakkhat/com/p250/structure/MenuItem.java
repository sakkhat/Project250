package sakkhat.com.p250.structure;

/**
 * Created by hp on 29-Sep-18.
 */

public class MenuItem {
    private String name;
    private int id;

    public MenuItem(String name, int id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
