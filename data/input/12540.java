public class Send_PolyPressure {
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
        for (int i = 0; i < 128; i++) {
            smsg.setMessage(ShortMessage.POLY_PRESSURE,0, i, 10);
            receiver.send(smsg, -1);
            assertEquals(channel.getPolyPressure(i),10);
            smsg.setMessage(ShortMessage.POLY_PRESSURE,0, i, 100);
            receiver.send(smsg, -1);
            assertEquals(channel.getPolyPressure(i),100);
        }
        soft.close();
    }
}
