public class NoteOff {
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
        SoftTestUtils soft = new SoftTestUtils();
        MidiChannel channel = soft.synth.getChannels()[0];
        channel.noteOn(60, 64);
        soft.read(1);
        VoiceStatus[] v = soft.synth.getVoiceStatus();
        assertEquals(v[0].note, 60);
        assertEquals(v[0].active, true);
        channel.noteOff(60);
        soft.read(1);
        v = soft.synth.getVoiceStatus();;
        assertEquals(v[0].active, false);
        soft.close();
    }
}
