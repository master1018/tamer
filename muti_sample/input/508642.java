public class AudioSystem
{
    public static final int STREAM_VOICE_CALL = 0;
    public static final int STREAM_SYSTEM = 1;
    public static final int STREAM_RING = 2;
    public static final int STREAM_MUSIC = 3;
    public static final int STREAM_ALARM = 4;
    public static final int STREAM_NOTIFICATION = 5;
    public static final int STREAM_BLUETOOTH_SCO = 6;
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_DTMF = 8;
    public static final int STREAM_TTS = 9;
    public static final int NUM_STREAMS = 5;
    private static final int NUM_STREAM_TYPES = 10;
    public static final int getNumStreamTypes() { return NUM_STREAM_TYPES; }
    public static native int muteMicrophone(boolean on);
    public static native boolean isMicrophoneMuted();
    public static int setMode(int mode) {
        return AUDIO_STATUS_ERROR;
    }
    public static int getMode() {
        return MODE_INVALID;
    }
    public static final int MODE_INVALID            = -2;
    public static final int MODE_CURRENT            = -1;
    public static final int MODE_NORMAL             = 0;
    public static final int MODE_RINGTONE           = 1;
    public static final int MODE_IN_CALL            = 2;
    public static final int NUM_MODES               = 3;
    @Deprecated public static final int ROUTE_EARPIECE          = (1 << 0);
    @Deprecated public static final int ROUTE_SPEAKER           = (1 << 1);
    @Deprecated public static final int ROUTE_BLUETOOTH = (1 << 2);
    @Deprecated public static final int ROUTE_BLUETOOTH_SCO     = (1 << 2);
    @Deprecated public static final int ROUTE_HEADSET           = (1 << 3);
    @Deprecated public static final int ROUTE_BLUETOOTH_A2DP    = (1 << 4);
    @Deprecated public static final int ROUTE_ALL               = 0xFFFFFFFF;
    public static int setRouting(int mode, int routes, int mask) {
        return AUDIO_STATUS_ERROR;
    }
    public static int getRouting(int mode) {
        return 0;
    }
    public static native boolean isStreamActive(int stream);
    public static native int setParameters(String keyValuePairs);
    public static native String getParameters(String keys);
    public static final int AUDIO_STATUS_OK = 0;
    public static final int AUDIO_STATUS_ERROR = 1;
    public static final int AUDIO_STATUS_SERVER_DIED = 100;
    private static ErrorCallback mErrorCallback;
    public interface ErrorCallback
    {
        void onError(int error);
    };
    public static void setErrorCallback(ErrorCallback cb)
    {
        mErrorCallback = cb;
    }
    private static void errorCallbackFromNative(int error)
    {
        if (mErrorCallback != null) {
            mErrorCallback.onError(error);
        }
    }
    public static final int DEVICE_OUT_EARPIECE = 0x1;
    public static final int DEVICE_OUT_SPEAKER = 0x2;
    public static final int DEVICE_OUT_WIRED_HEADSET = 0x4;
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 0x10;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 0x20;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 0x40;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 0x80;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 0x100;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 0x200;
    public static final int DEVICE_OUT_AUX_DIGITAL = 0x400;
    public static final int DEVICE_OUT_DEFAULT = 0x8000;
    public static final int DEVICE_IN_COMMUNICATION = 0x10000;
    public static final int DEVICE_IN_AMBIENT = 0x20000;
    public static final int DEVICE_IN_BUILTIN_MIC1 = 0x40000;
    public static final int DEVICE_IN_BUILTIN_MIC2 = 0x80000;
    public static final int DEVICE_IN_MIC_ARRAY = 0x100000;
    public static final int DEVICE_IN_BLUETOOTH_SCO_HEADSET = 0x200000;
    public static final int DEVICE_IN_WIRED_HEADSET = 0x400000;
    public static final int DEVICE_IN_AUX_DIGITAL = 0x800000;
    public static final int DEVICE_IN_DEFAULT = 0x80000000;
    public static final int DEVICE_STATE_UNAVAILABLE = 0;
    public static final int DEVICE_STATE_AVAILABLE = 1;
    public static final int PHONE_STATE_OFFCALL = 0;
    public static final int PHONE_STATE_RINGING = 1;
    public static final int PHONE_STATE_INCALL = 2;
    public static final int FORCE_NONE = 0;
    public static final int FORCE_SPEAKER = 1;
    public static final int FORCE_HEADPHONES = 2;
    public static final int FORCE_BT_SCO = 3;
    public static final int FORCE_BT_A2DP = 4;
    public static final int FORCE_WIRED_ACCESSORY = 5;
    public static final int FORCE_BT_CAR_DOCK = 6;
    public static final int FORCE_BT_DESK_DOCK = 7;
    public static final int FORCE_DEFAULT = FORCE_NONE;
    public static final int FOR_COMMUNICATION = 0;
    public static final int FOR_MEDIA = 1;
    public static final int FOR_RECORD = 2;
    public static final int FOR_DOCK = 3;
    public static native int setDeviceConnectionState(int device, int state, String device_address);
    public static native int getDeviceConnectionState(int device, String device_address);
    public static native int setPhoneState(int state);
    public static native int setRingerMode(int mode, int mask);
    public static native int setForceUse(int usage, int config);
    public static native int getForceUse(int usage);
    public static native int initStreamVolume(int stream, int indexMin, int indexMax);
    public static native int setStreamVolumeIndex(int stream, int index);
    public static native int getStreamVolumeIndex(int stream);
}
