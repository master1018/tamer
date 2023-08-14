public class NotificationPlayer implements OnCompletionListener {
    private static final int PLAY = 1;
    private static final int STOP = 2;
    private static final boolean mDebug = false;
    private static final class Command {
        int code;
        Context context;
        Uri uri;
        boolean looping;
        int stream;
        long requestTime;
        public String toString() {
            return "{ code=" + code + " looping=" + looping + " stream=" + stream
                    + " uri=" + uri + " }";
        }
    }
    private LinkedList<Command> mCmdQueue = new LinkedList();
    private Looper mLooper;
    private final class CreationAndCompletionThread extends Thread {
        public Command mCmd;
        public CreationAndCompletionThread(Command cmd) {
            super();
            mCmd = cmd;
        }
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            synchronized(this) {
                AudioManager audioManager =
                    (AudioManager) mCmd.context.getSystemService(Context.AUDIO_SERVICE);
                try {
                    MediaPlayer player = new MediaPlayer();
                    player.setAudioStreamType(mCmd.stream);
                    player.setDataSource(mCmd.context, mCmd.uri);
                    player.setLooping(mCmd.looping);
                    player.prepare();
                    if ((mCmd.uri != null) && (mCmd.uri.getEncodedPath() != null)
                            && (mCmd.uri.getEncodedPath().length() > 0)) {
                        if (mCmd.looping) {
                            audioManager.requestAudioFocus(null, mCmd.stream,
                                    AudioManager.AUDIOFOCUS_GAIN);
                        } else {
                            audioManager.requestAudioFocus(null, mCmd.stream,
                                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                        }
                    }
                    player.setOnCompletionListener(NotificationPlayer.this);
                    player.start();
                    if (mPlayer != null) {
                        mPlayer.release();
                    }
                    mPlayer = player;
                }
                catch (Exception e) {
                    Log.w(mTag, "error loading sound for " + mCmd.uri, e);
                }
                mAudioManager = audioManager;
                this.notify();
            }
            Looper.loop();
        }
    };
    private void startSound(Command cmd) {
        try {
            if (mDebug) Log.d(mTag, "Starting playback");
            synchronized(mCompletionHandlingLock) {
                if((mLooper != null)
                        && (mLooper.getThread().getState() != Thread.State.TERMINATED)) {
                    mLooper.quit();
                }
                mCompletionThread = new CreationAndCompletionThread(cmd);
                synchronized(mCompletionThread) {
                    mCompletionThread.start();
                    mCompletionThread.wait();
                }
            }
            long delay = SystemClock.uptimeMillis() - cmd.requestTime;
            if (delay > 1000) {
                Log.w(mTag, "Notification sound delayed by " + delay + "msecs");
            }
        }
        catch (Exception e) {
            Log.w(mTag, "error loading sound for " + cmd.uri, e);
        }
    }
    private final class CmdThread extends java.lang.Thread {
        CmdThread() {
            super("NotificationPlayer-" + mTag);
        }
        public void run() {
            while (true) {
                Command cmd = null;
                synchronized (mCmdQueue) {
                    if (mDebug) Log.d(mTag, "RemoveFirst");
                    cmd = mCmdQueue.removeFirst();
                }
                switch (cmd.code) {
                case PLAY:
                    if (mDebug) Log.d(mTag, "PLAY");
                    startSound(cmd);
                    break;
                case STOP:
                    if (mDebug) Log.d(mTag, "STOP");
                    if (mPlayer != null) {
                        long delay = SystemClock.uptimeMillis() - cmd.requestTime;
                        if (delay > 1000) {
                            Log.w(mTag, "Notification stop delayed by " + delay + "msecs");
                        }
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                        mAudioManager.abandonAudioFocus(null);
                        mAudioManager = null;
                        if((mLooper != null)
                                && (mLooper.getThread().getState() != Thread.State.TERMINATED)) {
                            mLooper.quit();
                        }
                    } else {
                        Log.w(mTag, "STOP command without a player");
                    }
                    break;
                }
                synchronized (mCmdQueue) {
                    if (mCmdQueue.size() == 0) {
                        mThread = null;
                        releaseWakeLock();
                        return;
                    }
                }
            }
        }
    }
    public void onCompletion(MediaPlayer mp) {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
        }
        synchronized (mCmdQueue) {
            if (mCmdQueue.size() == 0) {
                synchronized(mCompletionHandlingLock) {
                    if(mLooper != null) {
                        mLooper.quit();
                    }
                    mCompletionThread = null;
                }
            }
        }
    }
    private String mTag;
    private CmdThread mThread;
    private CreationAndCompletionThread mCompletionThread;
    private final Object mCompletionHandlingLock = new Object();
    private MediaPlayer mPlayer;
    private PowerManager.WakeLock mWakeLock;
    private AudioManager mAudioManager;
    private int mState = STOP;
    public NotificationPlayer(String tag) {
        if (tag != null) {
            mTag = tag;
        } else {
            mTag = "NotificationPlayer";
        }
    }
    public void play(Context context, Uri uri, boolean looping, int stream) {
        Command cmd = new Command();
        cmd.requestTime = SystemClock.uptimeMillis();
        cmd.code = PLAY;
        cmd.context = context;
        cmd.uri = uri;
        cmd.looping = looping;
        cmd.stream = stream;
        synchronized (mCmdQueue) {
            enqueueLocked(cmd);
            mState = PLAY;
        }
    }
    public void stop() {
        synchronized (mCmdQueue) {
            if (mState != STOP) {
                Command cmd = new Command();
                cmd.requestTime = SystemClock.uptimeMillis();
                cmd.code = STOP;
                enqueueLocked(cmd);
                mState = STOP;
            }
        }
    }
    private void enqueueLocked(Command cmd) {
        mCmdQueue.add(cmd);
        if (mThread == null) {
            acquireWakeLock();
            mThread = new CmdThread();
            mThread.start();
        }
    }
    public void setUsesWakeLock(Context context) {
        if (mWakeLock != null || mThread != null) {
            throw new RuntimeException("assertion failed mWakeLock=" + mWakeLock
                    + " mThread=" + mThread);
        }
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mTag);
    }
    private void acquireWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }
    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
}
