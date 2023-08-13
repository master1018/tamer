public class LatinCharReplacementTWTest {
    public static void  main(String[] args) throws Exception {
        final String bugID = "4658679";
        byte[] input = { (byte)0xa1,
                         (byte)0xf0,
                         (byte)'r',
                         (byte)'e',
                         (byte)'s',
                         (byte)0xe9,  
                         (byte)'r',
                         (byte)'v',
                         (byte)0xe9,  
                         (byte)'s',
                         (byte)0xa2,
                         (byte)0xf8
                       };
        char[] expected = { (char) 0xa7,
                         (char) 'r',
                         (char) 'e',
                         (char) 's',
                         (char) 0xFFFD,  
                         (char) 'r',
                         (char) 'v',
                         (char) 0xFFFD,  
                         (char) 's',
                         (char) 0xb0 };
        ByteArrayInputStream bais = new ByteArrayInputStream(input);
        InputStreamReader isr = new InputStreamReader(bais, "x-EUC-TW");
        char[] decoded = new char[128];
        int numChars = isr.read(decoded);
        if (numChars != expected.length) {
            throw new Exception("failure of test for bug " + bugID);
        }
        for (int i = 0 ; i < numChars; i++) {
           if (decoded[i] != expected[i])
                throw new Exception("failure of test for bug " + bugID);
        }
    }
}
