public class PolyPressure {
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
            channel.setPolyPressure(i, 10);
            assertEquals(channel.getPolyPressure(i),10);
            channel.setPolyPressure(i, 100);
            assertEquals(channel.getPolyPressure(i),100);
        }
        soft.close();
    }
}
