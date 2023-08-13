@TestTargetClass(VideoView.class)
public class VideoViewTest extends ActivityInstrumentationTestCase2<VideoViewStubActivity> {
    private static final long   TIME_OUT = 10000L;
    private static final long   OPERATION_INTERVAL  = 1500L;
    private static final int    TEST_VIDEO_DURATION = 11047;
    private static final String VIDEO_NAME   = "testvideo.3gp";
    private static final int DURATION_DELTA = 100;
    private VideoView mVideoView;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private String mVideoPath;
    private MediaController mMediaController;
    private static class MockListener {
        private boolean mTriggered;
        MockListener() {
            mTriggered = false;
        }
        public boolean isTriggered() {
            return mTriggered;
        }
        protected void onEvent() {
            mTriggered = true;
        }
    }
    private static class MockOnPreparedListener extends MockListener
            implements OnPreparedListener {
        public void onPrepared(MediaPlayer mp) {
            super.onEvent();
        }
    }
    private static class MockOnErrorListener extends MockListener implements OnErrorListener {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            super.onEvent();
            return false;
        }
    }
    private static class MockOnCompletionListener extends MockListener
            implements OnCompletionListener {
        public void onCompletion(MediaPlayer mp) {
            super.onEvent();
        }
    }
    public VideoViewTest() {
        super("com.android.cts.stub", VideoViewStubActivity.class);
    }
    private VideoView findVideoViewById(int id) {
        return (VideoView) mActivity.findViewById(id);
    }
    private String prepareSampleVideo() throws IOException {
        InputStream source = null;
        OutputStream target = null;
        try {
            source = mActivity.getResources().openRawResource(R.raw.testvideo);
            target = mActivity.openFileOutput(VIDEO_NAME, Context.MODE_WORLD_READABLE);
            final byte[] buffer = new byte[1024];
            for (int len = source.read(buffer); len > 0; len = source.read(buffer)) {
                target.write(buffer, 0, len);
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (target != null) {
                target.close();
            }
        }
        return mActivity.getFileStreamPath(VIDEO_NAME).getAbsolutePath();
    }
    private void waitForOperationComplete() throws InterruptedException {
        Thread.sleep(OPERATION_INTERVAL);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mVideoPath = prepareSampleVideo();
        assertNotNull(mVideoPath);
        mVideoView = findVideoViewById(R.id.videoview);
        mMediaController = new MediaController(mActivity);
        mVideoView.setMediaController(mMediaController);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "VideoView",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "VideoView",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "VideoView",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new VideoView(mActivity);
        new VideoView(mActivity, null);
        new VideoView(mActivity, null, 0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVideoPath",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnPreparedListener",
            args = {android.media.MediaPlayer.OnPreparedListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnCompletionListener",
            args = {android.media.MediaPlayer.OnCompletionListener.class}
        )
    })
    public void testPlayVideo1() throws Throwable {
        final MockOnPreparedListener preparedListener = new MockOnPreparedListener();
        mVideoView.setOnPreparedListener(preparedListener);
        final MockOnCompletionListener completionListener = new MockOnCompletionListener();
        mVideoView.setOnCompletionListener(completionListener);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.setVideoPath(mVideoPath);
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return preparedListener.isTriggered();
            }
        }.run();
        assertFalse(completionListener.isTriggered());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.start();
            }
        });
        new DelayedCheck(mVideoView.getDuration() + TIME_OUT) {
            @Override
            protected boolean check() {
                return completionListener.isTriggered();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVideoURI",
            args = {android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnPreparedListener",
            args = {android.media.MediaPlayer.OnPreparedListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPlaying",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "seekTo",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopPlayback",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentPosition",
            args = {}
        )
    })
    @BrokenTest("Fails in individual mode (current pos > 0 before start)")
    public void testPlayVideo2() throws Throwable {
        final int seekTo = mVideoView.getDuration() >> 1;
        final MockOnPreparedListener listener = new MockOnPreparedListener();
        mVideoView.setOnPreparedListener(listener);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.setVideoURI(Uri.parse(mVideoPath));
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return listener.isTriggered();
            }
        }.run();
        assertEquals(0, mVideoView.getCurrentPosition());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.start();
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mVideoView.isPlaying();
            }
        }.run();
        assertTrue(mVideoView.getCurrentPosition() > 0);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.pause();
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return !mVideoView.isPlaying();
            }
        }.run();
        int currentPosition = mVideoView.getCurrentPosition();
        Thread.sleep(OPERATION_INTERVAL);
        assertEquals(currentPosition, mVideoView.getCurrentPosition());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.seekTo(seekTo);
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mVideoView.getCurrentPosition() >= seekTo;
            }
        }.run();
        assertFalse(mVideoView.isPlaying());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.start();
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mVideoView.isPlaying();
            }
        }.run();
        assertTrue(mVideoView.getCurrentPosition() > seekTo);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.stopPlayback();
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return !mVideoView.isPlaying();
            }
        }.run();
        assertEquals(0, mVideoView.getCurrentPosition());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnErrorListener",
        args = {android.media.MediaPlayer.OnErrorListener.class}
    )
    public void testSetOnErrorListener() throws Throwable {
        final MockOnErrorListener listener = new MockOnErrorListener();
        mVideoView.setOnErrorListener(listener);
        runTestOnUiThread(new Runnable() {
            public void run() {
                String path = "unknown path";
                mVideoView.setVideoPath(path);
                mVideoView.start();
            }
        });
        mInstrumentation.waitForIdleSync();
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return listener.isTriggered();
            }
        }.run();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBufferPercentage",
        args = {}
    )
    public void testGetBufferPercentage() throws Throwable {
        final MockOnPreparedListener prepareListener = new MockOnPreparedListener();
        mVideoView.setOnPreparedListener(prepareListener);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.setVideoPath(mVideoPath);
            }
        });
        mInstrumentation.waitForIdleSync();
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return prepareListener.isTriggered();
            }
        }.run();
        int percent = mVideoView.getBufferPercentage();
        assertTrue(percent >= 0 && percent <= 100);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "resolveAdjustedSize",
        args = {int.class, int.class}
    )
    public void testResolveAdjustedSize() {
        mVideoView = new VideoView(mActivity);
        final int desiredSize = 100;
        int resolvedSize = mVideoView.resolveAdjustedSize(desiredSize, MeasureSpec.UNSPECIFIED);
        assertEquals(desiredSize, resolvedSize);
        final int specSize = MeasureSpec.getSize(MeasureSpec.AT_MOST);
        resolvedSize = mVideoView.resolveAdjustedSize(desiredSize, MeasureSpec.AT_MOST);
        assertEquals(Math.min(desiredSize, specSize), resolvedSize);
        resolvedSize = mVideoView.resolveAdjustedSize(desiredSize, MeasureSpec.EXACTLY);
        assertEquals(specSize, resolvedSize);
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onTouchEvent",
        args = {android.view.MotionEvent.class}
    )
    public void testOnTouchEvent() {
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "onKeyDown",
        args = {int.class, android.view.KeyEvent.class}
    )
    @ToBeFixed(bug = "", explanation = "After pressing KEYCODE_HEADSETHOOK, "
            + "the video should be playing, but it did not until time out.")
    @BrokenTest("Video starts playing automatically after setting the path.")
    public void testOnKeyDown() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.setVideoPath(mVideoPath);
                mVideoView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertFalse(mVideoView.isPlaying());
        sendKeys(KeyEvent.KEYCODE_HEADSETHOOK);
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return !mVideoView.isPlaying();
            }
        }.run();
        assertFalse(mMediaController.isShowing());
        sendKeys(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return !mVideoView.isPlaying();
            }
        }.run();
        assertFalse(mMediaController.isShowing());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.start();
            }
        });
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mVideoView.isPlaying();
            }
        }.run();
        sendKeys(KeyEvent.KEYCODE_MEDIA_STOP);
        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return !mVideoView.isPlaying();
            }
        }.run();
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onTrackballEvent",
        args = {android.view.MotionEvent.class}
    )
    public void testOnTrackballEvent() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDuration",
        args = {}
    )
    public void testGetDuration() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mVideoView.setVideoPath(mVideoPath);
            }
        });
        waitForOperationComplete();
        assertTrue(Math.abs(mVideoView.getDuration() - TEST_VIDEO_DURATION) < DURATION_DELTA);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setMediaController",
        args = {android.widget.MediaController.class}
    )
    public void testSetMediaController() {
        final MediaController ctlr = new MediaController(mActivity);
        mVideoView.setMediaController(ctlr);
    }
}
