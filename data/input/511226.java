public class MediaPlayerGetDurationStateUnitTest extends AndroidTestCase implements MediaPlayerMethodUnderTest {
    private MediaPlayerStateUnitTestTemplate mTestTemplate = new MediaPlayerStateUnitTestTemplate();
    public void checkStateErrors(MediaPlayerStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInPreparedStateAfterStop);
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInStartedState);
        assertTrue(!stateErrors.errorInStartedStateAfterPause);
        assertTrue(!stateErrors.errorInPausedState);
        assertTrue(!stateErrors.errorInStoppedState);
        assertTrue(!stateErrors.errorInPlaybackCompletedState);
        assertTrue(!stateErrors.errorInIdleState);  
        assertTrue(stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(stateErrors.errorInIdleStateAfterReset);
    }
    public void invokeMethodUnderTest(MediaPlayer player) {
        player.getDuration();
    }
    @LargeTest
    public void testGetDuration() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "getDuration()";
    }
}
