public class MediaPlayerPauseStateUnitTest extends AndroidTestCase implements MediaPlayerMethodUnderTest {
    private MediaPlayerStateUnitTestTemplate mTestTemplate = new MediaPlayerStateUnitTestTemplate();
    public void checkStateErrors(MediaPlayerStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInStartedState);
        assertTrue(!stateErrors.errorInStartedStateAfterPause);
        assertTrue(!stateErrors.errorInPausedState);
        assertTrue(stateErrors.errorInPreparedState);
        assertTrue(stateErrors.errorInPreparedStateAfterStop);
        assertTrue(stateErrors.errorInPlaybackCompletedState);
        assertTrue(!stateErrors.errorInIdleState);  
        assertTrue(stateErrors.errorInIdleStateAfterReset);
        assertTrue(stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInStoppedState);
        assertTrue(stateErrors.errorInErrorState);
    }
    public void invokeMethodUnderTest(MediaPlayer player) {
        player.pause();
    }
    @LargeTest
    public void testPause() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "pause()";
    }
}
