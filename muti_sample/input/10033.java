public class Send_ChannelPressure {
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
        smsg.setMessage(ShortMessage.CHANNEL_PRESSURE,0, 10,0);
        receiver.send(smsg, -1);
        assertEquals(channel.getChannelPressure(), 10);
        smsg.setMessage(ShortMessage.CHANNEL_PRESSURE,0, 90,0);
        receiver.send(smsg, -1);
        assertEquals(channel.getChannelPressure(), 90);
        soft.close();
    }
}
