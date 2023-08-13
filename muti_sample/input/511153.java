public class MediaRecorderPrepareStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInDataSourceConfiguredState);
        assertTrue(stateErrors.errorInPreparedState);
        assertTrue(stateErrors.errorInRecordingState);
        assertTrue(stateErrors.errorInInitialState);
        assertTrue(stateErrors.errorInInitialStateAfterReset);
        assertTrue(stateErrors.errorInInitialStateAfterStop);
        assertTrue(stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInErrorState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        try {
            recorder.prepare();
        } catch (IOException exception) {
            throw new RuntimeException();
        }
    }
    @MediumTest
    public void testPrepare() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "prepare()";
    }
}
