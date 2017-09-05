package me.bakumon.aryasocket.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 监听网络变化
 * 网络可用时重新链接
 * Created by bakumon on 2017/8/16.
 */
class NetWorkReceiver extends BroadcastReceiver {
    private static final String TAG = NetWorkReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

            // 获取网络连接管理器
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取当前网络状态信息
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();

            if (info != null && info.isAvailable()) {
                Log.i(TAG, "网络可用");
                AryaSocket.getInstance().immediateReconnect();
            }

        }
    }
}
