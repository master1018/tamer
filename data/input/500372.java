@TestTargetClass(RingtoneManager.class)
public class RingtoneManagerTest
        extends ActivityInstrumentationTestCase2<RingtonePickerActivity> {
    private static final String PKG = "com.android.cts.stub";
    private RingtonePickerActivity mActivity;
    private Instrumentation mInstrumentation;
    private Context mContext;
    private RingtoneManager mRingtoneManager;
    private AudioManager mAudioManager;
    private int mOriginalVolume;
    private Uri mDefaultUri;
    public RingtoneManagerTest() {
        super(PKG, RingtonePickerActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mContext = mInstrumentation.getContext();
        mRingtoneManager = new RingtoneManager(mActivity);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mDefaultUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                RingtoneManager.TYPE_RINGTONE);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume / 2,
                AudioManager.FLAG_ALLOW_RINGER_MODES);
    }
    @Override
    protected void tearDown() throws Exception {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mOriginalVolume,
                    AudioManager.FLAG_ALLOW_RINGER_MODES);
        }
        RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE,
                mDefaultUri);
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RingtoneManager",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RingtoneManager",
            args = {Context.class}
        )
    })
    public void testConstructors() {
        new RingtoneManager(mActivity);
        new RingtoneManager(mContext);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIncludeDrm",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIncludeDrm",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRingtoneUri",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRingtone",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRingtone",
            args = {Context.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCursor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRingtonePosition",
            args = {Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getActualDefaultRingtoneUri",
            args = {Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setActualDefaultRingtoneUri",
            args = {Context.class, int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultType",
            args = {Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getValidRingtoneUri",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDefault",
            args = {Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultUri",
            args = {int.class}
        )
    })
    public void testAccessMethods() {
        Cursor c = mRingtoneManager.getCursor();
        assertTrue("Must have at least one ring tone available", c.getCount() > 0);
        mRingtoneManager.setIncludeDrm(true);
        assertTrue(mRingtoneManager.getIncludeDrm());
        mRingtoneManager.setIncludeDrm(false);
        assertFalse(mRingtoneManager.getIncludeDrm());
        assertNotNull(mRingtoneManager.getRingtone(0));
        assertNotNull(RingtoneManager.getRingtone(mContext, Settings.System.DEFAULT_RINGTONE_URI));
        int expectedPosition = 0;
        Uri uri = mRingtoneManager.getRingtoneUri(expectedPosition);
        assertEquals(expectedPosition, mRingtoneManager.getRingtonePosition(uri));
        assertNotNull(RingtoneManager.getValidRingtoneUri(mContext));
        RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, uri);
        assertEquals(uri, RingtoneManager.getActualDefaultRingtoneUri(mContext,
                RingtoneManager.TYPE_RINGTONE));
        assertEquals(Settings.System.DEFAULT_RINGTONE_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        assertEquals(Settings.System.DEFAULT_NOTIFICATION_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        assertEquals(RingtoneManager.TYPE_RINGTONE,
                RingtoneManager.getDefaultType(Settings.System.DEFAULT_RINGTONE_URI));
        assertEquals(RingtoneManager.TYPE_NOTIFICATION,
                RingtoneManager.getDefaultType(Settings.System.DEFAULT_NOTIFICATION_URI));
        assertTrue(RingtoneManager.isDefault(Settings.System.DEFAULT_RINGTONE_URI));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setType",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "inferStreamType",
            args = {}
        )
    })
    public void testSetType() {
        mRingtoneManager.setType(RingtoneManager.TYPE_ALARM);
        assertEquals(AudioManager.STREAM_ALARM, mRingtoneManager.inferStreamType());
        Cursor c = mRingtoneManager.getCursor();
        assertTrue("Must have at least one alarm tone available", c.getCount() > 0);
        Ringtone r = mRingtoneManager.getRingtone(0);
        assertEquals(RingtoneManager.TYPE_ALARM, r.getStreamType());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopPreviousRingtone",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStopPreviousRingtone",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStopPreviousRingtone",
            args = {}
        )
    })
    public void testStopPreviousRingtone() {
        Cursor c = mRingtoneManager.getCursor();
        assertTrue("Must have at least one ring tone available", c.getCount() > 0);
        mRingtoneManager.setStopPreviousRingtone(true);
        assertTrue(mRingtoneManager.getStopPreviousRingtone());
        Uri uri = Uri.parse("android.resource:
        Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
        ringtone.play();
        assertTrue(ringtone.isPlaying());
        ringtone.stop();
        assertFalse(ringtone.isPlaying());
        Ringtone newRingtone = mRingtoneManager.getRingtone(0);
        assertFalse(ringtone.isPlaying());
        newRingtone.play();
        assertTrue(newRingtone.isPlaying());
        mRingtoneManager.stopPreviousRingtone();
        assertFalse(newRingtone.isPlaying());
    }
}
