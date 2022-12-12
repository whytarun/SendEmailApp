package net.simplifiedcoding.myemailsender;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static MainApplication singleton;
     static Context context;
    public MainApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        MainApplication.context =getApplicationContext();
    }

}
