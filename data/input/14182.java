public class TestStringCodingUTF8 {
    public static void main(String[] args) throws Throwable {
        test();
        System.setSecurityManager(new PermissiveSecurityManger());
        test();
    }
    static void test() throws Throwable {
        Charset cs = Charset.forName("UTF-8");
        char[] bmp = new char[0x10000];
        for (int i = 0; i < 0x10000; i++) {
            bmp[i] = (char)i;
        }
        test(cs, bmp, 0, bmp.length);
        ArrayList<Integer> list = new ArrayList<>(0x20000);
        for (int i = 0; i < 0x20000; i++) {
            list.add(i, i);
        }
        Collections.shuffle(list);
        int j = 0;
        char[] bmpsupp = new char[0x30000];
        for (int i = 0; i < 0x20000; i++) {
            j += Character.toChars(list.get(i), bmpsupp, j);
        }
        assert (j == bmpsupp.length);
        test(cs, bmpsupp, 0, bmpsupp.length);
        Random rnd = new Random();
        int maxlen = 1000;
        int itr = 5000;
        for (int i = 0; i < itr; i++) {
            int off = rnd.nextInt(bmpsupp.length - maxlen);
            int len = rnd.nextInt(maxlen);
            test(cs, bmpsupp, off, len);
        }
        for (int i = 0; i < itr; i++) {
            byte[] ba = new byte[rnd.nextInt(maxlen)];
            rnd.nextBytes(ba);
            if (!new String(ba, cs.name()).equals(
                 new String(decode(cs, ba, 0, ba.length))))
                throw new RuntimeException("new String(csn) failed");
            if (!new String(ba, cs).equals(
                 new String(decode(cs, ba, 0, ba.length))))
                throw new RuntimeException("new String(cs) failed");
        }
        System.out.println("done!");
    }
    static void test(Charset cs, char[] ca, int off, int len) throws Throwable {
        String str = new String(ca, off, len);
        byte[] ba = encode(cs, ca, off, len);
        byte[] baStr = str.getBytes(cs.name());
        if (!Arrays.equals(ba, baStr))
            throw new RuntimeException("getBytes(csn) failed");
        baStr = str.getBytes(cs);
        if (!Arrays.equals(ba, baStr))
            throw new RuntimeException("getBytes(cs) failed");
        if (!new String(ba, cs.name()).equals(new String(decode(cs, ba, 0, ba.length))))
            throw new RuntimeException("new String(csn) failed");
        if (!new String(ba, cs).equals(new String(decode(cs, ba, 0, ba.length))))
            throw new RuntimeException("new String(cs) failed");
    }
    static char[] decode(Charset cs, byte[] ba, int off, int len) {
        CharsetDecoder cd = cs.newDecoder();
        int en = (int)(len * cd.maxCharsPerByte());
        char[] ca = new char[en];
        if (len == 0)
            return ca;
        cd.onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset();
        ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
        CharBuffer cb = CharBuffer.wrap(ca);
        try {
            CoderResult cr = cd.decode(bb, cb, true);
            if (!cr.isUnderflow())
                cr.throwException();
            cr = cd.flush(cb);
            if (!cr.isUnderflow())
                cr.throwException();
        } catch (CharacterCodingException x) {
            throw new Error(x);
        }
        return Arrays.copyOf(ca, cb.position());
    }
    static byte[] encode(Charset cs, char[] ca, int off, int len) {
        CharsetEncoder ce = cs.newEncoder();
        int en = (int)(len * ce.maxBytesPerChar());
        byte[] ba = new byte[en];
        if (len == 0)
            return ba;
        ce.onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset();
        ByteBuffer bb = ByteBuffer.wrap(ba);
        CharBuffer cb = CharBuffer.wrap(ca, off, len);
        try {
            CoderResult cr = ce.encode(cb, bb, true);
            if (!cr.isUnderflow())
                cr.throwException();
            cr = ce.flush(bb);
            if (!cr.isUnderflow())
                cr.throwException();
        } catch (CharacterCodingException x) {
            throw new Error(x);
        }
        return Arrays.copyOf(ba, bb.position());
    }
    static class PermissiveSecurityManger extends SecurityManager {
        @Override public void checkPermission(java.security.Permission p) {}
    }
}
