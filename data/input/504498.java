@TestTargetClass(ToneGenerator.class)
public class ToneGeneratorTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ToneGenerator",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startTone",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopTone",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        )
    })
    public void testSyncGenerate() throws Exception {
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_RING,
                                                  ToneGenerator.MAX_VOLUME);
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2);
        final long DELAYED = 1000;
        Thread.sleep(DELAYED);
        toneGen.stopTone();
        toneGen.release();
    }
}
