@TestTargetClass(MediaPlayer.class)
public class MediaPlayerTest extends ActivityInstrumentationTestCase2<MediaStubActivity> {
    private static String TAG = "CtsMediaPlayerTest";
    private static final int SLEEP_TIME = 1000;
    private final String mSourceMediaOnSdcard;
    private Monitor mOnVideoSizeChangedCalled = new Monitor();
    private Monitor mOnBufferingUpdateCalled = new Monitor();
    private Monitor mOnPrepareCalled = new Monitor();
    private Monitor mOnSeekCompleteCalled = new Monitor();
    private Monitor mOnCompletionCalled = new Monitor();
    private Monitor mOnInfoCalled = new Monitor();
    private Monitor mOnErrorCalled = new Monitor();
    private Context mContext;
    private Resources mResources;
    private CtsTestServer mServer;
    private static Object sVideoSizeChanged;
    private static Object sLock;
    private static Looper sLooper = null;
    private static final int WAIT_FOR_COMMAND_TO_COMPLETE = 60000;  
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    static MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener =
        new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                synchronized (sVideoSizeChanged) {
                    Log.v(TAG, "sizechanged notification received ...");
                    sVideoSizeChanged.notify();
                }
            }
        };
    private static class Monitor {
        private boolean signalled;
        public synchronized void signal() {
            signalled = true;
            notifyAll();
        }
        public synchronized void waitForSignal() throws InterruptedException {
            while (!signalled) {
                wait();
            }
        }
    }
    public MediaPlayerTest() {
        super("com.android.cts.stub", MediaStubActivity.class);
        mSourceMediaOnSdcard = new File(Environment.getExternalStorageDirectory(),
                                        "record_and_play.3gp").getAbsolutePath();
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mResources = mContext.getResources();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        File file = new File(mSourceMediaOnSdcard);
        if (file.exists()) {
            file.delete();
        }
        if (mServer != null) {
            mServer.shutdown();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "create",
            args = {Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAudioStreamType",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWakeMode",
            args = {Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPlaying",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLooping",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isLooping",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentPosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "seekTo",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSource",
            args = {FileDescriptor.class, long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "prepare",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDuration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        )
    })
    public void testPlayAudio() throws Exception {
        final int mp3Duration = 34909;
        final int tolerance = 70;
        final int seekDuration = 100;
        final int resid = R.raw.testmp3_2;
        MediaPlayer mp = MediaPlayer.create(mContext, resid);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        assertFalse(mp.isPlaying());
        mp.start();
        assertTrue(mp.isPlaying());
        assertFalse(mp.isLooping());
        mp.setLooping(true);
        assertTrue(mp.isLooping());
        assertEquals(mp3Duration, mp.getDuration(), tolerance);
        int pos = mp.getCurrentPosition();
        assertTrue(pos >= 0);
        assertTrue(pos < mp3Duration - seekDuration);
        mp.seekTo(pos + seekDuration);
        assertEquals(pos + seekDuration, mp.getCurrentPosition(), tolerance);
        mp.pause();
        Thread.sleep(SLEEP_TIME);
        assertFalse(mp.isPlaying());
        mp.start();
        assertTrue(mp.isPlaying());
        mp.stop();
        mp.reset();
        AssetFileDescriptor afd = mResources.openRawResourceFd(resid);
        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
        mp.prepare();
        assertFalse(mp.isPlaying());
        mp.start();
        assertTrue(mp.isPlaying());
        while(mp.isPlaying()) {
            Thread.sleep(SLEEP_TIME);
        }
        mp.release();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "MediaPlayer",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDisplay",
            args = {SurfaceHolder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScreenOnWhilePlaying",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVideoHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVideoWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolume",
            args = {float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSource",
            args = {FileDescriptor.class}
        )
    })
    private static void initializeMessageLooper() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                sLooper = Looper.myLooper();
                synchronized (sLock) {
                    sLock.notify();
                }
                Looper.loop();  
                Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();
    }
    private static void terminateMessageLooper() {
        sLooper.quit();
    }
    public void testPlayVideo() throws Exception {
        final int expectedVideoWidth = 352; 
        final int expectedVideoHeight = 288; 
        final float leftVolume = 0.5f;
        final float rightVolume = 0.5f;
        final int resid = R.raw.testvideo;
        sLock = new Object();
        sVideoSizeChanged = new Object();
        MediaPlayer mp = new MediaPlayer();
        AssetFileDescriptor afd = mResources.openRawResourceFd(resid);
        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
        mp.setDisplay(getActivity().getSurfaceHolder());
        mp.setScreenOnWhilePlaying(true);
        mp.prepare();
        int videoWidth = 0;
        int videoHeight = 0;
        synchronized (sLock) {
            initializeMessageLooper();
            try {
                sLock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
            } catch(Exception e) {
                Log.v(TAG, "looper was interrupted.");
                return;
            }
        }
        try {
             mp.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
             synchronized (sVideoSizeChanged) {
                 try {
                     sVideoSizeChanged.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                 } catch (Exception e) {
                     Log.v(TAG, "wait was interrupted");
                 }
             }
             videoWidth = mp.getVideoWidth();
             videoHeight = mp.getVideoHeight();
             terminateMessageLooper();
        } catch (Exception e) {
             Log.e(TAG, e.getMessage());
        }
        assertEquals(expectedVideoWidth, videoWidth);
        assertEquals(expectedVideoHeight, videoHeight);
        mp.start();
        mp.setVolume(leftVolume, rightVolume);
        while (mp.isPlaying()) {
            Thread.sleep(SLEEP_TIME);
        }
        mp.release();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnBufferingUpdateListener",
            args = {OnBufferingUpdateListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSource",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDisplay",
            args = {SurfaceHolder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "prepare",
            args = {}
        )
    })
    public void testPlayStream() throws Throwable {
        mServer = new CtsTestServer(mContext);
        final String stream_mp3 = mServer.getAssetUrl("ringer.mp3");
        mMediaPlayer.setDataSource(stream_mp3);
        mMediaPlayer.setDisplay(getActivity().getSurfaceHolder());
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mOnBufferingUpdateCalled.signal();
            }
        });
        assertFalse(mOnBufferingUpdateCalled.signalled);
        mMediaPlayer.prepare();
        mOnBufferingUpdateCalled.waitForSignal();
        mMediaPlayer.start();
        Thread.sleep(SLEEP_TIME);
        mMediaPlayer.stop();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnCompletionListener",
            args = {OnCompletionListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnErrorListener",
            args = {OnErrorListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnPreparedListener",
            args = {OnPreparedListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnSeekCompleteListener",
            args = {OnSeekCompleteListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnVideoSizeChangedListener",
            args = {OnVideoSizeChangedListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnInfoListener",
            args = {MediaPlayer.OnInfoListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSource",
            args = {FileDescriptor.class, long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "prepare",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPlaying",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "seekTo",
            args = {int.class}
        )
    })
    public void testCallback() throws Throwable {
        final int mp4Duration = 8484;
        AssetFileDescriptor afd = mResources.openRawResourceFd(R.raw.testvideo);
        try {
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } finally {
            afd.close();
        }
        mMediaPlayer.setDisplay(getActivity().getSurfaceHolder());
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                mOnVideoSizeChangedCalled.signal();
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mOnPrepareCalled.signal();
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            public void onSeekComplete(MediaPlayer mp) {
                mOnSeekCompleteCalled.signal();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mOnCompletionCalled.signal();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mOnErrorCalled.signal();
                return false;
            }
        });
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mOnInfoCalled.signal();
                return false;
            }
        });
        assertFalse(mOnPrepareCalled.signalled);
        assertFalse(mOnVideoSizeChangedCalled.signalled);
        mMediaPlayer.prepare();
        mOnPrepareCalled.waitForSignal();
        mOnVideoSizeChangedCalled.waitForSignal();
        mOnSeekCompleteCalled.signalled = false;
        mMediaPlayer.seekTo(mp4Duration >> 1);
        mOnSeekCompleteCalled.waitForSignal();
        assertFalse(mOnCompletionCalled.signalled);
        mMediaPlayer.start();
        while(mMediaPlayer.isPlaying()) {
            Thread.sleep(SLEEP_TIME);
        }
        assertFalse(mMediaPlayer.isPlaying());
        mOnCompletionCalled.waitForSignal();
        assertFalse(mOnErrorCalled.signalled);
        mMediaPlayer.stop();
        mMediaPlayer.start();
        mOnErrorCalled.waitForSignal();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSource",
            args = {Context.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSource",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "create",
            args = {Context.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "create",
            args = {Context.class, Uri.class, SurfaceHolder.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "prepareAsync",
            args = {}
        )
    })
    public void testRecordAndPlay() throws Exception {
        recordMedia();
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(mSourceMediaOnSdcard);
        mp.prepareAsync();
        Thread.sleep(SLEEP_TIME);
        playAndStop(mp);
        mp.reset();
        Uri uri = Uri.parse(mSourceMediaOnSdcard);
        mp = new MediaPlayer();
        mp.setDataSource(mContext, uri);
        mp.prepareAsync();
        Thread.sleep(SLEEP_TIME);
        playAndStop(mp);
        mp.release();
        mp = MediaPlayer.create(mContext, uri);
        playAndStop(mp);
        mp.release();
        mp = MediaPlayer.create(mContext, uri, getActivity().getSurfaceHolder());
        playAndStop(mp);
        mp.release();
    }
    private void playAndStop(MediaPlayer mp) throws Exception {
        mp.start();
        Thread.sleep(SLEEP_TIME);
        mp.stop();
    }
    private void recordMedia() throws Exception {
        MediaRecorder mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mr.setOutputFile(mSourceMediaOnSdcard);
        mr.prepare();
        mr.start();
        Thread.sleep(SLEEP_TIME);
        mr.stop();
        mr.release();
    }
}
