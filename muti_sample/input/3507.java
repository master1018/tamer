public abstract class DebuggerBase implements Debugger {
  protected MachineDescription machDesc;
  protected DebuggerUtilities utils;
  protected long jbooleanSize;
  protected long jbyteSize;
  protected long jcharSize;
  protected long jdoubleSize;
  protected long jfloatSize;
  protected long jintSize;
  protected long jlongSize;
  protected long jshortSize;
  protected boolean javaPrimitiveTypesConfigured;
  protected long oopSize;
  protected long heapOopSize;
  protected long narrowOopBase;  
  protected int  narrowOopShift; 
  private PageCache cache;
  private boolean useFastAccessors;
  private boolean bigEndian;
  class Fetcher implements PageFetcher {
    public Page fetchPage(long pageBaseAddress, long numBytes) {
      ReadResult res = readBytesFromProcess(pageBaseAddress, numBytes);
      if (res.getData() == null) {
        return new Page(pageBaseAddress, numBytes);
      }
      return new Page(pageBaseAddress, res.getData());
    }
  }
  protected DebuggerBase() {
  }
  public void configureJavaPrimitiveTypeSizes(long jbooleanSize,
                                              long jbyteSize,
                                              long jcharSize,
                                              long jdoubleSize,
                                              long jfloatSize,
                                              long jintSize,
                                              long jlongSize,
                                              long jshortSize) {
    this.jbooleanSize = jbooleanSize;
    this.jbyteSize = jbyteSize;
    this.jcharSize = jcharSize;
    this.jdoubleSize = jdoubleSize;
    this.jfloatSize = jfloatSize;
    this.jintSize = jintSize;
    this.jlongSize = jlongSize;
    this.jshortSize = jshortSize;
    if (jbooleanSize < 1) {
      throw new RuntimeException("jboolean size is too small");
    }
    if (jbyteSize < 1) {
      throw new RuntimeException("jbyte size is too small");
    }
    if (jcharSize < 2) {
      throw new RuntimeException("jchar size is too small");
    }
    if (jdoubleSize < 8) {
      throw new RuntimeException("jdouble size is too small");
    }
    if (jfloatSize < 4) {
      throw new RuntimeException("jfloat size is too small");
    }
    if (jintSize < 4) {
      throw new RuntimeException("jint size is too small");
    }
    if (jlongSize < 8) {
      throw new RuntimeException("jlong size is too small");
    }
    if (jshortSize < 2) {
      throw new RuntimeException("jshort size is too small");
    }
    if (jintSize != jfloatSize) {
      throw new RuntimeException("jint size and jfloat size must be equal");
    }
    if (jlongSize != jdoubleSize) {
      throw new RuntimeException("jlong size and jdouble size must be equal");
    }
    useFastAccessors =
      ((cache != null) &&
       (jbooleanSize == 1) &&
       (jbyteSize    == 1) &&
       (jcharSize    == 2) &&
       (jdoubleSize  == 8) &&
       (jfloatSize   == 4) &&
       (jintSize     == 4) &&
       (jlongSize    == 8) &&
       (jshortSize   == 2));
    javaPrimitiveTypesConfigured = true;
  }
  public void putHeapConst(long heapOopSize, long narrowOopBase, int narrowOopShift) {
    this.heapOopSize = heapOopSize;
    this.narrowOopBase = narrowOopBase;
    this.narrowOopShift = narrowOopShift;
  }
  protected final void initCache(long pageSize, long maxNumPages) {
    cache = new PageCache(pageSize, maxNumPages, new Fetcher());
    if (machDesc != null) {
      bigEndian = machDesc.isBigEndian();
    }
  }
  protected final void setBigEndian(boolean bigEndian) {
    this.bigEndian = bigEndian;
  }
  protected final void clearCache() {
    if (cache != null) {
      cache.clear();
    }
  }
  protected final void disableCache() {
    if (cache != null) {
      cache.disable();
    }
  }
  protected final void enableCache() {
    if (cache != null) {
      cache.enable();
    }
  }
  protected final byte[] readBytes(long address, long numBytes)
    throws UnmappedAddressException, DebuggerException {
    if (cache != null) {
      return cache.getData(address, numBytes);
    } else {
      ReadResult res = readBytesFromProcess(address, numBytes);
      if (res.getData() != null) {
        return res.getData();
      }
      throw new UnmappedAddressException(res.getFailureAddress());
    }
  }
  protected final void writeBytes(long address, long numBytes, byte[] data)
    throws UnmappedAddressException, DebuggerException {
    if (cache != null) {
      cache.clear(address, numBytes);
    }
    writeBytesToProcess(address, numBytes, data);
  }
  public boolean readJBoolean(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jbooleanSize);
    if (useFastAccessors) {
      return (cache.getByte(address) != 0);
    } else {
      byte[] data = readBytes(address, jbooleanSize);
      return utils.dataToJBoolean(data, jbooleanSize);
    }
  }
  public byte readJByte(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jbyteSize);
    if (useFastAccessors) {
      return cache.getByte(address);
    } else {
      byte[] data = readBytes(address, jbyteSize);
      return utils.dataToJByte(data, jbyteSize);
    }
  }
  public char readJChar(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jcharSize);
    if (useFastAccessors) {
      return cache.getChar(address, bigEndian);
    } else {
      byte[] data = readBytes(address, jcharSize);
      return (char) utils.dataToJChar(data, jcharSize);
    }
  }
  public double readJDouble(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jdoubleSize);
    if (useFastAccessors) {
      return cache.getDouble(address, bigEndian);
    } else {
      byte[] data = readBytes(address, jdoubleSize);
      return utils.dataToJDouble(data, jdoubleSize);
    }
  }
  public float readJFloat(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jfloatSize);
    if (useFastAccessors) {
      return cache.getFloat(address, bigEndian);
    } else {
      byte[] data = readBytes(address, jfloatSize);
      return utils.dataToJFloat(data, jfloatSize);
    }
  }
  public int readJInt(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jintSize);
    if (useFastAccessors) {
      return cache.getInt(address, bigEndian);
    } else {
      byte[] data = readBytes(address, jintSize);
      return utils.dataToJInt(data, jintSize);
    }
  }
  public long readJLong(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jlongSize);
    if (useFastAccessors) {
      return cache.getLong(address, bigEndian);
    } else {
      byte[] data = readBytes(address, jlongSize);
      return utils.dataToJLong(data, jlongSize);
    }
  }
  public short readJShort(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jshortSize);
    if (useFastAccessors) {
      return cache.getShort(address, bigEndian);
    } else {
      byte[] data = readBytes(address, jshortSize);
      return utils.dataToJShort(data, jshortSize);
    }
  }
  public long readCInteger(long address, long numBytes, boolean isUnsigned)
    throws UnmappedAddressException, UnalignedAddressException {
    checkConfigured();
    utils.checkAlignment(address, numBytes);
    if (useFastAccessors) {
      if (isUnsigned) {
        switch((int) numBytes) {
        case 1: return cache.getByte(address) & 0xFF;
        case 2: return cache.getShort(address, bigEndian) & 0xFFFF;
        case 4: return cache.getInt(address, bigEndian) & 0xFFFFFFFFL;
        case 8: return cache.getLong(address, bigEndian);
        default: {
          byte[] data = readBytes(address, numBytes);
          return utils.dataToCInteger(data, isUnsigned);
        }
        }
      } else {
        switch((int) numBytes) {
        case 1: return cache.getByte(address);
        case 2: return cache.getShort(address, bigEndian);
        case 4: return cache.getInt(address, bigEndian);
        case 8: return cache.getLong(address, bigEndian);
        default: {
          byte[] data = readBytes(address, numBytes);
          return utils.dataToCInteger(data, isUnsigned);
        }
        }
      }
    } else {
      byte[] data = readBytes(address, numBytes);
      return utils.dataToCInteger(data, isUnsigned);
    }
  }
  public void writeJBoolean(long address, boolean value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jbooleanSize);
    byte[] data = utils.jbooleanToData(value);
    writeBytes(address, jbooleanSize, data);
  }
  public void writeJByte(long address, byte value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jbyteSize);
    byte[] data = utils.jbyteToData(value);
    writeBytes(address, jbyteSize, data);
  }
  public void writeJChar(long address, char value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jcharSize);
    byte[] data = utils.jcharToData(value);
    writeBytes(address, jcharSize, data);
  }
  public void writeJDouble(long address, double value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jdoubleSize);
    byte[] data = utils.jdoubleToData(value);
    writeBytes(address, jdoubleSize, data);
  }
  public void writeJFloat(long address, float value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jfloatSize);
    byte[] data = utils.jfloatToData(value);
    writeBytes(address, jfloatSize, data);
  }
  public void writeJInt(long address, int value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jintSize);
    byte[] data = utils.jintToData(value);
    writeBytes(address, jintSize, data);
  }
  public void writeJLong(long address, long value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jlongSize);
    byte[] data = utils.jlongToData(value);
    writeBytes(address, jlongSize, data);
  }
  public void writeJShort(long address, short value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jshortSize);
    byte[] data = utils.jshortToData(value);
    writeBytes(address, jshortSize, data);
  }
  public void writeCInteger(long address, long numBytes, long value)
    throws UnmappedAddressException, UnalignedAddressException {
    checkConfigured();
    utils.checkAlignment(address, numBytes);
    byte[] data = utils.cIntegerToData(numBytes, value);
    writeBytes(address, numBytes, data);
  }
  protected long readAddressValue(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    return readCInteger(address, machDesc.getAddressSize(), true);
  }
  protected long readCompOopAddressValue(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    long value = readCInteger(address, getHeapOopSize(), true);
    if (value != 0) {
      value = (long)(narrowOopBase + (long)(value << narrowOopShift));
    }
    return value;
  }
  protected void writeAddressValue(long address, long value)
    throws UnmappedAddressException, UnalignedAddressException {
    writeCInteger(address, machDesc.getAddressSize(), value);
  }
  protected final void checkConfigured() {
    if (machDesc == null) {
      throw new RuntimeException("MachineDescription must have been set by this point");
    }
    if (utils == null) {
      throw new RuntimeException("DebuggerUtilities must have been set by this point");
    }
  }
  protected final void checkJavaConfigured() {
    checkConfigured();
    if (!javaPrimitiveTypesConfigured) {
      throw new RuntimeException("Java primitive type sizes have not yet been configured");
    }
  }
  protected int parseCacheNumPagesProperty(int defaultNum) {
    String cacheNumPagesString = System.getProperty("cacheNumPages");
    if (cacheNumPagesString != null) {
      try {
        return Integer.parseInt(cacheNumPagesString);
      } catch (Exception e) {
        System.err.println("Error parsing cacheNumPages property:");
        e.printStackTrace();
      }
    }
    return defaultNum;
  }
  protected void invalidatePageCache(long startAddress, long numBytes) {
    cache.clear(startAddress, numBytes);
  }
  public long getJBooleanSize() {
    return jbooleanSize;
  }
  public long getJByteSize() {
    return jbyteSize;
  }
  public long getJCharSize() {
    return jcharSize;
  }
  public long getJDoubleSize() {
    return jdoubleSize;
  }
  public long getJFloatSize() {
    return jfloatSize;
  }
  public long getJIntSize() {
    return jintSize;
  }
  public long getJLongSize() {
    return jlongSize;
  }
  public long getJShortSize() {
    return jshortSize;
  }
  public long getHeapOopSize() {
    return heapOopSize;
  }
  public long getNarrowOopBase() {
    return narrowOopBase;
  }
  public int getNarrowOopShift() {
    return narrowOopShift;
  }
}
