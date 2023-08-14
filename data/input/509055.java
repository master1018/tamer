@TestTargetClass(AsyncPlayer.class)
public class AsyncPlayerTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AsyncPlayer",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "play",
            args = {Context.class, Uri.class, boolean.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        )
    })
    public void testAsyncPlayer() throws Exception {
        final Uri PLAY_URI = Settings.System.DEFAULT_NOTIFICATION_URI;
        AsyncPlayer asyncPlayer = new AsyncPlayer(null);
        asyncPlayer.play(getContext(), PLAY_URI, true, AudioManager.STREAM_RING);
        final int PLAY_TIME = 3000;
        Thread.sleep(PLAY_TIME);
        asyncPlayer.stop();
    }
}
