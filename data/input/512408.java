public class MediaRecorderSetOutputFormatStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInInitialState);
        assertTrue(stateErrors.errorInInitialStateAfterReset);
        assertTrue(stateErrors.errorInInitialStateAfterStop);
        assertTrue(stateErrors.errorInPreparedState);
        assertTrue(stateErrors.errorInRecordingState);
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(stateErrors.errorInDataSourceConfiguredState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.setOutputFormat(MediaRecorderStateUnitTestTemplate.OUTPUT_FORMAT);
    }
    @MediumTest
    public void testSetOutputFormat() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "setOutputFormat()";
    }
}
