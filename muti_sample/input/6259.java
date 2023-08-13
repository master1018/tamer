public class PitchBend {
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
        channel.setPitchBend(10);
        assertEquals(channel.getPitchBend(), 10);
        channel.setPitchBend(9000);
        assertEquals(channel.getPitchBend(), 9000);
        soft.close();
    }
}
