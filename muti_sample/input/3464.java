public class CompressedStream {
  protected Address buffer;
  protected int     position;
  public CompressedStream(Address buffer) {
    this(buffer, 0);
  }
  public CompressedStream(Address buffer, int position) {
    this.buffer   = buffer;
    this.position = position;
  }
  public Address getBuffer() {
    return buffer;
  }
  public static final int LogBitsPerByte = 3;
  public static final int BitsPerByte = 1 << 3;
  public static final int lg_H = 6;
  public static final int H = 1<<lg_H;  
  public static final int L = (1<<BitsPerByte) - H; 
  public static final int MAX_i = 4;      
  public int getPosition() {
    return position;
  }
  public void setPosition(int position) {
    this.position = position;
  }
  public int encodeSign(int value) {
    return (value << 1) ^ (value >> 31);
  }
  public int decodeSign(int value) {
    return (value >>> 1) ^ -(value & 1);
  }
  public int reverseInt(int i) {
    i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
    i = (i & 0x33333333) << 3 | (i >>> 2) & 0x33333333;
    i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
    i = (i << 24) | ((i & 0xff00) << 8) | ((i >>> 8) & 0xff00) | (i >>> 24);
    return i;
  }
}
