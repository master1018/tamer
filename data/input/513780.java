public class LightsService {
    private static final String TAG = "LightsService";
    static final int LIGHT_ID_BACKLIGHT = 0;
    static final int LIGHT_ID_KEYBOARD = 1;
    static final int LIGHT_ID_BUTTONS = 2;
    static final int LIGHT_ID_BATTERY = 3;
    static final int LIGHT_ID_NOTIFICATIONS = 4;
    static final int LIGHT_ID_ATTENTION = 5;
    static final int LIGHT_ID_BLUETOOTH = 6;
    static final int LIGHT_ID_WIFI = 7;
    static final int LIGHT_ID_COUNT = 8;
    static final int LIGHT_FLASH_NONE = 0;
    static final int LIGHT_FLASH_TIMED = 1;
    static final int LIGHT_FLASH_HARDWARE = 2;
    static final int BRIGHTNESS_MODE_USER = 0;
    static final int BRIGHTNESS_MODE_SENSOR = 1;
    private final Light mLights[] = new Light[LIGHT_ID_COUNT];
    public final class Light {
        private Light(int id) {
            mId = id;
        }
        public void setBrightness(int brightness) {
            setBrightness(brightness, BRIGHTNESS_MODE_USER);
        }
        public void setBrightness(int brightness, int brightnessMode) {
            synchronized (this) {
                int color = brightness & 0x000000ff;
                color = 0xff000000 | (color << 16) | (color << 8) | color;
                setLightLocked(color, LIGHT_FLASH_NONE, 0, 0, brightnessMode);
            }
        }
        public void setColor(int color) {
            synchronized (this) {
                setLightLocked(color, LIGHT_FLASH_NONE, 0, 0, 0);
            }
        }
        public void setFlashing(int color, int mode, int onMS, int offMS) {
            synchronized (this) {
                setLightLocked(color, mode, onMS, offMS, BRIGHTNESS_MODE_USER);
            }
        }
        public void pulse() {
            pulse(0x00ffffff, 7);
        }
        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (mColor == 0 && !mFlashing) {
                    setLightLocked(color, LIGHT_FLASH_HARDWARE, onMS, 1000, BRIGHTNESS_MODE_USER);
                    mH.sendMessageDelayed(Message.obtain(mH, 1, this), onMS);
                }
            }
        }
        public void turnOff() {
            synchronized (this) {
                setLightLocked(0, LIGHT_FLASH_NONE, 0, 0, 0);
            }
        }
        private void stopFlashing() {
            synchronized (this) {
                setLightLocked(mColor, LIGHT_FLASH_NONE, 0, 0, BRIGHTNESS_MODE_USER);
            }
        }
        private void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (color != mColor || mode != mMode || onMS != mOnMS || offMS != mOffMS) {
                mColor = color;
                mMode = mode;
                mOnMS = onMS;
                mOffMS = offMS;
                setLight_native(mNativePointer, mId, color, mode, onMS, offMS, brightnessMode);
            }
        }
        private int mId;
        private int mColor;
        private int mMode;
        private int mOnMS;
        private int mOffMS;
        private boolean mFlashing;
    }
    private final IHardwareService.Stub mLegacyFlashlightHack = new IHardwareService.Stub() {
        private static final String FLASHLIGHT_FILE = "/sys/class/leds/spotlight/brightness";
        public boolean getFlashlightEnabled() {
            try {
                FileInputStream fis = new FileInputStream(FLASHLIGHT_FILE);
                int result = fis.read();
                fis.close();
                return (result != '0');
            } catch (Exception e) {
                Slog.e(TAG, "getFlashlightEnabled failed", e);
                return false;
            }
        }
        public void setFlashlightEnabled(boolean on) {
            if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.FLASHLIGHT)
                    != PackageManager.PERMISSION_GRANTED &&
                    mContext.checkCallingOrSelfPermission(android.Manifest.permission.HARDWARE_TEST)
                    != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException("Requires FLASHLIGHT or HARDWARE_TEST permission");
            }
            try {
                FileOutputStream fos = new FileOutputStream(FLASHLIGHT_FILE);
                byte[] bytes = new byte[2];
                bytes[0] = (byte)(on ? '1' : '0');
                bytes[1] = '\n';
                fos.write(bytes);
                fos.close();
            } catch (Exception e) {
                Slog.e(TAG, "setFlashlightEnabled failed", e);
            }
        }
    };
    LightsService(Context context) {
        mNativePointer = init_native();
        mContext = context;
        ServiceManager.addService("hardware", mLegacyFlashlightHack);
        for (int i = 0; i < LIGHT_ID_COUNT; i++) {
            mLights[i] = new Light(i);
        }
    }
    protected void finalize() throws Throwable {
        finalize_native(mNativePointer);
        super.finalize();
    }
    public Light getLight(int id) {
        return mLights[id];
    }
    private Handler mH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Light light = (Light)msg.obj;
            light.stopFlashing();
        }
    };
    private static native int init_native();
    private static native void finalize_native(int ptr);
    private static native void setLight_native(int ptr, int light, int color, int mode,
            int onMS, int offMS, int brightnessMode);
    private final Context mContext;
    private int mNativePointer;
}
