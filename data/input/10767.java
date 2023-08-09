public class GetAvailableInstruments2 {
    private static void assertEquals(Object a, Object b) throws Exception {
        if (!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    private static void assertTrue(boolean value) throws Exception {
        if (!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        synth.openStream(null, null);
        Soundbank defsbk = synth.getDefaultSoundbank();
        if (defsbk != null) {
            synth.unloadAllInstruments(defsbk);
            assertTrue(defsbk.getInstruments().length == synth
                    .getAvailableInstruments().length);
        }
        synth.close();
    }
}
