public class SJISPropTest {
    public static void main(String[] args) throws Exception {
        boolean sjisIsMS932 = false;
        if (args[0].equals("MS932"))
                sjisIsMS932 = true;
        byte[] testBytes = { (byte)0x81, (byte)0x60 };
        String expectedMS932 = new String("\uFF5E");
        String expectedSJIS = new String("\u301C");
        String s = new String(testBytes, "shift_jis");
        if (sjisIsMS932 && !s.equals(expectedMS932))
            throw new Exception("not MS932");
        else if (!sjisIsMS932 && !s.equals(expectedSJIS))
            throw new Exception("not SJIS");
    }
}
