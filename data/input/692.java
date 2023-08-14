public class Page {
  private long   baseAddress;
  private byte[] data;
  private Page   prev;
  private Page   next;
  private long   unmappedPageLength;
  public Page(long baseAddress, byte[] data) {
    this.baseAddress = baseAddress;
    this.data = data;
  }
  public Page(long baseAddress, long unmappedPageLength) {
    this.baseAddress = baseAddress;
    this.unmappedPageLength = unmappedPageLength;
  }
  public long getBaseAddress() {
    return baseAddress;
  }
  public long getSize() {
    if (data != null) {
      return data.length;
    } else {
      return unmappedPageLength;
    }
  }
  public boolean isMapped() {
    return (data != null);
  }
  public Page getPrev() {
    return prev;
  }
  public void setPrev(Page prev) {
    this.prev = prev;
  }
  public Page getNext() {
    return next;
  }
  public void setNext(Page next) {
    this.next = next;
  }
  public void getData(long startAddress, long numBytes,
                      int[] destBuf, long destBufOffset)
    throws IndexOutOfBoundsException {
    int startOffset = (int) (startAddress - baseAddress);
    if ((data == null) &&
        ((startOffset < 0) || ((startOffset + numBytes) > (baseAddress + unmappedPageLength)))) {
      throw new IndexOutOfBoundsException("startAddress = " + startAddress +
                                          ", baseAddress = " + baseAddress +
                                          ", unmappedPageLength = " + unmappedPageLength);
    }
    for (int i = 0; i < (int) numBytes; ++i) {
      if (data != null) {
        destBuf[i + (int) destBufOffset] = ((int) (data[i + startOffset]) & 0x000000FF);
      } else {
        destBuf[i + (int) destBufOffset] = -1;
      }
    }
  }
  public void getDataAsBytes(long startAddress, long numBytes,
                             byte[] destBuf, long destBufOffset)
    throws IndexOutOfBoundsException {
    long startOffset = startAddress - baseAddress;
    if (data == null) {
      throw new RuntimeException("Bug in PageCache; should not fetch from unmapped pages using getDataAsBytes");
    }
    System.arraycopy(data, (int) startOffset, destBuf, (int) destBufOffset, (int) numBytes);
  }
  public boolean getBoolean(long address) {
    return (getByte(address) != 0);
  }
  public byte getByte(long address) {
    return data[(int) address - (int) baseAddress];
  }
  public short getShort(long address, boolean bigEndian) {
    int start = (int) address - (int) baseAddress;
    if (bigEndian) {
      return (short)
        (((data[start + 1] & 0xFF)) |
         ((data[start]     & 0xFF) << 8));
    } else {
      return (short)
        (((data[start + 1] & 0xFF) << 8) |
         ((data[start]     & 0xFF)));
    }
  }
  public char getChar(long address, boolean bigEndian) {
    return (char) getShort(address, bigEndian);
  }
  public int getInt(long address, boolean bigEndian) {
    int start = (int) address - (int) baseAddress;
    if (bigEndian) {
      return
        ((data[start + 3] & 0xFF))       |
        ((data[start + 2] & 0xFF) << 8)  |
        ((data[start + 1] & 0xFF) << 16) |
        ((data[start]     & 0xFF) << 24);
    } else {
      return
        ((data[start + 3] & 0xFF) << 24) |
        ((data[start + 2] & 0xFF) << 16) |
        ((data[start + 1] & 0xFF) << 8)  |
        ((data[start]     & 0xFF));
    }
  }
  public long getLong(long address, boolean bigEndian) {
    int start = (int) address - (int) baseAddress;
    if (bigEndian) {
      return
        ((data[start + 7] & 0xFFL))       |
        ((data[start + 6] & 0xFFL) << 8)  |
        ((data[start + 5] & 0xFFL) << 16) |
        ((data[start + 4] & 0xFFL) << 24) |
        ((data[start + 3] & 0xFFL) << 32) |
        ((data[start + 2] & 0xFFL) << 40) |
        ((data[start + 1] & 0xFFL) << 48) |
        ((data[start]     & 0xFFL) << 56);
    } else {
      return
        ((data[start + 7] & 0xFFL) << 56) |
        ((data[start + 6] & 0xFFL) << 48) |
        ((data[start + 5] & 0xFFL) << 40) |
        ((data[start + 4] & 0xFFL) << 32) |
        ((data[start + 3] & 0xFFL) << 24) |
        ((data[start + 2] & 0xFFL) << 16) |
        ((data[start + 1] & 0xFFL) << 8)  |
        ((data[start]     & 0xFFL));
    }
  }
  public float getFloat(long address, boolean bigEndian) {
    return Float.intBitsToFloat(getInt(address, bigEndian));
  }
  public double getDouble(long address, boolean bigEndian) {
    return Double.longBitsToDouble(getLong(address, bigEndian));
  }
}
