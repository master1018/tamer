public class MediaPlayerStartStateUnitTest extends AndroidTestCase implements MediaPlayerMethodUnderTest {
    private MediaPlayerStateUnitTestTemplate mTestTemplate = new MediaPlayerStateUnitTestTemplate();
    public void checkStateErrors(MediaPlayerStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInPreparedStateAfterStop);
        assertTrue(!stateErrors.errorInStartedState);
        assertTrue(!stateErrors.errorInStartedStateAfterPause);
        assertTrue(!stateErrors.errorInPausedState);
        assertTrue(!stateErrors.errorInPlaybackCompletedState);
        assertTrue(!stateErrors.errorInIdleState);  
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(stateErrors.errorInIdleStateAfterReset);
        assertTrue(stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInStoppedState);
    }
    public void invokeMethodUnderTest(MediaPlayer player) {
        player.start();
    }
    @LargeTest
    public void testStart() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "start()";
    }
}
