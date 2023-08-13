public class GetReceiver2 {
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        Receiver recv = synth.getReceiver();
        assertTrue(recv != null);
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_OFF, 0, 64, 64);
        synth.open(new DummySourceDataLine(), null);
        recv.send(sm, -1);
        synth.close();
        try
        {
            recv.send(sm, -1);
            throw new RuntimeException("Exception not thrown!");
        }
        catch(Exception e)
        {
        }
    }
}
