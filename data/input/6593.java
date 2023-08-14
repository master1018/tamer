public class CompressedReadStream extends CompressedStream {
  public CompressedReadStream(Address buffer) {
    this(buffer, 0);
  }
  public CompressedReadStream(Address buffer, int position) {
    super(buffer, position);
  }
  public boolean readBoolean() {
    return (read() != 0);
  }
  public byte readByte() {
    return (byte) read();
  }
  public char readChar() {
    return (char) readInt();
  }
  public short readShort() {
    return (short) readSignedInt();
  }
  public int readSignedInt() {
    return decodeSign(readInt());
  }
  public int readInt() {
    int b0 = read();
    if (b0 < L) {
      return b0;
    } else {
      return readIntMb(b0);
    }
  }
  public float readFloat() {
    return Float.intBitsToFloat(reverseInt(readInt()));
  }
  public double readDouble() {
    int rh = readInt();
    int rl = readInt();
    int h = reverseInt(rh);
    int l = reverseInt(rl);
    return Double.longBitsToDouble((h << 32) | ((long)l & 0x00000000FFFFFFFFL));
  }
  public long readLong() {
    long low = readSignedInt() & 0x00000000FFFFFFFFL;
    long high = readSignedInt();
    return (high << 32) | low;
  }
  private int readIntMb(int b0) {
    int pos = position - 1;
    int sum = b0;
    int lg_H_i = lg_H;
    for (int i = 0; ;) {
      int b_i = read(pos + (++i));
      sum += b_i << lg_H_i; 
      if (b_i < L || i == MAX_i) {
        setPosition(pos+i+1);
        return sum;
      }
      lg_H_i += lg_H;
    }
  }
  private short read(int index) {
    return (short) buffer.getCIntegerAt(index, 1, true);
  }
  private short read() {
    short retval = (short) buffer.getCIntegerAt(position, 1, true);
    ++position;
    return retval;
  }
}
