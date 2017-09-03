package me.bakumon.aryasocket.library;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AryaSocket
 * Created by bakumon on 2017/9/3.
 */

public class AryaSocket {
    private WeakReference<Context> mContext;
    private static AryaSocket mInstance;
    private static final String TAG = AryaSocket.class.getSimpleName();

    private static final int PER_RECONNECT_INTERVAL = 1000; // 重连每次增加的时间间隔
    private static final int MAX_RECONNECT_INTERVAL = 60000; // 重连的最大时间间隔

    private static final int FRAME_QUEUE_SIZE = 5;
    private static final int CONNECT_TIMEOUT = 5000;
    private String url = "http://dev.banyar.cn:9443/";

    private Status mStatus;
    private WebSocket ws;
    private Handler handler;

    private AryaSocket() {
    }

    public static AryaSocket getInstance() {
        if (mInstance == null) {
            synchronized (AryaSocket.class) {
                if (mInstance == null) {
                    mInstance = new AryaSocket();
                }
            }
        }
        return mInstance;
    }

    public void init(Application application) {
        mContext = new WeakReference<>(application.getApplicationContext());
        // 应用位于前后台监听
        ForegroundCallbacks.init(application).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                AryaSocket.getInstance().immediateReconnect();
                Log.i(TAG, "onBecameForeground: ");
            }

