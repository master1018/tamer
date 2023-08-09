public class TestX11CNS {
    static char[] decode(byte[] bb, Charset cs)
        throws Exception {
        CharsetDecoder dec = cs.newDecoder();
        ByteBuffer bbf = ByteBuffer.wrap(bb);
        CharBuffer cbf = CharBuffer.allocate(bb.length);
        CoderResult cr = dec.decode(bbf, cbf, true);
        if (cr != CoderResult.UNDERFLOW) {
            System.out.println("DEC-----------------");
            int pos = bbf.position();
            System.out.printf("  cr=%s, bbf.pos=%d, bb[pos]=%x,%x,%x,%x%n",
                              cr.toString(), pos,
                              bb[pos++]&0xff, bb[pos++]&0xff,bb[pos++]&0xff, bb[pos++]&0xff);
            throw new RuntimeException("Decoding err: " + cs.name());
        }
        char[] cc = new char[cbf.position()];
        cbf.flip(); cbf.get(cc);
        return cc;
    }
    static byte[] encode(char[] cc, Charset cs)
        throws Exception {
        ByteBuffer bbf = ByteBuffer.allocate(cc.length * 4);
        CharBuffer cbf = CharBuffer.wrap(cc);
        CharsetEncoder enc = cs.newEncoder();
        CoderResult cr = enc.encode(cbf, bbf, true);
        if (cr != CoderResult.UNDERFLOW) {
            System.out.println("ENC-----------------");
            int pos = cbf.position();
            System.out.printf("  cr=%s, cbf.pos=%d, cc[pos]=%x%n",
                              cr.toString(), pos, cc[pos]&0xffff);
            throw new RuntimeException("Encoding err: " + cs.name());
        }
        byte[] bb = new byte[bbf.position()];
        bbf.flip(); bbf.get(bb);
        return bb;
    }
    static char[] getChars(Charset newCS, Charset oldCS) {
        CharsetEncoder enc = oldCS.newEncoder();
        CharsetEncoder encNew = newCS.newEncoder();
        char[] cc = new char[0x10000];
        int pos = 0;
        int i = 0;
        while (i < 0x10000) {
            if (i == 0x4ea0 || i == 0x51ab || i == 0x52f9) {
                i++;continue;
            }
            if (enc.canEncode((char)i) != encNew.canEncode((char)i)) {
                System.out.printf("  Err i=%x%n", i);
            }
            if (enc.canEncode((char)i)) {
                cc[pos++] = (char)i;
            }
            i++;
        }
        return Arrays.copyOf(cc, pos);
    }
    static void compare(Charset newCS, Charset oldCS) throws Exception {
        if (newCS == null)
            return;  
        char[] cc = getChars(newCS, oldCS);
        System.out.printf("    Diff <%s> <%s>...%n", newCS.name(), oldCS.name());
        byte[] bb1 = encode(cc, newCS);
        byte[] bb2 = encode(cc, oldCS);
        if (!Arrays.equals(bb1, bb2)) {
            System.out.printf("        encoding failed!%n");
        }
        char[] cc1 = decode(bb1, newCS);
        char[] cc2 = decode(bb1, oldCS);
        if (!Arrays.equals(cc1, cc2)) {
            for (int i = 0; i < cc1.length; i++) {
                if (cc1[i] != cc2[i]) {
                    System.out.printf("i=%d, cc1=%x cc2=%x,  bb=<%x%x>%n",
                                      i,
                                      cc1[i]&0xffff, cc2[i]&0xffff,
                                      bb1[i*2]&0xff, bb1[i*2+1]&0xff);
                }
            }
            System.out.printf("        decoding failed%n");
        }
    }
    private static Charset getCharset(String czName)
        throws Exception {
        try {
            return (Charset)Class.forName(czName).newInstance();
        } catch (ClassNotFoundException e){}
        return null;  
    }
    public static void main(String[] args) throws Exception {
        compare(getCharset("sun.awt.motif.X11CNS11643P1"),
                new X11CNS11643P1());
        compare(getCharset("sun.awt.motif.X11CNS11643P2"),
                new X11CNS11643P2());
        compare(getCharset("sun.awt.motif.X11CNS11643P3"),
                new X11CNS11643P3());
    }
}
