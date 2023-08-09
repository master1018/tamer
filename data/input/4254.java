public class GetChannels {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        synth.openStream(null, null);
        assertTrue(synth.getChannels() != null);
        assertTrue(synth.getChannels().length == 16);
        MidiChannel[] channels = synth.getChannels();
        for (int i = 0; i < channels.length; i++) {
            assertTrue(channels[i] != null);
        }
        synth.close();
    }
}
