package me.bakumon.aryasocket;

import android.app.Application;

import me.bakumon.aryasocket.library.AryaSocket;

/**
 * Created by bakumon on 17-9-3.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AryaSocket.getInstance().init(this);
    }
}
