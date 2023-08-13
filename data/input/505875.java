class MediaPlayerStateUnitTestTemplate extends AndroidTestCase {
    private static final String TEST_PATH = MediaNames.TEST_PATH_1;
    private static final String TAG = "MediaPlayerSeekToStateUnitTest";
    private static final int SEEK_TO_END  = 135110;  
    private static int WAIT_FOR_COMMAND_TO_COMPLETE = 1000;  
    private MediaPlayerStateErrors mStateErrors = new MediaPlayerStateErrors();
    private MediaPlayer mMediaPlayer = null;
    private boolean mInitialized = false;
    private boolean mOnCompletionHasBeenCalled = false;
    private MediaPlayerStateErrors.MediaPlayerState mMediaPlayerState = null;
    private Looper mLooper = null;
    private final Object lock = new Object();
    private MediaPlayerMethodUnderTest mMethodUnderTest = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };
    public void runTestOnMethod(MediaPlayerMethodUnderTest testMethod) {
        mMethodUnderTest = testMethod;
        if (mMethodUnderTest != null) {  
            initializeMessageLooper();
            synchronized(lock) {
                try {
                    lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                } catch(Exception e) {
                    Log.v(TAG, "runTestOnMethod: wait was interrupted.");
                }
            }
            assertTrue(mInitialized);  
            checkMethodUnderTestInAllPossibleStates();
            terminateMessageLooper();   
            assertTrue(mOnCompletionHasBeenCalled);
            mMethodUnderTest.checkStateErrors(mStateErrors);
            cleanUp();
        }
    }
    private void initializeMessageLooper() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mMediaPlayer = new MediaPlayer();                
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer player, int what, int extra) {
                        Log.v(TAG, "onError has been called.");
                        synchronized(lock) {
                            Log.v(TAG, "notify lock.");
                            setStateError(mMediaPlayerState, true);
                            if (mMediaPlayerState != MediaPlayerStateErrors.MediaPlayerState.ERROR) {
                                notifyStateError();
                            }
                            lock.notify();
                        }
                        return true;
                    }
                });
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer player) {
                        Log.v(TAG, "onCompletion has been called.");
                        synchronized(lock) {
                            if (mMediaPlayerState == MediaPlayerStateErrors.MediaPlayerState.PLAYBACK_COMPLETED) {
                                mOnCompletionHasBeenCalled = true;
                            }
                            lock.notify();
                        }
                    }
                });
                synchronized(lock) {
                    mInitialized = true;
                    lock.notify();
                }
                Looper.loop();  
                Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();
    }
    private void callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState state) {
        Log.v(TAG, "call " + mMethodUnderTest + ": started in state " + state);
        setMediaPlayerToState(state);
        mMethodUnderTest.invokeMethodUnderTest(mMediaPlayer);
        synchronized(lock) {
            try {
                lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
           } catch(Exception e) {
               Log.v(TAG, "callMediaPlayerMethodUnderTestInState: wait is interrupted in state " + state);
           }
        }
        Log.v(TAG, "call " + mMethodUnderTest + ": ended in state " + state);
    }
    private void setMediaPlayerToIdleStateAfterReset() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.reset();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToIdleStateAfterReset: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToInitializedState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToInitializedState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToPreparedState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToPreparedState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToPreparedStateAfterStop() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.stop();
            mMediaPlayer.prepare();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToPreparedStateAfterStop: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToStartedState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToStartedState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToStartedStateAfterPause() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.pause();
            try {
                Thread.sleep(MediaNames.PAUSE_WAIT_TIME);
            } catch(Exception ie) {
                Log.v(TAG, "sleep was interrupted and terminated prematurely");
            }
            mMediaPlayer.start();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToStartedStateAfterPause: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToPausedState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.pause();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToPausedState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToStoppedState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.stop();
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToStoppedState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
    }
    private void setMediaPlayerToPlaybackCompletedState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(SEEK_TO_END);
            mMediaPlayer.start();
            synchronized(lock) {
                try {
                    lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                } catch(Exception e) {
                    Log.v(TAG, "setMediaPlayerToPlaybackCompletedState: wait was interrupted.");
                }
            }
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToPlaybackCompletedState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(false);
        }
        Log.v(TAG, "setMediaPlayerToPlaybackCompletedState: done.");
    }
    private void setMediaPlayerToErrorState() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(TEST_PATH);
            mMediaPlayer.start();
            synchronized(lock) {
                try {
                    lock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
                } catch(Exception e) {
                    Log.v(TAG, "setMediaPlayerToErrorState: wait was interrupted.");
                }
            }
        } catch(Exception e) {
            Log.v(TAG, "setMediaPlayerToErrorState: Exception " + e.getClass().getName() + " was thrown.");
            assertTrue(e instanceof IllegalStateException);
        }
        Log.v(TAG, "setMediaPlayerToErrorState: done.");
    }
    private void setMediaPlayerToState(MediaPlayerStateErrors.MediaPlayerState state) {
        mMediaPlayerState = state;
        switch(state) {
            case IDLE:
                break;
            case IDLE_AFTER_RESET:
                setMediaPlayerToIdleStateAfterReset();
                break;
            case INITIALIZED:
                setMediaPlayerToInitializedState();
                break;
            case PREPARED:
                setMediaPlayerToPreparedState();
                break;
            case PREPARED_AFTER_STOP:
                setMediaPlayerToPreparedStateAfterStop();
                break;
            case STARTED:
                setMediaPlayerToStartedState();
                break;
            case STARTED_AFTER_PAUSE:
                setMediaPlayerToStartedStateAfterPause();
                break;
            case PAUSED:
                setMediaPlayerToPausedState();
                break;
            case STOPPED:
                setMediaPlayerToStoppedState();
                break;
            case PLAYBACK_COMPLETED:
                setMediaPlayerToPlaybackCompletedState();
                break;
            case ERROR:
                setMediaPlayerToErrorState();
                break;
        }
    }
    private void setStateError(MediaPlayerStateErrors.MediaPlayerState state, boolean error) {
        switch(state) {
            case IDLE:
                mStateErrors.errorInIdleState = error;
                break;
            case IDLE_AFTER_RESET:
                mStateErrors.errorInIdleStateAfterReset = error;
                break;
            case INITIALIZED:
                mStateErrors.errorInInitializedState = error;
                break;
            case PREPARED:
                mStateErrors.errorInPreparedState = error;
                break;
            case PREPARED_AFTER_STOP:
                mStateErrors.errorInPreparedStateAfterStop = error;
                break;
            case STARTED:
                mStateErrors.errorInStartedState = error;
                break;
            case STARTED_AFTER_PAUSE:
                mStateErrors.errorInStartedStateAfterPause = error;
                break;
            case PAUSED:
                mStateErrors.errorInPausedState = error;
                break;
            case STOPPED:
                mStateErrors.errorInStoppedState = error;
                break;
            case PLAYBACK_COMPLETED:
                mStateErrors.errorInPlaybackCompletedState = error;
                break;
            case ERROR:
                mStateErrors.errorInErrorState = error;
                break;
        }
    }
    private void notifyStateError() {
        mHandler.sendMessage(mHandler.obtainMessage(MediaPlayerStateErrors.MEDIA_PLAYER_ERROR));
    }
    private void checkIdleState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.IDLE);
    }
    private void checkIdleStateAfterReset() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.IDLE_AFTER_RESET);
    }
    private void checkInitializedState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.INITIALIZED);
    }
    private void checkPreparedState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.PREPARED);
    }
    private void checkPreparedStateAfterStop() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.PREPARED_AFTER_STOP);
    }
    private void checkStartedState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.STARTED);
    }
    private void checkPausedState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.PAUSED);
    }
    private void checkStartedStateAfterPause() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.STARTED_AFTER_PAUSE);
    }
    private void checkStoppedState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.STOPPED);
    }
    private void checkPlaybackCompletedState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.PLAYBACK_COMPLETED);
    }
    private void checkErrorState() {
        callMediaPlayerMethodUnderTestInState(MediaPlayerStateErrors.MediaPlayerState.ERROR);
    }
    private void checkMethodUnderTestInAllPossibleStates() {
        checkIdleState(); 
        checkErrorState();
        checkIdleStateAfterReset();
        checkInitializedState();
        checkStartedState();
        checkStartedStateAfterPause();
        checkPausedState();
        checkPreparedState();
        checkPreparedStateAfterStop();
        checkPlaybackCompletedState();
        checkStoppedState();
    }
    private void terminateMessageLooper() {
        mLooper.quit();
        mMediaPlayer.release();
    }
    private void cleanUp() {
        mMediaPlayer = null;
        mMediaPlayerState = null;
        mLooper = null;
        mStateErrors = null;
        mMethodUnderTest = null;
    }
}
