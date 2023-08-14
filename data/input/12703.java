public class Load6 {
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
        int[] msg = {0xf0,0x7e,0x7f,0x08,0x06,0,0,
                name[0],name[1],name[2],name[3],name[4],name[5],name[6],
                name[7],name[8],name[9],name[10],name[11],name[12],name[13],
                name[14],name[15],
                5,10,15,20,25,30,35,40,45,50,51,52,
                5,10,15,20,25,30,35,40,45,50,51,52,
                0,0xf7};
        int x = msg[1] & 0xFF;
        for (int i = 2; i < msg.length - 2; i++)
            x = x ^ (msg[i] & 0xFF);
        msg[msg.length-2] = (x & 127);
        int[] oct = {5,10,15,20,25,30,35,40,45,50,51,52,5,10,15,20,25,30,35,40,45,50,51,52};
        byte[] bmsg = new byte[msg.length];
        for (int i = 0; i < bmsg.length; i++)
            bmsg[i] = (byte)msg[i];
        tuning.load(bmsg);
        double[] tunings = tuning.getTuning();
        for (int i = 0; i < tunings.length; i++)
        {
            double c = (oct[(i%12)*2]*128 + oct[(i%12)*2+1] -8192)*(100.0/8192.0);
            assertTrue(Math.abs(tunings[i]-(i*100 + (c))) < 0.00001);
        }
        msg[msg.length - 2] += 10;
        for (int i = 0; i < bmsg.length; i++)
            bmsg[i] = (byte)msg[i];
        tuning = new SoftTuning();
        tuning.load(bmsg);
        assertTrue(!tuning.getName().equals("Testing123      "));
    }
}
