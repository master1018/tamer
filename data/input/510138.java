public class MediaRecorderSetOutputFileStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInDataSourceConfiguredState);
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInRecordingState);
        assertTrue(!stateErrors.errorInErrorState);
        assertTrue(!stateErrors.errorInInitialState);
        assertTrue(!stateErrors.errorInInitialStateAfterReset);
        assertTrue(!stateErrors.errorInInitialStateAfterStop);
        assertTrue(!stateErrors.errorInInitializedState);
    }
    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.setOutputFile(MediaRecorderStateUnitTestTemplate.RECORD_OUTPUT_PATH);
    }
    @MediumTest
    public void testSetOutputFile() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "setOutputFile()";
    }
}
