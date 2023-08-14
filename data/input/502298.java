public class PowerManager
{
    private static final String TAG = "PowerManager";
    private static final int WAKE_BIT_CPU_STRONG = 1;
    private static final int WAKE_BIT_CPU_WEAK = 2;
    private static final int WAKE_BIT_SCREEN_DIM = 4;
    private static final int WAKE_BIT_SCREEN_BRIGHT = 8;
    private static final int WAKE_BIT_KEYBOARD_BRIGHT = 16;
    private static final int WAKE_BIT_PROXIMITY_SCREEN_OFF = 32;
    private static final int LOCK_MASK = WAKE_BIT_CPU_STRONG
                                        | WAKE_BIT_CPU_WEAK
                                        | WAKE_BIT_SCREEN_DIM
                                        | WAKE_BIT_SCREEN_BRIGHT
                                        | WAKE_BIT_KEYBOARD_BRIGHT
                                        | WAKE_BIT_PROXIMITY_SCREEN_OFF;
    public static final int PARTIAL_WAKE_LOCK = WAKE_BIT_CPU_STRONG;
    public static final int FULL_WAKE_LOCK = WAKE_BIT_CPU_WEAK | WAKE_BIT_SCREEN_BRIGHT 
                                            | WAKE_BIT_KEYBOARD_BRIGHT;
    public static final int SCREEN_BRIGHT_WAKE_LOCK = WAKE_BIT_CPU_WEAK | WAKE_BIT_SCREEN_BRIGHT;
    public static final int SCREEN_DIM_WAKE_LOCK = WAKE_BIT_CPU_WEAK | WAKE_BIT_SCREEN_DIM;
    public static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = WAKE_BIT_PROXIMITY_SCREEN_OFF;
    public static final int WAIT_FOR_PROXIMITY_NEGATIVE = 1;
    public static final int ACQUIRE_CAUSES_WAKEUP = 0x10000000;
    public static final int ON_AFTER_RELEASE = 0x20000000;
    public class WakeLock
    {
        static final int RELEASE_WAKE_LOCK = 1;
        Runnable mReleaser = new Runnable() {
            public void run() {
                release();
            }
        };
        int mFlags;
        String mTag;
        IBinder mToken;
        int mCount = 0;
        boolean mRefCounted = true;
        boolean mHeld = false;
        WakeLock(int flags, String tag)
        {
            switch (flags & LOCK_MASK) {
            case PARTIAL_WAKE_LOCK:
            case SCREEN_DIM_WAKE_LOCK:
            case SCREEN_BRIGHT_WAKE_LOCK:
            case FULL_WAKE_LOCK:
            case PROXIMITY_SCREEN_OFF_WAKE_LOCK:
                break;
            default:
                throw new IllegalArgumentException();
            }
            mFlags = flags;
            mTag = tag;
            mToken = new Binder();
        }
        public void setReferenceCounted(boolean value)
        {
            mRefCounted = value;
        }
        public void acquire()
        {
            synchronized (mToken) {
                if (!mRefCounted || mCount++ == 0) {
                    try {
                        mService.acquireWakeLock(mFlags, mToken, mTag);
                    } catch (RemoteException e) {
                    }
                    mHeld = true;
                }
            }
        }
        public void acquire(long timeout) {
            acquire();
            mHandler.postDelayed(mReleaser, timeout);
        }
        public void release()
        {
            release(0);
        }
        public void release(int flags)
        {
            synchronized (mToken) {
                if (!mRefCounted || --mCount == 0) {
                    try {
                        mService.releaseWakeLock(mToken, flags);
                    } catch (RemoteException e) {
                    }
                    mHeld = false;
                }
                if (mCount < 0) {
                    throw new RuntimeException("WakeLock under-locked " + mTag);
                }
            }
        }
        public boolean isHeld()
        {
            synchronized (mToken) {
                return mHeld;
            }
        }
        public String toString() {
            synchronized (mToken) {
                return "WakeLock{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " held=" + mHeld + ", refCount=" + mCount + "}";
            }
        }
        @Override
        protected void finalize() throws Throwable
        {
            synchronized (mToken) {
                if (mHeld) {
                    Log.wtf(TAG, "WakeLock finalized while still held: " + mTag);
                    try {
                        mService.releaseWakeLock(mToken, 0);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }
    public WakeLock newWakeLock(int flags, String tag)
    {
        if (tag == null) {
            throw new NullPointerException("tag is null in PowerManager.newWakeLock");
        }
        return new WakeLock(flags, tag);
    }
    public void userActivity(long when, boolean noChangeLights)
    {
        try {
            mService.userActivity(when, noChangeLights);
        } catch (RemoteException e) {
        }
    }
    public void goToSleep(long time) 
    {
        try {
            mService.goToSleep(time);
        } catch (RemoteException e) {
        }
    }
    public void setBacklightBrightness(int brightness)
    {
        try {
            mService.setBacklightBrightness(brightness);
        } catch (RemoteException e) {
        }
    }
    public int getSupportedWakeLockFlags()
    {
        try {
            return mService.getSupportedWakeLockFlags();
        } catch (RemoteException e) {
            return 0;
        }
    }
    public boolean isScreenOn()
    {
        try {
            return mService.isScreenOn();
        } catch (RemoteException e) {
            return false;
        }
    }
    public void reboot(String reason)
    {
        try {
            mService.reboot(reason);
        } catch (RemoteException e) {
        }
    }
    private PowerManager()
    {
    }
    public PowerManager(IPowerManager service, Handler handler)
    {
        mService = service;
        mHandler = handler;
    }
    IPowerManager mService;
    Handler mHandler;
}
