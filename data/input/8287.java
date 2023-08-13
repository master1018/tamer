public class HWKatakanaMS932EncodeTest {
   public static void main(String[] args) throws Exception {
        char[] testChars = new char[1];
        byte[] testBytes = new byte[1];
        int offset = 0;
        String encoding = "windows-31j";
        for (int lsByte = 0x61 ; lsByte <= 0x9F; lsByte++, offset++) {
            testChars[0] = (char) (lsByte | 0xFF00);
            String s = new String(testChars);
            testBytes = s.getBytes(encoding);
            if ( testBytes[0] != (byte)(0xa1 + offset))
                throw new Exception("failed Test");
        }
    }
}
