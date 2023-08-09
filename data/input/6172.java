public class RC5ParameterSpecEquals {
    public static void main(String[] args) throws Exception {
        byte[] iv_1 = {
            (byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,
            (byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11
        };
        byte[] iv_2 = {
            (byte)0x22,(byte)0x22,(byte)0x22,(byte)0x22,
            (byte)0x22,(byte)0x22,(byte)0x22,(byte)0x22
        };
        RC5ParameterSpec rc_1 = new RC5ParameterSpec(1, 2, 3);
        RC5ParameterSpec rc_2 = new RC5ParameterSpec(1, 2, 3);
        if (!(rc_1.equals(rc_2)))
            throw new Exception("Should be equal");
        rc_1 = new RC5ParameterSpec(1, 2, 3, iv_1);
        rc_2 = new RC5ParameterSpec(1, 2, 3, iv_1);
        if (!(rc_1.equals(rc_2)))
            throw new Exception("Should be equal");
        rc_1 = new RC5ParameterSpec(1, 2, 32, iv_1);
        rc_2 = new RC5ParameterSpec(1, 2, 32, iv_2);
        if (rc_1.equals(rc_2))
            throw new Exception("Should be different");
    }
}
