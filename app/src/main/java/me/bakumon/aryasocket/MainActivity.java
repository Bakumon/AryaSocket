package me.bakumon.aryasocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    public void onScanSuccess(String text) {
        Log.e(TAG, "onScanSuccess: " + text);
    }
}
