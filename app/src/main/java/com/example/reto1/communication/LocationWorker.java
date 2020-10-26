package com.example.reto1.communication;

import com.example.reto1.activity.MapsActivity;
import com.example.reto1.model.User;
import com.google.gson.Gson;

public class LocationWorker extends Thread{

    public static final long DELAY = 3000;

    private MapsActivity mapsActivity;
    private boolean isAlive;

    public LocationWorker(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
        isAlive = true;
    }

    public void run(){
        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();
        Gson gson = new Gson();
        while(isAlive){
            delay();
            String json = gson.toJson(new User(mapsActivity.getUser().getId(), mapsActivity.getCurrPos()));
            utilDomi.PUTrequest("https://reto1-apps-moviles.firebaseio.com/users/"+mapsActivity.getUser().getId()+".json", json);
            mapsActivity.computeDistances();
        }
    }

    public void delay(){
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        isAlive = false;
    }
}
