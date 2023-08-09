public class SoundPool
{
    static { System.loadLibrary("soundpool"); }
    private final static String TAG = "SoundPool";
    private final static boolean DEBUG = false;
    private int mNativeContext; 
    private EventHandler mEventHandler;
    private OnLoadCompleteListener mOnLoadCompleteListener;
    private final Object mLock;
    private static final int SAMPLE_LOADED = 1;
    public SoundPool(int maxStreams, int streamType, int srcQuality) {
        if (native_setup(new WeakReference(this), maxStreams, streamType, srcQuality) != 0) {
            throw new RuntimeException("Native setup failed");
        }
        mLock = new Object();
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
    }
    public int load(String path, int priority)
    {
        if (path.startsWith("http:"))
            return _load(path, priority);
        int id = 0;
        try {
            File f = new File(path);
            if (f != null) {
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
                if (fd != null) {
                    id = _load(fd.getFileDescriptor(), 0, f.length(), priority);
                    fd.close();
                }
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "error loading " + path);
        }
        return id;
    }
    public int load(Context context, int resId, int priority) {
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
        int id = 0;
        if (afd != null) {
            id = _load(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), priority);
            try {
                afd.close();
            } catch (java.io.IOException ex) {
            }
        }
        return id;
    }
    public int load(AssetFileDescriptor afd, int priority) {
        if (afd != null) {
            long len = afd.getLength();
            if (len < 0) {
                throw new AndroidRuntimeException("no length for fd");
            }
            return _load(afd.getFileDescriptor(), afd.getStartOffset(), len, priority);
        } else {
            return 0;
        }
    }
    public int load(FileDescriptor fd, long offset, long length, int priority) {
        return _load(fd, offset, length, priority);
    }
    private native final int _load(String uri, int priority);
    private native final int _load(FileDescriptor fd, long offset, long length, int priority);
    public native final boolean unload(int soundID);
    public native final int play(int soundID, float leftVolume, float rightVolume,
            int priority, int loop, float rate);
    public native final void pause(int streamID);
    public native final void resume(int streamID);
    public native final void autoPause();
    public native final void autoResume();
    public native final void stop(int streamID);
    public native final void setVolume(int streamID,
            float leftVolume, float rightVolume);
    public native final void setPriority(int streamID, int priority);
    public native final void setLoop(int streamID, int loop);
    public native final void setRate(int streamID, float rate);
    public interface OnLoadCompleteListener
    {
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status);
    }
    public void setOnLoadCompleteListener(OnLoadCompleteListener listener)
    {
        synchronized(mLock) {
            mOnLoadCompleteListener = listener;
        }
    }
    private class EventHandler extends Handler
    {
        private SoundPool mSoundPool;
        public EventHandler(SoundPool soundPool, Looper looper) {
            super(looper);
            mSoundPool = soundPool;
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case SAMPLE_LOADED:
                if (DEBUG) Log.d(TAG, "Sample " + msg.arg1 + " loaded");
                synchronized(mLock) {
                    if (mOnLoadCompleteListener != null) {
                        mOnLoadCompleteListener.onLoadComplete(mSoundPool, msg.arg1, msg.arg2);
                    }
                }
                break;
            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }
    private static void postEventFromNative(Object weakRef, int msg, int arg1, int arg2, Object obj)
    {
        SoundPool soundPool = (SoundPool)((WeakReference)weakRef).get();
        if (soundPool == null)
            return;
        if (soundPool.mEventHandler != null) {
            Message m = soundPool.mEventHandler.obtainMessage(msg, arg1, arg2, obj);
            soundPool.mEventHandler.sendMessage(m);
        }
    }
    public native final void release();
    private native final int native_setup(Object weakRef, int maxStreams, int streamType, int srcQuality);
    protected void finalize() { release(); }
}
