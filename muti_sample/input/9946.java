public class Send_ProgramChange {
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
        Receiver receiver = soft.synth.getReceiver();
        ShortMessage smsg = new ShortMessage();
        smsg.setMessage(ShortMessage.PROGRAM_CHANGE,0, 36,0);
        receiver.send(smsg, -1);
        assertEquals(channel.getProgram(), 36);
        smsg.setMessage(ShortMessage.PROGRAM_CHANGE,0, 48,0);
        receiver.send(smsg, -1);
        assertEquals(channel.getProgram(), 48);
        soft.close();
    }
}
