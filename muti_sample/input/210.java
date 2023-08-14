public class RC2ParameterSpecEquals {
    public static void main(String[] args) throws Exception {
        byte[] iv_1 = {
            (byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,
            (byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,
            (byte)0x33,(byte)0x33
        };
        byte[] iv_2 = {
            (byte)0x22,(byte)0x22,(byte)0x22,(byte)0x22,
            (byte)0x22,(byte)0x22,(byte)0x22,(byte)0x22,
        };
        RC2ParameterSpec rc_1 = new RC2ParameterSpec(2);
        RC2ParameterSpec rc_2 = new RC2ParameterSpec(2);
        if (!(rc_1.equals(rc_2)))
            throw new Exception("Should be equal");
        RC2ParameterSpec rc_3 = new RC2ParameterSpec(2, iv_1);
        RC2ParameterSpec rc_4 = new RC2ParameterSpec(2, iv_1);
        if (!(rc_3.equals(rc_4)))
            throw new Exception("Should be equal");
        RC2ParameterSpec rc_5 = new RC2ParameterSpec(2, iv_2);
        if (rc_3.equals(rc_5))
            throw new Exception("Should be different");
        RC2ParameterSpec rc_6 = new RC2ParameterSpec(2, iv_1, 2);
        if (rc_3.equals(rc_6))
            throw new Exception("Should be different");
    }
}
