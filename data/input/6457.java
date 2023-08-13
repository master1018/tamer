public class TestIllegalSJIS {
  public static void main(String[] args) throws Exception
  {
    CharsetDecoder dec = Charset.forName("SJIS").newDecoder()
      .onUnmappableCharacter(CodingErrorAction.REPLACE)
      .onMalformedInput(CodingErrorAction.REPLACE);
    byte[] sjis      = {(byte)0xA0, (byte)0x00};
    int b;
    for (b = 0; b < 0xFD; b++) {
      sjis[1] = (byte) b;
      CharBuffer cb = dec.decode(ByteBuffer.wrap(sjis));
      if (cb.charAt(0) != 0xFFFD) {
        throw new Exception(Integer.toHexString(0xa000 + b) + " failed to convert to 0xFFFD");
      }
    }
  }
}
