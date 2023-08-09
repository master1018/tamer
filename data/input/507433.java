public class MediaRecorderResetStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInRecordingState);
        assertTrue(!stateErrors.errorInInitialState);
        assertTrue(!stateErrors.errorInInitialStateAfterReset);
        assertTrue(!stateErrors.errorInInitialStateAfterStop);
        assertTrue(!stateErrors.errorInInitializedState);
        assertTrue(!stateErrors.errorInErrorState);
        assertTrue(!stateErrors.errorInDataSourceConfiguredState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.reset();
    }
    @MediumTest
    public void testReset() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "reset()";
    }
}
