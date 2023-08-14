public class MediaRecorderSetAudioSourceStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInInitialState);
        assertTrue(!stateErrors.errorInInitialStateAfterReset);
        assertTrue(!stateErrors.errorInInitialStateAfterStop);
        assertTrue(!stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInPreparedState);
        assertTrue(stateErrors.errorInRecordingState);
        assertTrue(stateErrors.errorInDataSourceConfiguredState);
        assertTrue(stateErrors.errorInErrorState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.setAudioSource(MediaRecorderStateUnitTestTemplate.AUDIO_SOURCE);
    }
    @MediumTest
    public void testSetAudioSource() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "setAudioSource()";
    }
}
