public class BitMap {
  public BitMap(int sizeInBits) {
    this.size = sizeInBits;
    int nofWords = sizeInWords();
    data = new int[nofWords];
  }
  public int size() {
    return size;
  }
  public boolean at(int offset) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(offset>=0 && offset < size(), "BitMap index out of bounds");
    }
    return Bits.isSetNthBit(wordFor(offset), offset % bitsPerWord);
  }
  public void atPut(int offset, boolean value) {
    int index = indexFor(offset);
    int pos   = offset % bitsPerWord;
    if (value) {
      data[index] = Bits.setNthBit(data[index], pos);
    } else {
      data[index] = Bits.clearNthBit(data[index], pos);
    }
  }
  public void set_size(int value) {
    size = value;
  }
  public void set_map(Address addr) {
    for (int i=0; i<sizeInWords(); i++) {
      data[i] =  (int) addr.getCIntegerAt(0, bytesPerWord, true);
      addr = addr.addOffsetTo(bytesPerWord);
    }
  }
  public void clear() {
    for (int i = 0; i < sizeInWords(); i++) {
      data[i] = Bits.NoBits;
    }
  }
  public void iterate(BitMapClosure blk) {
    for (int index = 0; index < sizeInWords(); index++) {
      int rest = data[index];
      for (int offset = index * bitsPerWord; rest != Bits.NoBits; offset++) {
        if (rest % 2 == 1) {
          if (offset < size()) {
            blk.doBit(offset);
          } else {
            return; 
          }
        }
        rest = rest >>> 1;
      }
    }
  }
  public boolean setUnion(BitMap other) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(size() == other.size(), "must have same size");
    }
    boolean changed = false;
    for (int index = 0; index < sizeInWords(); index++) {
      int temp = data[index] | other.data[index];
      changed = changed || (temp != data[index]);
      data[index] = temp;
    }
    return changed;
  }
  public void setIntersection(BitMap other) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(size() == other.size(), "must have same size");
    }
    for (int index = 0; index < sizeInWords(); index++) {
      data[index] = data[index] & (other.data[index]);
    }
  }
  public void setFrom(BitMap other) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(size() == other.size(), "must have same size");
    }
    for (int index = 0; index < sizeInWords(); index++) {
      data[index] = other.data[index];
    }
  }
  public boolean setDifference(BitMap other) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(size() == other.size(), "must have same size");
    }
    boolean changed = false;
    for (int index = 0; index < sizeInWords(); index++) {
      int temp = data[index] & ~(other.data[index]);
      changed = changed || (temp != data[index]);
      data[index] = temp;
    }
    return changed;
  }
  public boolean isSame(BitMap other) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(size() == other.size(), "must have same size");
    }
    for (int index = 0; index < sizeInWords(); index++) {
      if (data[index] != (other.data[index])) return false;
    }
    return true;
  }
  public int getNextOneOffset(int l_offset, int r_offset) {
    if (l_offset == r_offset) {
      return l_offset;
    }
    int index = indexFor(l_offset);
    int r_index = indexFor(r_offset);
    int res_offset = l_offset;
    int pos = bitInWord(res_offset);
    int res = data[index] >> pos;
    if (res != 0) {
      for (; (res & 1) == 0; res_offset++) {
        res = res >> 1;
      }
      return res_offset;
    }
    for (index++; index < r_index; index++) {
      res = data[index];
      if (res != 0) {
        for (res_offset = index * bitsPerWord; (res & 1) == 0; res_offset++) {
          res = res >> 1;
        }
        return res_offset;
      }
    }
    return r_offset;
  }
  private int   size; 
  private int[] data;
  private static final int bitsPerWord = 32;
  private static final int bytesPerWord = 4;
  private int sizeInWords() {
    return (size() + bitsPerWord - 1) / bitsPerWord;
  }
  private int indexFor(int offset) {
    return offset / bitsPerWord;
  }
  private int wordFor(int offset) {
    return data[offset / bitsPerWord];
  }
  private int bitInWord(int offset) {
    return offset & (bitsPerWord - 1);
  }
}
