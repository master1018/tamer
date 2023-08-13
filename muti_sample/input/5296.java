public class ClipSetPos {
    final static AudioFormat audioFormat = new AudioFormat(44100f, 16, 2, true, true);
    final static int frameLength = 44100 * 2; 
    final static byte[] dataBuffer
            = new byte[frameLength * (audioFormat.getSampleSizeInBits()/8)
                       * audioFormat.getChannels()];
    final static int MAX_FRAME_DELTA = 20;
    public static void main(String[] args) {
        boolean testPassed = true;
        Clip clip = null;
        try {
            clip = (Clip)AudioSystem.getLine(new DataLine.Info(Clip.class, audioFormat));
            clip.open(audioFormat, dataBuffer, 0, dataBuffer.length);
        } catch (LineUnavailableException ex) {
            log(ex);
            log("Cannot test (this is not failure)");
            return;
        } catch (IllegalArgumentException ex) {
            log(ex);
            log("Cannot test (this is not failure)");
            return;
        }
        log("clip: " + clip.getClass().getName());
        int len = clip.getFrameLength();
        for (int pos=0; pos < len; pos += (len /100)) {
            clip.setFramePosition(pos);
            int curPos = clip.getFramePosition();
            if (Math.abs(pos - curPos) > MAX_FRAME_DELTA) {
                log("Tried to set pos to " + pos + ", but got back " + curPos);
                testPassed = false;
            } else {
                log("Sucessfully set pos to " + pos);
            }
        }
        clip.close();
        if (testPassed) {
            log("Test PASSED.");
        } else {
            log("Test FAILED.");
            throw new RuntimeException("Test FAILED (see log)");
        }
    }
    static void log(String s) {
        System.out.println(s);
    }
    static void log(Exception ex) {
        ex.printStackTrace(System.out);
    }
}
