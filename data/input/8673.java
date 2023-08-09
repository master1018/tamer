public class InvalidIV {
    public static void main(String[] args) throws Exception {
        byte[] iv_1 = {
            (byte)0x11
        };
        byte[] iv_2 = {
            (byte)0x22,(byte)0x22,(byte)0x22,(byte)0x22
        };
        try {
            RC5ParameterSpec rc5Params = new RC5ParameterSpec(1, 2, 8, iv_1);
            throw new Exception("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}
        try {
            RC5ParameterSpec rc5Params = new RC5ParameterSpec(1, 2, 8, iv_2, 3);
            throw new Exception("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}
    }
}
