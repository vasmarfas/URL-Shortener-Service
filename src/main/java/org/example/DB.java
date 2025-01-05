package org.example;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DB {
    private static final String dbFileName = "db.json";
    private List<String> userDB = new ArrayList<>();
    private List<UserLink> linkDB = new ArrayList<>();


    public void addLink(UserLink userLink) {
        for (UserLink link : linkDB){
            if (Objects.equals(link, userLink)) return;
        }
        linkDB.add(userLink);
        saveToFile();
    }
    public void updateLink(UserLink userLink) {
        for (UserLink link : linkDB){
            if (Objects.equals(link.id, userLink.id)) {
                int indexToReplace = linkDB.indexOf(link);
                linkDB.set(indexToReplace, userLink);
            }
        }
        saveToFile();
    }
    public UserLink getLinkByID(String linkIDString) {
        for (UserLink link : linkDB){
            if (Objects.equals(link.id, linkIDString)) return link;
        }
        return null;
    }
    public boolean removeLinkByID(String linkIDString) {
        for (UserLink link : linkDB){
            if (Objects.equals(link.id, linkIDString)) {
                linkDB.remove(link);
                saveToFile();
                return true;
            }
        }
        saveToFile();
        return false;
    }
    public boolean checkLinkAvailability(String linkIDString) {
        for (UserLink link : linkDB){
            if (Objects.equals(link.id, linkIDString)) return false;
        }
        return true;
    }
    public boolean addUser(String uuid) {
        for (String user : userDB){
            if (Objects.equals(user, uuid)) return false;
        }
        userDB.add(uuid);
        saveToFile();
        return true;
    }
    public boolean authUser(String uuid) {
        for (String user : userDB){
            if (Objects.equals(user, uuid)) return true;
        }
        saveToFile();
        return false;
    }


    public ArrayList<UserLink> getAllUserLinks(String uuid) {
        ArrayList<UserLink> outList = new ArrayList<>();
        for (UserLink link : linkDB){
            if (Objects.equals(link.creatorUserUUID, uuid)) {
                outList.add(link);
            }
        }
        return outList;
    }

    public void saveToFile() {
        try (Writer writer = new FileWriter(dbFileName)) {
            Gson gson = new Gson();
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Загрузка данных из JSON файла
    private static DB loadFromFile() {

        try {
            Reader reader = new FileReader(dbFileName);
            Gson gson = new Gson();
            return gson.fromJson(reader, DB.class);
        } catch (FileNotFoundException e) {
            return new DB();
        }
        catch (Exception e) {
            e.printStackTrace();
            return new DB();
        }
    }

    public static DB createDB(){
        return loadFromFile();
    }

}
