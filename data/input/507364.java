public class MediaPlayer
{
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final boolean METADATA_ALL = false;
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    private final static String TAG = "MediaPlayer";
    private final static String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private int mNativeContext; 
    private int mListenerContext; 
    private Surface mSurface; 
    private SurfaceHolder  mSurfaceHolder;
    private EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    public MediaPlayer() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
        native_setup(new WeakReference<MediaPlayer>(this));
    }
    private native void _setVideoSurface();
    public Parcel newRequest() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(IMEDIA_PLAYER);
        return parcel;
    }
    public int invoke(Parcel request, Parcel reply) {
        int retcode = native_invoke(request, reply);
        reply.setDataPosition(0);
        return retcode;
    }
    public void setDisplay(SurfaceHolder sh) {
        mSurfaceHolder = sh;
        if (sh != null) {
            mSurface = sh.getSurface();
        } else {
            mSurface = null;
        }
        _setVideoSurface();
        updateSurfaceScreenOn();
    }
    public static MediaPlayer create(Context context, Uri uri) {
        return create (context, uri, null);
    }
    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(context, uri);
            if (holder != null) {
                mp.setDisplay(holder);
            }
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
        }
        return null;
    }
    public static MediaPlayer create(Context context, int resid) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) return null;
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
        }
        return null;
    }
    public void setDataSource(Context context, Uri uri)
        throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, null);
    }
    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
        throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String scheme = uri.getScheme();
        if(scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            fd = resolver.openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return;
            }
            if (fd.getDeclaredLength() < 0) {
                setDataSource(fd.getFileDescriptor());
            } else {
                setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
            }
            return;
        } catch (SecurityException ex) {
        } catch (IOException ex) {
        } finally {
            if (fd != null) {
                fd.close();
            }
        }
        Log.d(TAG, "Couldn't open file on client side, trying server side");
        setDataSource(uri.toString(), headers);
        return;
    }
    public native void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException;
    public native void setDataSource(String path,  Map<String, String> headers)
            throws IOException, IllegalArgumentException, IllegalStateException;
    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }
    public native void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException;
    public native void prepare() throws IOException, IllegalStateException;
    public native void prepareAsync() throws IllegalStateException;
    public  void start() throws IllegalStateException {
        stayAwake(true);
        _start();
    }
    private native void _start() throws IllegalStateException;
    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
    }
    private native void _stop() throws IllegalStateException;
    public void pause() throws IllegalStateException {
        stayAwake(false);
        _pause();
    }
    private native void _pause() throws IllegalStateException;
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode|PowerManager.ON_AFTER_RELEASE, MediaPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }
    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }
    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }
    public native int getVideoWidth();
    public native int getVideoHeight();
    public native boolean isPlaying();
    public native void seekTo(int msec) throws IllegalStateException;
    public native int getCurrentPosition();
    public native int getDuration();
    public Metadata getMetadata(final boolean update_only,
                                final boolean apply_filter) {
        Parcel reply = Parcel.obtain();
        Metadata data = new Metadata();
        if (!native_getMetadata(update_only, apply_filter, reply)) {
            reply.recycle();
            return null;
        }
        if (!data.parse(reply)) {
            reply.recycle();
            return null;
        }
        return data;
    }
    public int setMetadataFilter(Set<Integer> allow, Set<Integer> block) {
        Parcel request =  newRequest();
        int capacity = request.dataSize() + 4 * (1 + allow.size() + 1 + block.size());
        if (request.dataCapacity() < capacity) {
            request.setDataCapacity(capacity);
        }
        request.writeInt(allow.size());
        for(Integer t: allow) {
            request.writeInt(t);
        }
        request.writeInt(block.size());
        for(Integer t: block) {
            request.writeInt(t);
        }
        return native_setMetadataFilter(request);
    }
    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mOnVideoSizeChangedListener = null;
        _release();
    }
    private native void _release();
    public void reset() {
        stayAwake(false);
        _reset();
        mEventHandler.removeCallbacksAndMessages(null);
    }
    private native void _reset();
    public boolean suspend() {
        if (native_suspend_resume(true) < 0) {
            return false;
        }
        stayAwake(false);
        mEventHandler.removeCallbacksAndMessages(null);
        return true;
    }
    public boolean resume() {
        if (native_suspend_resume(false) < 0) {
            return false;
        }
        if (isPlaying()) {
            stayAwake(true);
        }
        return true;
    }
    private native int native_suspend_resume(boolean isSuspend);
    public native void setAudioStreamType(int streamtype);
    public native void setLooping(boolean looping);
    public native boolean isLooping();
    public native void setVolume(float leftVolume, float rightVolume);
    public native Bitmap getFrameAt(int msec) throws IllegalStateException;
    private native final int native_invoke(Parcel request, Parcel reply);
    private native final boolean native_getMetadata(boolean update_only,
                                                    boolean apply_filter,
                                                    Parcel reply);
    private native final int native_setMetadataFilter(Parcel request);
    private static native final void native_init();
    private native final void native_setup(Object mediaplayer_this);
    private native final void native_finalize();
    @Override
    protected void finalize() { native_finalize(); }
    private static final int MEDIA_NOP = 0; 
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;
    private class EventHandler extends Handler
    {
        private MediaPlayer mMediaPlayer;
        public EventHandler(MediaPlayer mp, Looper looper) {
            super(looper);
            mMediaPlayer = mp;
        }
        @Override
        public void handleMessage(Message msg) {
            if (mMediaPlayer.mNativeContext == 0) {
                Log.w(TAG, "mediaplayer went away with unhandled events");
                return;
            }
            switch(msg.what) {
            case MEDIA_PREPARED:
                if (mOnPreparedListener != null)
                    mOnPreparedListener.onPrepared(mMediaPlayer);
                return;
            case MEDIA_PLAYBACK_COMPLETE:
                if (mOnCompletionListener != null)
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                stayAwake(false);
                return;
            case MEDIA_BUFFERING_UPDATE:
                if (mOnBufferingUpdateListener != null)
                    mOnBufferingUpdateListener.onBufferingUpdate(mMediaPlayer, msg.arg1);
                return;
            case MEDIA_SEEK_COMPLETE:
              if (mOnSeekCompleteListener != null)
                  mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
              return;
            case MEDIA_SET_VIDEO_SIZE:
              if (mOnVideoSizeChangedListener != null)
                  mOnVideoSizeChangedListener.onVideoSizeChanged(mMediaPlayer, msg.arg1, msg.arg2);
              return;
            case MEDIA_ERROR:
                Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                boolean error_was_handled = false;
                if (mOnErrorListener != null) {
                    error_was_handled = mOnErrorListener.onError(mMediaPlayer, msg.arg1, msg.arg2);
                }
                if (mOnCompletionListener != null && ! error_was_handled) {
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                }
                stayAwake(false);
                return;
            case MEDIA_INFO:
                Log.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                if (mOnInfoListener != null) {
                    mOnInfoListener.onInfo(mMediaPlayer, msg.arg1, msg.arg2);
                }
                return;
            case MEDIA_NOP: 
                break;
            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }
    private static void postEventFromNative(Object mediaplayer_ref,
                                            int what, int arg1, int arg2, Object obj)
    {
        MediaPlayer mp = (MediaPlayer)((WeakReference)mediaplayer_ref).get();
        if (mp == null) {
            return;
        }
        if (mp.mEventHandler != null) {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }
    public interface OnPreparedListener
    {
        void onPrepared(MediaPlayer mp);
    }
    public void setOnPreparedListener(OnPreparedListener listener)
    {
        mOnPreparedListener = listener;
    }
    private OnPreparedListener mOnPreparedListener;
    public interface OnCompletionListener
    {
        void onCompletion(MediaPlayer mp);
    }
    public void setOnCompletionListener(OnCompletionListener listener)
    {
        mOnCompletionListener = listener;
    }
    private OnCompletionListener mOnCompletionListener;
    public interface OnBufferingUpdateListener
    {
        void onBufferingUpdate(MediaPlayer mp, int percent);
    }
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener)
    {
        mOnBufferingUpdateListener = listener;
    }
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    public interface OnSeekCompleteListener
    {
        public void onSeekComplete(MediaPlayer mp);
    }
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener)
    {
        mOnSeekCompleteListener = listener;
    }
    private OnSeekCompleteListener mOnSeekCompleteListener;
    public interface OnVideoSizeChangedListener
    {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height);
    }
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener)
    {
        mOnVideoSizeChangedListener = listener;
    }
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public interface OnErrorListener
    {
        boolean onError(MediaPlayer mp, int what, int extra);
    }
    public void setOnErrorListener(OnErrorListener listener)
    {
        mOnErrorListener = listener;
    }
    private OnErrorListener mOnErrorListener;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public interface OnInfoListener
    {
        boolean onInfo(MediaPlayer mp, int what, int extra);
    }
    public void setOnInfoListener(OnInfoListener listener)
    {
        mOnInfoListener = listener;
    }
    private OnInfoListener mOnInfoListener;
    public native static int snoop(short [] outData, int kind);
}
