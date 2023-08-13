public class Symbol extends VMObject {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type  = db.lookupType("Symbol");
    length     = type.getCIntegerField("_length");
    baseOffset = type.getField("_body").getOffset();
    idHash = type.getCIntegerField("_identity_hash");
  }
  public static Symbol create(Address addr) {
    if (addr == null) {
      return null;
    }
    return new Symbol(addr);
  }
  Symbol(Address addr) {
    super(addr);
  }
  public boolean isSymbol()            { return true; }
  private static long baseOffset; 
  private static CIntegerField length;
  public long   getLength() { return          length.getValue(this.addr); }
  public byte getByteAt(long index) {
    return addr.getJByteAt(baseOffset + index);
  }
  private static CIntegerField idHash;
  public int identityHash() { return     (int)idHash.getValue(this.addr); }
  public boolean equals(byte[] modUTF8Chars) {
    int l = (int) getLength();
    if (l != modUTF8Chars.length) return false;
    while (l-- > 0) {
      if (modUTF8Chars[l] != getByteAt(l)) return false;
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(l == -1, "we should be at the beginning");
    }
    return true;
  }
  public byte[] asByteArray() {
    int length = (int) getLength();
    byte [] result = new byte [length];
    for (int index = 0; index < length; index++) {
      result[index] = getByteAt(index);
    }
    return result;
  }
  public String asString() {
    try {
      return readModifiedUTF8(asByteArray());
    } catch(Exception e) {
      System.err.println(addr);
      e.printStackTrace();
      return null;
    }
  }
  public boolean startsWith(String str) {
    return asString().startsWith(str);
  }
  public void printValueOn(PrintStream tty) {
    tty.print("#" + asString());
  }
  public int fastCompare(Symbol other) {
    return (int) addr.minus(other.addr);
  }
  private static String readModifiedUTF8(byte[] buf) throws IOException {
    final int len = buf.length;
    byte[] tmp = new byte[len + 2];
    tmp[0] = (byte) ((len >>> 8) & 0xFF);
    tmp[1] = (byte) ((len >>> 0) & 0xFF);
    System.arraycopy(buf, 0, tmp, 2, len);
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmp));
    return dis.readUTF();
  }
}
