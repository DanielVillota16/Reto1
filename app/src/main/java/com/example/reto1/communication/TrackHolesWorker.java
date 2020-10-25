package com.example.reto1.communication;

import com.example.reto1.activity.MapsActivity;
import com.example.reto1.model.Hole;
import com.example.reto1.model.Position;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class TrackHolesWorker extends Thread{

    public static final long DELAY = 3000;

    private MapsActivity mapsActivity;
    private boolean isAlive;
    public TrackHolesWorker(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
        isAlive = true;
    }

    public void run(){
        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();
        Gson gson = new Gson();
        while(isAlive){
            delay();
            String json = utilDomi.GETrequest("https://reto1-apps-moviles.firebaseio.com/holes.json");
            Type type = new TypeToken<HashMap<String,Hole>>(){}.getType();
            HashMap<String, Hole> holes = gson.fromJson(json, type);
            if(holes==null) continue;
            ArrayList<Hole> holesList = new ArrayList<>();
            holes.forEach((key,value)->{ holesList.add(value); });
            mapsActivity.updateHoleMarkers(holesList);
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
