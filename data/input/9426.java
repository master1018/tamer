public class Encodings {
    static boolean equals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i]) return false;
        return true;
    }
    static void go(String enc, String str, final byte[] bytes, boolean bidir)
        throws Exception
    {
        final Charset charset = Charset.forName(enc);
        if (!(new String(bytes, enc).equals(str)))
            throw new Exception(enc + ": String constructor failed");
        if (!(new String(bytes, charset).equals(str)))
            throw new Exception(charset + ": String constructor failed");
        String start = str.substring(0, 2);
        String end = str.substring(2);
        if (enc.equals("UTF-16BE") || enc.equals("UTF-16LE")) {
            if (!(new String(bytes, 0, 4, charset).equals(start)))
                throw new Exception(charset + ": String constructor failed");
            if (!(new String(bytes, 4, bytes.length - 4, charset).equals(end)))
                throw new Exception(charset + ": String constructor failed");
        } else if (enc.equals("UTF-16")) {
            if (!(new String(bytes, 0, 6, charset).equals(start)))
                throw new Exception(charset + ": String constructor failed");
        } else {
            if (!(new String(bytes, 0, 2, charset).equals(start)))
                throw new Exception(charset + ": String constructor failed");
            if (!(new String(bytes, 2, bytes.length - 2, charset).equals(end)))
                throw new Exception(charset + ": String constructor failed");
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        InputStreamReader r = new InputStreamReader(bi, enc);
        String inEnc = r.getEncoding();
        int n = str.length();
        char[] cs = new char[n];
        for (int i = 0; i < n;) {
            int m;
            if ((m = r.read(cs, i, n - i)) < 0)
                throw new Exception(enc + ": EOF on InputStreamReader");
            i += m;
        }
        if (!(new String(cs).equals(str)))
            throw new Exception(enc + ": InputStreamReader failed");
        if (!bidir) {
            System.err.println(enc + " --> " + inEnc);
            return;
        }
        byte[] bs = str.getBytes(enc);
        if (!equals(bs, bytes))
            throw new Exception(enc + ": String.getBytes failed");
        bs = str.getBytes(charset);
        if (!equals(bs, bytes))
            throw new Exception(charset + ": String.getBytes failed");
        if (charset.name().equals("UTF-16BE")) {
            String s = new String(bytes, charset);
            byte[] bb = s.getBytes(Charset.forName("UTF-16LE"));
            if (bytes.length != bb.length) {
                throw new RuntimeException("unequal length: "
                                           + bytes.length + " != "
                                           + bb.length);
            } else {
                boolean diff = false;
                for (int i = 0; i < bytes.length; i++) {
                    if (bytes[i] != bb[i])
                        diff = true;
                }
                if (!diff)
                    throw new RuntimeException("byte arrays equal");
            }
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(bo, enc);
        String outEnc = w.getEncoding();
        w.write(str);
        w.close();
        bs = bo.toByteArray();
        if (!equals(bs, bytes))
            throw new Exception(enc + ": OutputStreamWriter failed");
        System.err.println(enc + " --> " + inEnc + " / " + outEnc);
    }
    static void go(String enc, String str, byte[] bytes) throws Exception {
        go(enc, str, bytes, true);
    }
    public static void main(String[] args) throws Exception {
        go("US-ASCII", "abc", new byte[] { 'a', 'b', 'c' });
        go("us-ascii", "abc", new byte[] { 'a', 'b', 'c' });
        go("ISO646-US", "abc", new byte[] { 'a', 'b', 'c' });
        go("ISO-8859-1", "ab\u00c7", new byte[] { 'a', 'b', (byte)'\u00c7' });
        go("UTF-8", "ab\u1e09",
           new byte[] { 'a', 'b',
                        (byte)(0xe0 | (0x0f & (0x1e09 >> 12))),
                        (byte)(0x80 | (0x3f & (0x1e09 >> 6))),
                        (byte)(0x80 | (0x3f & 0x1e09)) });
        go("UTF-16BE", "ab\u1e09",
           new byte[] { 0, 'a', 0, 'b', 0x1e, 0x09 });
        go("UTF-16LE", "ab\u1e09",
           new byte[] { 'a', 0, 'b', 0, 0x09, 0x1e });
        go("UTF-16", "ab\u1e09",
           new byte[] { (byte)0xfe, (byte)0xff, 0, 'a', 0, 'b', 0x1e, 0x09 });
        go("UTF-16", "ab\u1e09",
           new byte[] { (byte)0xff, (byte)0xfe, 'a', 0, 'b', 0, 0x09, 0x1e },
           false);
    }
}
