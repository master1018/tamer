public class MediaRecorderStopStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInRecordingState);
        assertTrue(stateErrors.errorInInitialState);
        assertTrue(stateErrors.errorInInitialStateAfterReset);
        assertTrue(stateErrors.errorInInitialStateAfterStop);
        assertTrue(stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(stateErrors.errorInDataSourceConfiguredState);
        assertTrue(stateErrors.errorInPreparedState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.stop();
    }
    @MediumTest
    public void testStop() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "stop()";
    }
}
