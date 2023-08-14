public class MediaPlayerGetCurrentPositionStateUnitTest extends AndroidTestCase implements MediaPlayerMethodUnderTest {
    private MediaPlayerStateUnitTestTemplate mTestTemplate = new MediaPlayerStateUnitTestTemplate();
    public void checkStateErrors(MediaPlayerStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInInitializedState);
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInPreparedStateAfterStop);
        assertTrue(!stateErrors.errorInStartedState);
        assertTrue(!stateErrors.errorInStartedStateAfterPause);
        assertTrue(!stateErrors.errorInPausedState);
        assertTrue(!stateErrors.errorInStoppedState);
        assertTrue(!stateErrors.errorInPlaybackCompletedState);
        assertTrue(!stateErrors.errorInIdleStateAfterReset);
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(!stateErrors.errorInIdleState);  
    }
    public void invokeMethodUnderTest(MediaPlayer player) {
        player.getCurrentPosition();
    }
    @LargeTest
    public void testGetCurrentPosition() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "getCurrentPosition()";
    }
}
