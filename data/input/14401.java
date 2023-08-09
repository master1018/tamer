public class Open {
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
        Field f = SoftSynthesizer.class.getDeclaredField("testline");
        f.setAccessible(true);
        f.set(null, new DummySourceDataLine());
        AudioSynthesizer synth = new SoftSynthesizer();
        synth.open();
        assertTrue(synth.isOpen());
        synth.close();
    }
}
