public class PaddedBitString {
    private final static byte[] DER_BITSTRING_PAD6 = { 3, 3, 6,
                                                   (byte)0x5d, (byte)0xcb };
    private final static byte[] DER_BITSTRING_NOPAD = { 3, 3, 0,
                                                   (byte)0x5d, (byte)0xc0 };
    public static void main(String args[]) throws Exception {
        byte[] ba0, ba1;
        try {
            DerInputStream derin = new DerInputStream(DER_BITSTRING_PAD6);
            ba1 = derin.getBitString();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new Exception("Unable to parse BitString with 6 padding bits");
        }
        try {
            DerInputStream derin = new DerInputStream(DER_BITSTRING_NOPAD);
            ba0 = derin.getBitString();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new Exception("Unable to parse BitString with no padding");
        }
        if (Arrays.equals(ba1, ba0) == false ) {
            throw new Exception("BitString comparison check failed");
        }
    }
}
