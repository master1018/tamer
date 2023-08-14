public class MediaAudioManagerTest extends ActivityInstrumentationTestCase2<MediaFrameworkTest> {
    private String TAG = "MediaAudioManagerTest";
    private AudioManager mAudioManager;
    private int[] ringtoneMode = {AudioManager.RINGER_MODE_NORMAL,
             AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE};
    public MediaAudioManagerTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
     }
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
     public boolean validateSetRingTone(int i) {
         int getRingtone = mAudioManager.getRingerMode();
         if (i != getRingtone)
             return false;
         else
             return true;
     }
     @MediumTest
     public void testSetRingtoneMode() throws Exception {
         boolean result = false;
         for (int i = 0; i < ringtoneMode.length; i++) {
             mAudioManager.setRingerMode(ringtoneMode[i]);
             result = validateSetRingTone(ringtoneMode[i]);
             assertTrue("SetRingtoneMode : " + ringtoneMode[i], result);
         }
     }
 }