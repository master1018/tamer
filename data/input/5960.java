public class GetMicrosecondPosition {
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
        AudioInputStream stream = synth.openStream(null, null);
        assertTrue(synth.getMicrosecondPosition() == 0);
        AudioFormat format = stream.getFormat();
        byte[] buff = new byte[((int)format.getFrameRate())*format.getFrameSize()];;
        stream.read(buff);
        assertTrue(Math.abs(synth.getMicrosecondPosition()-1000000) < 10000);
        synth.close();
    }
}
