public class Send_ActiveSense {
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
    public static void sendActiveSens(Receiver r) throws Exception
    {
        ShortMessage smsg = new ShortMessage();
        smsg.setMessage(ShortMessage.ACTIVE_SENSING);
        r.send(smsg, -1);
    }
    public static void main(String[] args) throws Exception {
        SoftTestUtils soft = new SoftTestUtils();
        MidiChannel channel = soft.synth.getChannels()[0];
        Receiver receiver = soft.synth.getReceiver();
        sendActiveSens(receiver);
        sendActiveSens(receiver);
        channel.noteOn(60, 64);
        assertTrue(soft.findVoice(0,60) != null);
        for (int i = 0; i < 10; i++) {
            soft.read(0.2); 
            sendActiveSens(receiver);
            assertTrue(soft.findVoice(0,60) != null);
        }
        soft.read(2);
        assertTrue(soft.findVoice(0,60) == null);
        soft.close();
    }
}
