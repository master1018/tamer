public class EucJpLinuxDecoderRecoveryTest {
    public static void main(String[] args) throws Exception {
        byte[] encoded = {
                (byte)0xa6, (byte)0xc5,
                (byte)0x8f, (byte)0xa2, (byte)0xb7,
                (byte)0xa6, (byte)0xc7 };
        char[] decodedChars = new char[3];
        char[] expectedChars =
                        {
                        '\u03B5',  
                        '\ufffd',  
                        '\u03B7'   
                        };
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
        InputStreamReader isr = new InputStreamReader(bais, "EUC_JP_LINUX");
        int n = 0;   
        try {
            n = isr.read(decodedChars);
        } catch (Exception ex) {
            throw new Error("euc-jp-linux decoding broken");
        }
        if (n != expectedChars.length)
            throw new Error("Unexpected number of chars decoded");
        for (int i = 0; i < n; i++) {
            if (expectedChars[i] != decodedChars[i])
                throw new Error("euc-jp-linux decoding incorrect");
        }
    }
}
