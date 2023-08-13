public class Load7 {
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
        SoftTuning tuning = new SoftTuning();
        int[] msg = {0xf0,0x7f,0x7f,0x08,0x07,0x00,0x00,0x02,
                36,36,64,0,
                40,70,0,0,
                0xf7};
        byte[] bmsg = new byte[msg.length];
        for (int i = 0; i < bmsg.length; i++)
            bmsg[i] = (byte)msg[i];
        tuning.load(bmsg);
        double[] tunings = tuning.getTuning();
        for (int i = 0; i < tunings.length; i++) {
            if(i == 36)
                assertTrue(Math.abs(tunings[i]-3650)< 0.00001);
            else if(i == 40)
                assertTrue(Math.abs(tunings[i]-7000) < 0.00001);
            else
                assertTrue(Math.abs(tunings[i]-i*100) < 0.00001);
        }
    }
}
