package main.java;

import java.util.ArrayList;

public class SaveIO {

    public SaveIO() {}

    public static void save(ArrayList<Plate> plates, String saveName) {
        HibernateIO.save(saveName, plates);
    }

    public static void load(ArrayList<Plate> plates, String saveName) {
        String clean = saveName.endsWith(".txt")
                ? saveName.substring(0, saveName.length() - 4)
                : saveName;
        HibernateIO.load(clean, plates);
    }

    public static void newSave(String saveName) {
        HibernateIO.newSave(saveName);
    }

    public static String[] loadSaves() {
        return HibernateIO.getSaveNames();
    }
}