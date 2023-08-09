public class Send_Omni {
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
        smsg.setMessage(ShortMessage.CONTROL_CHANGE,0, 125,0);
        receiver.send(smsg, -1);
        assertEquals(channel.getOmni(), false);
        smsg.setMessage(ShortMessage.CONTROL_CHANGE,0, 124,0);
        receiver.send(smsg, -1);
        assertEquals(channel.getOmni(), false);
        channel.noteOn(60, 64);
        soft.read(1);
        assertTrue(soft.findVoice(0,60) != null);
        smsg.setMessage(ShortMessage.CONTROL_CHANGE,0, 124,0);
        receiver.send(smsg, -1);
        soft.read(1);
        assertTrue(soft.findVoice(0,60) == null);
        soft.close();
    }
}
