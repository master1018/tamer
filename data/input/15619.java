public class CompressedLineNumberReadStream extends CompressedReadStream {
  public CompressedLineNumberReadStream(Address buffer) {
    this(buffer, 0);
  }
  public CompressedLineNumberReadStream(Address buffer, int position) {
    super(buffer, position);
  }
  public boolean readPair() {
    int next = readByte() & 0xFF;
    if (next == 0) return false;
    if (next == 0xFF) {
      bci  += readSignedInt();
      line += readSignedInt();
    } else {
      bci  += next >> 3;
      line += next & 0x7;
    }
    return true;
  }
  public int bci()  { return bci;  }
  public int line() { return line; }
  private int bci;
  private int line;
}
