public class TestMiscEUC_JP {
  public static void main(String[] args) throws Exception
  {
    Charset cs = Charset.forName("EUC_JP");
    CharsetDecoder dec  = cs.newDecoder();
    CharsetEncoder enc  = cs.newEncoder();
    byte[] euc           = {(byte)0x8F, (byte)0xA2, (byte)0xB7};
    CharBuffer cb = dec.decode(ByteBuffer.wrap(euc));
    if (cb.charAt(0) != 0xFF5E) {
      throw new Exception("Converted EUC_JP 0x8FA2B7 to: 0x"
                          + Integer.toHexString((int)cb.charAt(0)));
    }
    ByteBuffer bb = enc.encode(cb);
    if (!((bb.limit() == 3)
          && (bb.get() == euc[0])
          && (bb.get() == euc[1])
          && (bb.get() == euc[2]))) {
      cb.flip();
      bb.flip();
      throw new Exception("Roundrip failed for char 0x"
                          + Integer.toHexString((int)cb.charAt(0)) + ": "
                          + Integer.toHexString(bb.limit()) + " 0x"
                          + Integer.toHexString((int)bb.get() & 0xff) + " "
                          + Integer.toHexString((int)bb.get() & 0xff) + " "
                          + Integer.toHexString((int)bb.get() & 0xff));
    }
  }
}
