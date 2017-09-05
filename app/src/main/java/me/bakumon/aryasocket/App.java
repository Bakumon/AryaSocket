package me.bakumon.aryasocket;

import android.app.Application;

import me.bakumon.aryasocket.library.AryaConfig;
import me.bakumon.aryasocket.library.AryaSocket;

/**
 * 测试
 * Created by bakumon on 17-9-3.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        String heartbeat = "";

        AryaConfig aryaConfig = new AryaConfig.Builder("")
                .setHeartData(heartbeat).build();

        AryaSocket.getInstance().init(this, aryaConfig);
    }
}
