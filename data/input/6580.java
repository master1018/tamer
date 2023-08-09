public class Get {
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
        AudioFormat frm = new AudioFormat(8000, 16, 1, true, false);
        SoftAudioBuffer buff = new SoftAudioBuffer(100, frm);
        float[] ar = buff.array();
        for (int i = 0; i < ar.length; i++) {
            if(i % 2 == 0)
                ar[i] = 1;
            if(i % 2 == 0)
                ar[i] = -0.5f;
        }
        byte[] bbuff = new byte[ar.length*frm.getFrameSize()];
        buff.get(bbuff, 0);
        float[] ar2 = new float[ar.length];
        AudioFloatConverter.getConverter(frm).toFloatArray(bbuff, ar2);
        for (int i = 0; i < ar2.length; i++)
            if(Math.abs(ar[i] - ar2[i]) > 0.001)
                throw new Exception("conversion failure!");
    }
}