            @Override
            public void onBecameBackground() {
                Log.i(TAG, "onBecameBackground: ");
            }
        });
        // 开屏监听
        new ScreenListener(application).begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                AryaSocket.getInstance().immediateReconnect();
                Log.i(TAG, "onScreenOn: ");
            }

            @Override
            public void onScreenOff() {
                Log.i(TAG, "onScreenOff: ");
            }

            @Override
            public void onUserPresent() {
                Log.i(TAG, "onUserPresent: ");
            }
        });
        // 动态注册网络状态改变的广播接受者
        NetWorkReceiver netWorkReceiver = new NetWorkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        application.registerReceiver(netWorkReceiver, filter);
        init();
    }

    private void init() {
        try {
            ws = new WebSocketFactory().createSocket(url, CONNECT_TIMEOUT)
                    .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(new WsListener())//添加回调监听
                    .connectAsynchronously();//异步连接
            mStatus = Status.CONNECTING;
            Log.i(TAG, "init: 第一次连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 继承默认的监听空实现WebSocketAdapter,重写我们需要的方法
     * onTextMessage 收到文字信息
     * onConnected 连接成功
     * onConnectError 连接失败
     * onDisconnected 连接关闭
     */
    private class WsListener extends WebSocketAdapter {
        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
            Log.i(TAG, "onTextMessage: " + text);
            disposeTextMessage(text);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                throws Exception {
            super.onConnected(websocket, headers);
            Log.i(TAG, "onConnected: 连接成功");
            mHandler.post(mSendHeart);
            mStatus = Status.CONNECT_SUCCESS;
            cancelReconnect();//连接成功的时候取消重连,初始化连接次数
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception)
                throws Exception {
            super.onConnectError(websocket, exception);
            Log.i(TAG, "onConnectError: 连接错误");
            mHandler.removeCallbacks(mSendHeart);
            mStatus = Status.CONNECT_FAIL;
            reconnect();//连接错误的时候调用重连方法
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
                throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Log.i(TAG, "onDisconnected: 断开连接");
            mHandler.removeCallbacks(mSendHeart);
            mStatus = Status.CONNECT_FAIL;
            reconnect();//连接断开的时候调用重连方法
        }
    }

    private void disposeTextMessage(final String text) {
        if (callBacks != null) {
            for (int i = 0; i < callBacks.size(); i++) {
                final AryaSocketListener receiver = callBacks.get(i);
                if (receiver != null && handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            receiver.onScanSuccess(text);
                        }
                    });
                }
            }
        }
    }

    private void send(String msg) {
        if (ws != null && ws.isOpen()) {
            Log.i(TAG, "send: " + msg);
            ws.sendText(msg);
        } else {
            init();
        }
    }

    public void disconnect() {
        if (ws != null) {
            ws.disconnect();
        }
    }

    private Handler mHandler = new Handler();

    private int reconnectCount = 0;//重连次数

    // 立刻重连，防止重连次数过多后，再进行连接会等到时间间隔后才重连
    void immediateReconnect() {
        if (reconnectCount > 3) {
            reconnectCount = 0;
            mStatus = Status.CONNECT_FAIL;
        }
        reconnect();
    }

    private void reconnect() {
        if (!isNetConnect()) {
            reconnectCount = 0;
            Log.i(TAG, "reconnect: 重连失败网络不可用");
            return;
        }
        // 当前连接断开了
        // 不是正在重连状态
        if (ws != null && !ws.isOpen() && mStatus != Status.CONNECTING) {

            reconnectCount++;
            mStatus = Status.CONNECTING;

            long reconnectTime = 0;
            if (reconnectCount > 1) {
                long temp = PER_RECONNECT_INTERVAL * (reconnectCount - 1);
                reconnectTime = temp > MAX_RECONNECT_INTERVAL ? MAX_RECONNECT_INTERVAL : temp;
            }

            mHandler.postDelayed(mReconnectTask, reconnectTime);
            Log.i(TAG, "reconnect: " + "准备开始第" + reconnectCount + "次重连,下次" + reconnectTime + "ms后重连 -- webSocketurl:" + url);
        }
    }

    private Runnable mReconnectTask = new Runnable() {

        @Override
        public void run() {
            try {
                ws = new WebSocketFactory().createSocket(url, CONNECT_TIMEOUT)
                        .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                        .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                        .addListener(new WsListener())//添加回调监听
                        .connectAsynchronously();//异步连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable mSendHeart = new Runnable() {
        @Override
        public void run() {
            sendHeart();
            mHandler.postDelayed(mSendHeart, 20000);
        }
    };

    private void sendHeart() {
//        String oid = BanyarSPUtil.getInstance().getOid();
//        if (TextUtils.isEmpty(oid)) {
//            oid = "0";
//        }
//
//        SendHeartParam param = new SendHeartParam();
//        param.inname = "heartbeat";
//
//        SendHeartParam.Data data = new SendHeartParam.Data();
//        data.uid = BanyarSPUtil.getInstance().getUid();
//        data.type = "1";
//        data.city = BanyarSPUtil.getInstance().getCity();
//        data.sys = Constants.sys;
//        data.sysver = Constants.sysver;
//        data.ver = Constants.ver;
//        data.devid = Constants.devid;
//        data.platform = Constants.platform;
//        data.terminal = Constants.terminal;
//        data.oid = oid;
//        data.secret = Constants.secret;
//
//        param.data = data;
//
//        String paramString = new Gson().toJson(param);
//
//        send(paramString);
    }

    private void cancelReconnect() {
        reconnectCount = 0;
        mHandler.removeCallbacks(mReconnectTask);
    }

    private boolean isNetConnect() {
        if (mContext == null || mContext.get() == null) {
            return true;
        }
        ConnectivityManager connectivity = (ConnectivityManager) mContext.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    private List<AryaSocketListener> callBacks = new ArrayList<>();

    public void registerListener(AryaSocketListener receiver) {
        if (handler == null) {
            handler = new Handler();
        }
        if (callBacks == null) {
            callBacks = new ArrayList<>();
        }
        callBacks.add(receiver);
    }

    public void unRegisterListener(AryaSocketListener receiver) {
        if (callBacks != null) {
            callBacks.remove(receiver);
        }
    }

    private enum Status {
        CONNECT_SUCCESS,//连接成功
        CONNECT_FAIL,//连接失败
        CONNECTING //正在连接
    }
}
