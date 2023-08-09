public class ProgramChange {
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
        channel.programChange(36);
        assertEquals(channel.getProgram(), 36);
        channel.programChange(48);
        assertEquals(channel.getProgram(), 48);
        soft.close();
    }
}
