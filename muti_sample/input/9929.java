public class TestISCII91 {
    public static void main(String[] args) throws Throwable{
        CharToByteConverter c2b = new CharToByteISCII91();
        ByteToCharConverter b2c = new ByteToCharISCII91();
        Charset cs = Charset.forName("ISCII91");
        String charsToEncode = getCharsForEncoding("ISCII91");
        byte [] c2bBytes = c2b.convertAll(charsToEncode.toCharArray());
        byte [] csBytes = cs.encode(charsToEncode).array();
        for (int i = 0; i < c2bBytes.length; ++i) {
            if (c2bBytes[i] != csBytes[i])
                throw new RuntimeException("ISCII91 encoding failed!");
        }
        char[] c2bChars = b2c.convertAll(c2bBytes);
        char[] csChars = cs.decode(ByteBuffer.wrap(csBytes)).array();
        for (int i = 0; i < c2bChars.length; ++i) {
            if (c2bChars[i] != csChars[i])
                throw new RuntimeException("ISCII91 decoding failed!");
        }
    }
    static String getCharsForEncoding(String encodingName)
        throws CharacterCodingException{
        Charset set = Charset.forName(encodingName);
        CharBuffer chars = CharBuffer.allocate(300);
        CharsetEncoder encoder = set.newEncoder();
        for (int c = 0; chars.remaining() > 0 && c < Character.MAX_VALUE; ++c) {
            if (Character.isDefined((char) c) && !Character.isISOControl((char) c) && encoder.canEncode((char) c)) {
                chars.put((char) c);
            }
        }
        chars.limit(chars.position());
        chars.rewind();
        return chars.toString();
    }
}
