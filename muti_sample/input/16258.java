public class Controller {
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
        for (int i = 0; i < 128; i++) {
            if(i == 0 || i == 32) continue;
            channel.controlChange(i, 10);
            assertEquals(channel.getController(i), 10);
            channel.controlChange(i, 100);
            assertEquals(channel.getController(i), 100);
        }
        soft.close();
    }
}
