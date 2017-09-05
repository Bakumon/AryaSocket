package me.bakumon.aryasocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import me.bakumon.aryasocket.library.AryaSocket;
import me.bakumon.aryasocket.library.AryaSocketListener;

public class MainActivity extends AppCompatActivity implements AryaSocketListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AryaSocket.getInstance().registerListener(this);
    }

    @Override
    public void disposeTextMessage(String text) {
        Log.e(TAG, "disposeTextMessage: " + text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AryaSocket.getInstance().unRegisterListener(this);
    }
}
