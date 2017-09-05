package me.bakumon.aryasocket.library;

/**
 * AryaSocket 配置项
 * Created by bakumon on 2017/9/5.
 */

public class AryaConfig {

    private String mSocketURL; // socket 连接地址
    private String mHeartData; // 心跳数据
    private int mPerReconnectInterval; // 重连每次增加的时间间隔
    private int mMaxReconnectInterval;  // 重连的最大时间间隔
    private int mFrameQueueSize; // 帧队列数
    private int mConnectTimeout; // 连接超时时间

    private AryaConfig(AryaConfig.Builder builder) {
        mSocketURL = builder.mSocketURL;
        mHeartData = builder.mHeartData;
        mPerReconnectInterval = builder.mPerReconnectInterval;
        mMaxReconnectInterval = builder.mMaxReconnectInterval;
        mFrameQueueSize = builder.mFrameQueueSize;
        mConnectTimeout = builder.mConnectTimeout;
    }

    String getSocketURL() {
        return mSocketURL;
    }

    String getHeartData() {
        return mHeartData;
    }

    int getPerReconnectInterval() {
        return mPerReconnectInterval;
    }

    int getMaxReconnectInterval() {
        return mMaxReconnectInterval;
    }

    int getFrameQueueSize() {
        return mFrameQueueSize;
    }

    int getConnectTimeout() {
        return mConnectTimeout;
    }

    public static class Builder {
        private String mSocketURL;
        private String mHeartData;
        private int mPerReconnectInterval;
        private int mMaxReconnectInterval;
        private int mFrameQueueSize;
        private int mConnectTimeout;

        public Builder(String socketURL) {
            mSocketURL = socketURL;
        }

        public Builder setHeartData(String heartData) {
            mHeartData = heartData;
            return this;
        }

        public Builder setPerReconnectInterval(int perReconnectInterval) {
            mPerReconnectInterval = perReconnectInterval;
            return this;
        }

        public Builder setMaxReconnectInterval(int maxReconnectInterval) {
            mMaxReconnectInterval = maxReconnectInterval;
            return this;
        }

        public Builder setFrameQueueSize(int frameQueueSize) {
            mFrameQueueSize = frameQueueSize;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            mConnectTimeout = connectTimeout;
            return this;
        }

        public AryaConfig build() {
            return new AryaConfig(this);
        }

    }
}
