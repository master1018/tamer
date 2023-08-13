class MediaRecorderStateUnitTestTemplate extends AndroidTestCase {
    public static final String RECORD_OUTPUT_PATH = "/sdcard/recording.3gp";
    public static final int OUTPUT_FORMAT= MediaRecorder.OutputFormat.THREE_GPP;
    public static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AMR_NB;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final String TAG = "MediaRecorderStateUnitTest";
    private MediaRecorderStateErrors mStateErrors = new MediaRecorderStateErrors();
    private MediaRecorder mMediaRecorder = new MediaRecorder();
    private MediaRecorderStateErrors.MediaRecorderState mMediaRecorderState = null;
    private MediaRecorderMethodUnderTest mMethodUnderTest = null;
    public void runTestOnMethod(MediaRecorderMethodUnderTest testMethod) {
        mMethodUnderTest = testMethod;
        if (mMethodUnderTest != null) {  
            checkMethodUnderTestInAllPossibleStates();
            mMethodUnderTest.checkStateErrors(mStateErrors);
            cleanUp();
        }
    }
    private void callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState state) {
        Log.v(TAG, "call " + mMethodUnderTest + ": started in state " + state);
        setMediaRecorderToState(state);
        try {
            mMethodUnderTest.invokeMethodUnderTest(mMediaRecorder);
        } catch(Exception e) {
            setStateError(mMediaRecorderState, true);
        }
        Log.v(TAG, "call " + mMethodUnderTest + ": ended in state " + state);
    }
    private void setMediaRecorderToInitialStateAfterReset() {
        try {
            mMediaRecorder.reset();
        } catch(Exception e) {
            fail("setMediaRecorderToInitialStateAfterReset: Exception " + e.getClass().getName() + " was thrown.");
        }
    }
    private void setMediaRecorderToInitialStateAfterStop() {
        try {
            mMediaRecorder.reset();
        } catch(Exception e) {
            fail("setMediaRecorderToInitialStateAfterReset: Exception " + e.getClass().getName() + " was thrown.");
        }
    }
    private void setMediaRecorderToInitializedState() {
        try {
            mMediaRecorder.reset();
            if (mMethodUnderTest.toString() != "setAudioSource()") {
                mMediaRecorder.setAudioSource(AUDIO_SOURCE);
            }
        } catch(Exception e) {
            fail("setMediaRecorderToInitializedState: Exception " + e.getClass().getName() + " was thrown.");
        }
    }
    private void setMediaRecorderToPreparedState() {
        try {
            mMediaRecorder.reset();
            mMediaRecorder.setAudioSource(AUDIO_SOURCE);
            mMediaRecorder.setOutputFormat(OUTPUT_FORMAT);
            mMediaRecorder.setAudioEncoder(AUDIO_ENCODER);
            mMediaRecorder.setOutputFile(RECORD_OUTPUT_PATH);
            mMediaRecorder.prepare();
        } catch(Exception e) {
            fail("setMediaRecorderToPreparedState: Exception " + e.getClass().getName() + " was thrown.");
        }
    }
    private void setMediaRecorderToRecordingState() {
        try {
            mMediaRecorder.reset();
            mMediaRecorder.setAudioSource(AUDIO_SOURCE);
            mMediaRecorder.setOutputFormat(OUTPUT_FORMAT);
            mMediaRecorder.setAudioEncoder(AUDIO_ENCODER);
            mMediaRecorder.setOutputFile(RECORD_OUTPUT_PATH);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch(Exception e) {
            fail("setMediaRecorderToRecordingState: Exception " + e.getClass().getName() + " was thrown.");
        }
    }
    private void setMediaRecorderToDataSourceConfiguredState() {
        try {
            mMediaRecorder.reset();
            mMediaRecorder.setAudioSource(AUDIO_SOURCE);
            mMediaRecorder.setOutputFormat(OUTPUT_FORMAT);
            if (mMethodUnderTest.toString() != "setAudioEncoder()") {
                mMediaRecorder.setAudioEncoder(AUDIO_ENCODER);
            }
            if (mMethodUnderTest.toString() != "setOutputFile()") {
                mMediaRecorder.setOutputFile(RECORD_OUTPUT_PATH);
            }
        } catch(Exception e) {
            fail("setMediaRecorderToDataSourceConfiguredState: Exception " + e.getClass().getName() + " was thrown.");
        }
    }
    private void setMediaRecorderToErrorState() {
        try {
            mMediaRecorder.reset();
            if (mMethodUnderTest.toString() != "setAudioEncoder()") {
                mMediaRecorder.setAudioSource(AUDIO_SOURCE);
            }
            if (mMethodUnderTest.toString() != "setOutputFile()") {
                mMediaRecorder.setOutputFormat(OUTPUT_FORMAT);
            }
            mMediaRecorder.start();
        } catch(Exception e) {
            if (!(e instanceof IllegalStateException)) {
                fail("setMediaRecorderToErrorState: Exception " + e.getClass().getName() + " was thrown.");
            }
        }
        Log.v(TAG, "setMediaRecorderToErrorState: done.");
    }
    private void setMediaRecorderToState(MediaRecorderStateErrors.MediaRecorderState state) {
        mMediaRecorderState = state;
        switch(state) {
            case INITIAL:
                break;
            case INITIAL_AFTER_RESET:
                setMediaRecorderToInitialStateAfterReset();
                break;
            case INITIAL_AFTER_STOP:
                setMediaRecorderToInitialStateAfterStop();
                break;
            case INITIALIZED:
                setMediaRecorderToInitializedState();
                break;
            case DATASOURCECONFIGURED:
                setMediaRecorderToDataSourceConfiguredState();
                break;
            case PREPARED:
                setMediaRecorderToPreparedState();
                break;
            case RECORDING:
                setMediaRecorderToRecordingState();
                break;
            case ERROR:
                setMediaRecorderToErrorState();
                break;
        }
    }
    private void setStateError(MediaRecorderStateErrors.MediaRecorderState state, boolean error) {
        switch(state) {
            case INITIAL:
                mStateErrors.errorInInitialState = error;
                break;
            case INITIAL_AFTER_RESET:
                mStateErrors.errorInInitialStateAfterReset = error;
                break;
            case INITIAL_AFTER_STOP:
                mStateErrors.errorInInitialStateAfterStop = error;
                break;
            case INITIALIZED:
                mStateErrors.errorInInitializedState = error;
                break;
            case DATASOURCECONFIGURED:
                mStateErrors.errorInDataSourceConfiguredState = error;
                break;
            case PREPARED:
                mStateErrors.errorInPreparedState = error;
                break;
            case RECORDING:
                mStateErrors.errorInRecordingState = error;
                break;
            case ERROR:
                mStateErrors.errorInErrorState = error;
                break;
        }
    }
    private void checkInitialState() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.INITIAL);
    }
    private void checkInitialStateAfterReset() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.INITIAL_AFTER_RESET);
    }
    private void checkInitialStateAfterStop() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.INITIAL_AFTER_STOP);
    }
    private void checkInitializedState() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.INITIALIZED);
    }
    private void checkPreparedState() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.PREPARED);
    }
    private void checkRecordingState() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.RECORDING);
    }
    private void checkDataSourceConfiguredState() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.DATASOURCECONFIGURED);
    }
    private void checkErrorState() {
        callMediaRecorderMethodUnderTestInState(MediaRecorderStateErrors.MediaRecorderState.ERROR);
    }
    private void checkMethodUnderTestInAllPossibleStates() {
        checkInitialState(); 
        checkErrorState();
        checkInitialStateAfterReset();
        checkInitialStateAfterStop();
        checkInitializedState();
        checkRecordingState();
        checkDataSourceConfiguredState();
        checkPreparedState();
    }
    private void cleanUp() {
        mMediaRecorder.release();
        mMediaRecorder = null;
        mMediaRecorderState = null;
        mStateErrors = null;
        mMethodUnderTest = null;
    }
}
