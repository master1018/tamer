public class JetPlayer
{    
    private static int MAXTRACKS = 32;
    private static final int JET_EVENT                   = 1;
    private static final int JET_USERID_UPDATE           = 2;
    private static final int JET_NUMQUEUEDSEGMENT_UPDATE = 3;
    private static final int JET_PAUSE_UPDATE            = 4;
    private static final int JET_EVENT_VAL_MASK    = 0x0000007f; 
    private static final int JET_EVENT_CTRL_MASK   = 0x00003f80; 
    private static final int JET_EVENT_CHAN_MASK   = 0x0003c000; 
    private static final int JET_EVENT_TRACK_MASK  = 0x00fc0000; 
    private static final int JET_EVENT_SEG_MASK    = 0xff000000; 
    private static final int JET_EVENT_CTRL_SHIFT  = 7;  
    private static final int JET_EVENT_CHAN_SHIFT  = 14; 
    private static final int JET_EVENT_TRACK_SHIFT = 18; 
    private static final int JET_EVENT_SEG_SHIFT   = 24; 
    private static final int JET_OUTPUT_RATE = 22050; 
    private static final int JET_OUTPUT_CHANNEL_CONFIG =
            AudioFormat.CHANNEL_OUT_STEREO; 
    private NativeEventHandler mEventHandler = null;
    private Looper mInitializationLooper = null;
    private final Object mEventListenerLock = new Object();
    private OnJetEventListener mJetEventListener = null;
    private static JetPlayer singletonRef;
    @SuppressWarnings("unused")
    private int mNativePlayerInJavaObj;
    public static JetPlayer getJetPlayer() {
        if (singletonRef == null) {
            singletonRef = new JetPlayer();
        }
        return singletonRef;
    }
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();    
    }
    private JetPlayer() {
        if ((mInitializationLooper = Looper.myLooper()) == null) {
            mInitializationLooper = Looper.getMainLooper();
        }
        int buffSizeInBytes = AudioTrack.getMinBufferSize(JET_OUTPUT_RATE,
                JET_OUTPUT_CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT);
        if ((buffSizeInBytes != AudioTrack.ERROR) 
                && (buffSizeInBytes != AudioTrack.ERROR_BAD_VALUE)) {
            native_setup(new WeakReference<JetPlayer>(this),
                    JetPlayer.getMaxTracks(),
                    Math.max(1200, buffSizeInBytes / 4));
        }
    }
    protected void finalize() { 
        native_finalize(); 
    }
    public void release() {
        native_release();
        singletonRef = null;
    }
    public static int getMaxTracks() {
        return JetPlayer.MAXTRACKS;
    }
    public boolean loadJetFile(String path) {
        return native_loadJetFromFile(path);
    }
    public boolean loadJetFile(AssetFileDescriptor afd) {
        long len = afd.getLength();
        if (len < 0) {
            throw new AndroidRuntimeException("no length for fd");
        }
        return native_loadJetFromFileD(
                afd.getFileDescriptor(), afd.getStartOffset(), len);
    }
    public boolean closeJetFile() {
        return native_closeJetFile();
    }
    public boolean play() {
        return native_playJet();
    }
    public boolean pause() {
        return native_pauseJet();
    }
    public boolean queueJetSegment(int segmentNum, int libNum, int repeatCount,
        int transpose, int muteFlags, byte userID) {
        return native_queueJetSegment(segmentNum, libNum, repeatCount, 
                transpose, muteFlags, userID);
    }
    public boolean queueJetSegmentMuteArray(int segmentNum, int libNum, int repeatCount,
            int transpose, boolean[] muteArray, byte userID) {
        if (muteArray.length != JetPlayer.getMaxTracks()) {
            return false;
        }
        return native_queueJetSegmentMuteArray(segmentNum, libNum, repeatCount,
                transpose, muteArray, userID);
    }
    public boolean setMuteFlags(int muteFlags, boolean sync) {
        return native_setMuteFlags(muteFlags, sync);
    }
    public boolean setMuteArray(boolean[] muteArray, boolean sync) {
        if(muteArray.length != JetPlayer.getMaxTracks())
            return false;
        return native_setMuteArray(muteArray, sync);
    }
    public boolean setMuteFlag(int trackId, boolean muteFlag, boolean sync) {
        return native_setMuteFlag(trackId, muteFlag, sync);
    }
    public boolean triggerClip(int clipId) {
        return native_triggerClip(clipId);
    }
    public boolean clearQueue() {
        return native_clearQueue();
    }
    private class NativeEventHandler extends Handler
    {
        private JetPlayer mJet;
        public NativeEventHandler(JetPlayer jet, Looper looper) {
            super(looper);
            mJet = jet;
        }
        @Override
        public void handleMessage(Message msg) {
            OnJetEventListener listener = null;
            synchronized (mEventListenerLock) {
                listener = mJet.mJetEventListener;
            }
            switch(msg.what) {
            case JET_EVENT:
                if (listener != null) {
                    mJetEventListener.onJetEvent(
                            mJet,
                            (short)((msg.arg1 & JET_EVENT_SEG_MASK)   >> JET_EVENT_SEG_SHIFT),
                            (byte) ((msg.arg1 & JET_EVENT_TRACK_MASK) >> JET_EVENT_TRACK_SHIFT),
                            (byte)(((msg.arg1 & JET_EVENT_CHAN_MASK)  >> JET_EVENT_CHAN_SHIFT) + 1),
                            (byte) ((msg.arg1 & JET_EVENT_CTRL_MASK)  >> JET_EVENT_CTRL_SHIFT),
                            (byte)  (msg.arg1 & JET_EVENT_VAL_MASK) );
                }
                return;
            case JET_USERID_UPDATE:
                if (listener != null) {
                    listener.onJetUserIdUpdate(mJet, msg.arg1, msg.arg2);
                }
                return;
            case JET_NUMQUEUEDSEGMENT_UPDATE:
                if (listener != null) {
                    listener.onJetNumQueuedSegmentUpdate(mJet, msg.arg1);
                }
                return;
            case JET_PAUSE_UPDATE:
                if (listener != null)
                    listener.onJetPauseUpdate(mJet, msg.arg1);
                return;
            default:
                loge("Unknown message type " + msg.what);
                return;
            }
        }
    }
    public void setEventListener(OnJetEventListener listener) {
        setEventListener(listener, null);
    }
    public void setEventListener(OnJetEventListener listener, Handler handler) {
        synchronized(mEventListenerLock) {
            mJetEventListener = listener;
            if (listener != null) {
                if (handler != null) {
                    mEventHandler = new NativeEventHandler(this, handler.getLooper());
                } else {
                    mEventHandler = new NativeEventHandler(this, mInitializationLooper);
                }
            } else {
                mEventHandler = null;
            }
        }
    }
    public interface OnJetEventListener {
        void onJetEvent(JetPlayer player,
                short segment, byte track, byte channel, byte controller, byte value);
        void onJetUserIdUpdate(JetPlayer player, int userId, int repeatCount);
        void onJetNumQueuedSegmentUpdate(JetPlayer player, int nbSegments);
        void onJetPauseUpdate(JetPlayer player, int paused);
    }
    private native final boolean native_setup(Object Jet_this,
                int maxTracks, int trackBufferSize);
    private native final void    native_finalize();
    private native final void    native_release();
    private native final boolean native_loadJetFromFile(String pathToJetFile);
    private native final boolean native_loadJetFromFileD(FileDescriptor fd, long offset, long len);
    private native final boolean native_closeJetFile();
    private native final boolean native_playJet();
    private native final boolean native_pauseJet();
    private native final boolean native_queueJetSegment(int segmentNum, int libNum,
            int repeatCount, int transpose, int muteFlags, byte userID);
    private native final boolean native_queueJetSegmentMuteArray(int segmentNum, int libNum, 
            int repeatCount, int transpose, boolean[] muteArray, byte userID);
    private native final boolean native_setMuteFlags(int muteFlags, boolean sync);
    private native final boolean native_setMuteArray(boolean[]muteArray, boolean sync);
    private native final boolean native_setMuteFlag(int trackId, boolean muteFlag, boolean sync);
    private native final boolean native_triggerClip(int clipId);
    private native final boolean native_clearQueue();
    @SuppressWarnings("unused")
    private static void postEventFromNative(Object jetplayer_ref,
            int what, int arg1, int arg2) {
        JetPlayer jet = (JetPlayer)((WeakReference)jetplayer_ref).get();
        if ((jet != null) && (jet.mEventHandler != null)) {
            Message m = 
                jet.mEventHandler.obtainMessage(what, arg1, arg2, null);
            jet.mEventHandler.sendMessage(m);
        }
    }
    private final static String TAG = "JetPlayer-J";
    private static void logd(String msg) {
        Log.d(TAG, "[ android.media.JetPlayer ] " + msg);
    }
    private static void loge(String msg) {
        Log.e(TAG, "[ android.media.JetPlayer ] " + msg);
    }
}
