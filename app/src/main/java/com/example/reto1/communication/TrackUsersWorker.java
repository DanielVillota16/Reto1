package com.example.reto1.communication;

import com.example.reto1.activity.MapsActivity;
import com.example.reto1.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class TrackUsersWorker extends Thread{

    public static final long DELAY = 3000;

    private MapsActivity mapsActivity;
    private boolean isAlive;
    public TrackUsersWorker(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
        isAlive = true;
    }

    public void run(){
        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();
        Gson gson = new Gson();
        while(isAlive){
            delay();
            String json = utilDomi.GETrequest("https://reto1-apps-moviles.firebaseio.com/users.json");
            Type type = new TypeToken<HashMap<String, User>>(){}.getType();
            HashMap<String, User> users = gson.fromJson(json, type);
            if(users==null) continue;
            ArrayList<User> usersList = new ArrayList<>();
            users.forEach((key,value)->{ usersList.add(value); });
            mapsActivity.updateUserMarkers(usersList);
        }
    }

    public void finish(){
        isAlive = false;
    }

    public void delay(){
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
