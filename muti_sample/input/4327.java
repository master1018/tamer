public class LeftOverSurrogate {
    public static void main(String args[]) throws Exception {
        String s = "abc\uD800\uDC00qrst"; 
        char[] c = s.toCharArray();
        CharsetEncoder enc = Charset.forName("ISO8859_1").newEncoder()
          .onUnmappableCharacter(CodingErrorAction.REPLACE);
        ByteBuffer bb = ByteBuffer.allocate(10);
        CharBuffer cb = CharBuffer.wrap(c);
        cb.limit(4);
        enc.encode(cb, bb, false);
        cb.limit(7);
        enc.encode(cb, bb, true);
        byte[] first = bb.array();
        for(int i = 0; i < 7; i++)
            System.err.printf("[%d]=%d was %d\n",
                              i,
                              (int) first[i] &0xffff,
                              (int) c[i] & 0xffff);
    }
}
