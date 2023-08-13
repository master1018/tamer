public class FindASCIICodingBugs {
    private static int failures = 0;
    private static void check(boolean condition) {
        if (! condition) {
            new Error("test failed").printStackTrace();
            failures++;
        }
    }
    private static boolean equals(byte[] ba, ByteBuffer bb) {
        if (ba.length != bb.limit())
            return false;
        for (int i = 0; i < ba.length; i++)
            if (ba[i] != bb.get(i))
                return false;
        return true;
    }
    public static void main(String[] args) throws Exception {
        for (Map.Entry<String,Charset> e
                 : Charset.availableCharsets().entrySet()) {
            String csn = e.getKey();
            Charset cs = e.getValue();
            if (csn.equals("x-JIS0208"))      continue; 
            if (csn.equals("JIS_X0212-1990")) continue; 
            if (! cs.canEncode()) continue;
            CharsetEncoder enc = cs.newEncoder();
            CharsetDecoder dec = cs.newDecoder();
            if (! enc.canEncode('A')) continue;
            System.out.println(csn);
            try {
                byte[] bytes1 = "A".getBytes(csn);
                ByteBuffer bb = enc.encode(CharBuffer.wrap(new char[]{'A'}));
                check(equals(bytes1, bb));
                check(new String(bytes1, csn).equals("A"));
                CharBuffer cb = dec.decode(bb);
                check(cb.toString().equals("A"));
            } catch (Throwable t) {
                t.printStackTrace();
                failures++;
            }
        }
        if (failures > 0)
            throw new Exception(failures + "tests failed");
    }
}
