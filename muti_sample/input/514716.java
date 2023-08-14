public class MusicPlayerStability extends ActivityInstrumentationTestCase2 <TrackBrowserActivity>{
    private static String TAG = "musicplayerstability";
    private static int PLAY_TIME = 30000;
    private ListView mTrackList;
    public MusicPlayerStability() {
        super("com.android.music",TrackBrowserActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        getActivity();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @LargeTest
    public void testPlay30sMP3() throws Exception {
        try {
            Instrumentation inst = getInstrumentation();
            Thread.sleep(2000);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            mTrackList = getActivity().getListView();
            int scrollCount = mTrackList.getMaxScrollAmount();
            if (scrollCount != -1) {
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            } else {
                assertTrue("testPlayMP3", false);
            }
            Thread.sleep(PLAY_TIME);
        } catch (Exception e) {
            assertTrue("testPlayMP3", false);
        }
    }
    @LargeTest
    public void testLaunchMusicPlayer() throws Exception {
        try {
            Thread.sleep(PLAY_TIME);
        } catch (Exception e) {
            assertTrue("MusicPlayer Do Nothing", false);
        }
        assertTrue("MusicPlayer Do Nothing", true);
    }
}
