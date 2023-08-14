public class Load1 {
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
        byte[] name;
        name = "Testing123      ".getBytes("ascii");
        int[] msg = new int[24+3*128];
        int[] head = {0xf0,0x7e,0x7f,0x08,0x01,0x00};
        int ox = 0;
        for (int i = 0; i < head.length; i++)
            msg[ox++] = head[i];
        for (int i = 0; i < name.length; i++)
            msg[ox++] = name[i];
        for (int i = 0; i < 128; i++) {
            msg[ox++] = i;
            msg[ox++] = 64;
            msg[ox++] = 0;
        }
        int x = msg[1] & 0xFF; 
        x = x ^ (msg[2] & 0xFF); 
        x = x ^ (msg[4] & 0xFF); 
        x = x ^ (msg[5] & 0xFF); 
        for (int i = 22; i < msg.length - 2; i++)
            x = x ^ (msg[i] & 0xFF);
        msg[ox++] = (x & 127);
        msg[ox++] = 0xf7;
        byte[] bmsg = new byte[msg.length];
        for (int i = 0; i < bmsg.length; i++)
            bmsg[i] = (byte)msg[i];
        tuning.load(bmsg);
        assertEquals(tuning.getName(), "Testing123      ");
        double[] tunings = tuning.getTuning();
        for (int i = 0; i < tunings.length; i++)
            assertTrue(Math.abs(tunings[i]-(i*100 + 50)) < 0.00001);
    }
}
