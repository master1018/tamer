public class MediaPlayerSetVolumeStateUnitTest extends AndroidTestCase implements MediaPlayerMethodUnderTest {
    private MediaPlayerStateUnitTestTemplate mTestTemplate = new MediaPlayerStateUnitTestTemplate();
    public void checkStateErrors(MediaPlayerStateErrors stateErrors) {
        assertTrue(!stateErrors.errorInStartedState);
        assertTrue(!stateErrors.errorInStartedStateAfterPause);
        assertTrue(!stateErrors.errorInPausedState);
        assertTrue(!stateErrors.errorInPreparedState);
        assertTrue(!stateErrors.errorInPreparedStateAfterStop);
        assertTrue(!stateErrors.errorInPlaybackCompletedState);
        assertTrue(!stateErrors.errorInIdleStateAfterReset);
        assertTrue(!stateErrors.errorInInitializedState);
        assertTrue(!stateErrors.errorInStoppedState);
        assertTrue(!stateErrors.errorInIdleState);
        assertTrue(stateErrors.errorInErrorState);
    }
    public void invokeMethodUnderTest(MediaPlayer player) {
        player.setVolume(0.5f, 0.5f);
    }
    @LargeTest
    public void testSetVolume() {
        mTestTemplate.runTestOnMethod(this);
    }
    @Override
    public String toString() {
        return "setVolume()";
    }
}
