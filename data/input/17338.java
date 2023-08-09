public class GetFormat {
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
        AudioSynthesizer synth = new SoftSynthesizer();
        AudioFormat defformat = synth.getFormat();
        assertTrue(defformat != null);
        synth.openStream(null, null);
        assertTrue(synth.getFormat().toString().equals(defformat.toString()));
        synth.close();
        AudioFormat custformat = new AudioFormat(8000, 16, 1, true, false);
        synth.openStream(custformat, null);
        assertTrue(synth.getFormat().toString().equals(custformat.toString()));
        synth.close();
    }
}
