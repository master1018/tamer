public class MediaRecorderSetAudioEncoderStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInDataSourceConfiguredState);
        assertTrue(stateErrors.errorInPreparedState);
        assertTrue(stateErrors.errorInRecordingState);
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(stateErrors.errorInInitialState);
        assertTrue(stateErrors.errorInInitialStateAfterReset);
        assertTrue(stateErrors.errorInInitialStateAfterStop);
        assertTrue(stateErrors.errorInInitializedState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.setAudioEncoder(MediaRecorderStateUnitTestTemplate.AUDIO_ENCODER);
    }
    @MediumTest
    public void testSetAudioEncoder() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "setAudioEncoder()";
    }
}
