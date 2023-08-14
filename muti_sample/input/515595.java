public class SoundTest extends TestCase {
  public static String TAG = "SoundTest";
    public void testMidiSupport() {
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            Assert.assertTrue("AndroidSequencer must exist", sequencer instanceof AndroidSequencer);
            MidiDevice.Info info = sequencer.getDeviceInfo();
            Assert.assertNotNull("Device info must exist", info);
            Sequence sequence = MidiSystem.getSequence(new File("/system/sounds/test.mid"));
            Assert.assertNotNull("Sequence must exist", sequence);
            Assert.assertFalse("Sequencer must not be open", sequencer.isOpen());
            sequencer.open();
            Assert.assertTrue("Sequencer must be open", sequencer.isOpen());
            Assert.assertNull("Sequencer must not have Sequence", sequencer.getSequence());
            sequencer.setSequence(sequence);
            Assert.assertNotNull("Sequencer must have Sequence", sequencer.getSequence());
            Assert.assertFalse("Sequencer must not be running", sequencer.isRunning());
            sequencer.start();
            Thread.sleep(1000);
            Assert.assertTrue("Sequencer must be running (after 1 second)", sequencer.isRunning());
            Thread.sleep(3000);
            Assert.assertTrue("Sequencer must be running", sequencer.isRunning());
            sequencer.stop();
            Thread.sleep(1000);
            Assert.assertFalse("Sequencer must not be running (after 1 second)", sequencer.isRunning());
            sequencer.close();
            Assert.assertFalse("Sequencer must not be open", sequencer.isOpen());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 
    public void testSampledSupport() {
        try {
            Clip clip = AudioSystem.getClip();
            Assert.assertTrue("AndroidClip must exist", clip instanceof AndroidClip);
            Line.Info info = clip.getLineInfo();
            Assert.assertNotNull("Line info must exist", info);
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File("/system/media/audio/ringtones/ringer.ogg"));
            Assert.assertNotNull("AudioInputStream must exist", stream);
            Assert.assertFalse("Clip must not be open", clip.isOpen());
            clip.open(stream);
            Assert.assertTrue("Clip must be open", clip.isOpen());
            Assert.assertFalse("Clip must not be running", clip.isRunning());
            clip.start();
            Thread.sleep(1000);
            Assert.assertTrue("Clip must be running (after 1 second)", clip.isRunning());
            Thread.sleep(2000);
            Assert.assertTrue("Clip must be running", clip.isRunning());
            clip.stop();
            Thread.sleep(1000);
            Assert.assertFalse("Clip must not be running (after 1 second)", clip.isRunning());
            clip.close();
            Assert.assertFalse("Clip must not be open", clip.isOpen());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 
}
