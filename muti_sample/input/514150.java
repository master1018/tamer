@TestTargetClass(JetPlayer.class)
public class JetPlayerTest extends AndroidTestCase {
    private OnJetEventListener mOnJetEventListener;
    private boolean mOnJetUserIdUpdateCalled;
    private boolean mOnJetPauseUpdateCalled;
    private boolean mOnJetNumQueuedSegmentUpdateCalled;
    private boolean mOnJetEventCalled;
    private String mJetFile;
    private Handler mHandler = new Handler();
    private final JetPlayer mJetPlayer = JetPlayer.getJetPlayer();
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mOnJetEventListener  = new MockOnJetEventListener();
        mJetFile =
            new File(Environment.getExternalStorageDirectory(), "test.jet").getAbsolutePath();
        assertTrue(JetPlayer.getMaxTracks() > 0);
    }
    @Override
    protected void tearDown() throws Exception {
        File jetFile = new File(mJetFile);
        if (jetFile.exists()) {
            jetFile.delete();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEventListener",
            args = {OnJetEventListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadJetFile",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "play",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "if call release , process will crash.",
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearQueue",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeJetFile",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMuteArray",
            args = {boolean[].class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMuteFlag",
            args = {int.class, boolean.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMuteFlags",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "queueJetSegment",
            args = {int.class, int.class, int.class, int.class, int.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "triggerClip",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMaxTracks",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getJetPlayer",
            args = {}
        )
    })
    public void testLoadJetFromPath() throws Throwable {
        mJetPlayer.clearQueue();
        prepareFile();
        mJetPlayer.setEventListener(mOnJetEventListener);
        mJetPlayer.loadJetFile(mJetFile);
        runJet();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEventListener",
            args = {OnJetEventListener.class, Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadJetFile",
            args = {AssetFileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "play",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "if call release , process will crash.",
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearQueue",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeJetFile",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMuteArray",
            args = {boolean[].class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMuteFlag",
            args = {int.class, boolean.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMuteFlags",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "queueJetSegment",
            args = {int.class, int.class, int.class, int.class, int.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "triggerClip",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMaxTracks",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getJetPlayer",
            args = {}
        )
    })
    public void testLoadJetFromFd() throws Throwable {
        mJetPlayer.clearQueue();
        mJetPlayer.setEventListener(mOnJetEventListener, mHandler);
        mJetPlayer.loadJetFile(mContext.getResources().openRawResourceFd(R.raw.test_jet));
        runJet();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "queueJetSegmentMuteArray",
        args = {int.class, int.class, int.class, int.class, boolean[].class, byte.class}
    )
    public void testQueueJetSegmentMuteArray() throws Throwable {
        mJetPlayer.clearQueue();
        mJetPlayer.setEventListener(mOnJetEventListener, mHandler);
        mJetPlayer.loadJetFile(mContext.getResources().openRawResourceFd(R.raw.test_jet));
        byte userID = 0;
        int segmentNum = 3;
        int libNum = -1;
        int repeatCount = 0;
        int transpose = 0;
        boolean[] muteFlags = new boolean[32];
        assertTrue(mJetPlayer.queueJetSegmentMuteArray(segmentNum, libNum, repeatCount, transpose,
                muteFlags, userID));
        assertTrue(mJetPlayer.play());
        for (int i = 0; i < muteFlags.length; i++) {
            muteFlags[i] = true;
        }
        muteFlags[8] = false;
        muteFlags[9] = false;
        muteFlags[10] = false;
        assertTrue(mJetPlayer.queueJetSegmentMuteArray(segmentNum, libNum, repeatCount, transpose,
                muteFlags, userID));
        Thread.sleep(20000);
        assertTrue(mJetPlayer.pause());
        assertTrue(mJetPlayer.clearQueue());
        assertFalse(mJetPlayer.play());
        assertTrue(mJetPlayer.closeJetFile());
    }
    private void runJet() throws Throwable {
        byte userID = 0;
        int segmentNum = 3;
        int libNum = -1;
        int repeatCount = 1;
        int transpose = 0;
        int muteFlags = 0;
        mJetPlayer.queueJetSegment(segmentNum, libNum, repeatCount, transpose, muteFlags, userID);
        segmentNum = 6;
        repeatCount = 1;
        transpose = -1;
        mJetPlayer.queueJetSegment(segmentNum, libNum, repeatCount, transpose, muteFlags, userID);
        segmentNum = 7;
        transpose = 0;
        mJetPlayer.queueJetSegment(segmentNum, libNum, repeatCount, transpose, muteFlags, userID);
        for (int i = 0; i < 7; i++) {
            assertTrue(mJetPlayer.triggerClip(i));
        }
        assertTrue(mJetPlayer.play());
        Thread.sleep(10000);
        assertTrue(mJetPlayer.pause());
        assertFalse(mJetPlayer.setMuteArray(new boolean[40], false));
        boolean[] muteArray = new boolean[32];
        for (int i = 0; i < muteArray.length; i++) {
            muteArray[i] = true;
        }
        muteArray[8] = false;
        muteArray[9] = false;
        muteArray[10] = false;
        assertTrue(mJetPlayer.setMuteArray(muteArray, true));
        Thread.sleep(1000);
        assertTrue(mJetPlayer.play());
        Thread.sleep(1000);
        assertTrue(mJetPlayer.setMuteFlag(9, true, true));
        Thread.sleep(1000);
        assertTrue(mJetPlayer.setMuteFlags(0, false));
        Thread.sleep(1000);
        assertTrue(mJetPlayer.setMuteFlags(0xffffffff, false));
        Thread.sleep(1000);
        assertTrue(mJetPlayer.setMuteFlags(0, false));
        Thread.sleep(30000);
        assertTrue(mJetPlayer.pause());
        assertTrue(mJetPlayer.closeJetFile());
        assertTrue(mOnJetEventCalled);
        assertTrue(mOnJetPauseUpdateCalled);
        assertTrue(mOnJetNumQueuedSegmentUpdateCalled);
        assertTrue(mOnJetUserIdUpdateCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clone",
        args = {}
    )
    public void testClone() throws Exception {
        try {
            mJetPlayer.clone();
            fail("should throw CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
        }
    }
    private void prepareFile() throws IOException {
        InputStream source = null;
        OutputStream target = null;
        try {
            source = mContext.getResources().openRawResource(R.raw.test_jet);
            target = new FileOutputStream(mJetFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) != -1) {
                target.write(buffer, 0, length);
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (target != null) {
                target.close();
            }
        }
    }
    private class MockOnJetEventListener implements OnJetEventListener {
        public void onJetEvent(JetPlayer player, short segment, byte track, byte channel,
                byte controller, byte value) {
            mOnJetEventCalled = true;
        }
        public void onJetNumQueuedSegmentUpdate(JetPlayer player, int nbSegments) {
            mOnJetNumQueuedSegmentUpdateCalled = true;
        }
        public void onJetPauseUpdate(JetPlayer player, int paused) {
            mOnJetPauseUpdateCalled = true;
        }
        public void onJetUserIdUpdate(JetPlayer player, int userId, int repeatCount) {
            mOnJetUserIdUpdateCalled = true;
        }
    }
}
