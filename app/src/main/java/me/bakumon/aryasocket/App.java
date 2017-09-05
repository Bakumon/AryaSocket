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

        String hear = "{" +
                "\"inname\":\"heartbeat\",\"data\":{" +
                "\"uid\":" + "\"" + "0" + "\"" + "," +
                "\"sys\":" + "\"" + "Android" + "\"" + "," +
                "\"sysver\":" + "\"" + "unkown" + "\"" + "," +
                "\"ver\":" + "\"" + "1.0.0" + "\"" + "," +
                "\"devid\":" + "\"" + "unkown" + "\"" + "," +
                "\"platform\":" + "\"" + "unkown" + "\"" + "," +
                "\"terminal\":" + "\"Cheji\"" + "," +
                "\"city\":" + "\"" + "北京" + "\"" + "," +
                "\"oid\":" + "\"" + "0" + "\"" + "," +
                "\"secret\":" + "\"" + "3ccd8bfc139d10658c2ac38823fae960" + "\"" + "," +
                "\"type\": 1" +
                "}" +
                "}";

        AryaConfig aryaConfig = new AryaConfig.Builder("http://dev.banyar.cn:9443/")
                .setHeartData(hear).build();

        AryaSocket.getInstance().init(this, aryaConfig);
    }
}
