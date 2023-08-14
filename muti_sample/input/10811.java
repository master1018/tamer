public class TestUtility {
    private static final String digits = "0123456789abcdef";
    public TestUtility() {
    }
    public static String hexDump(byte[] bytes) {
        StringBuffer buf = new StringBuffer (bytes.length * 2);
        int  i;
        buf.append ("    ");                    
        for (i = 0; i < bytes.length; i++) {
            buf.append (digits.charAt ((bytes[i] >> 4) & 0x0f));
            buf.append (digits.charAt (bytes[i] & 0x0f));
            if (((i + 1) % 32) == 0) {
                if ((i +  1) != bytes.length)
                    buf.append ("\n    ");      
            } else if (((i + 1) % 4) == 0)
                buf.append (' ');               
        }
        return buf.toString ();
    }
    public static boolean equalsBlock(byte[] b1, byte[] b2) {
        if (b1.length != b2.length)
            return false;
        for (int i=0; i<b1.length; i++) {
            if (b1[i] != b2[i])
                return false;
        }
        return true;
    }
    public static boolean equalsBlock(byte[] b1, byte[] b2, int len) {
        for (int i=0; i<len; i++) {
            if (b1[i] != b2[i])
                return false;
        }
        return true;
    }
}
