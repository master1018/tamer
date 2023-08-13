class ProcAddress implements Address {
  protected ProcDebugger debugger;
  protected long addr;
  ProcAddress(ProcDebugger debugger, long addr) {
    this.debugger = debugger;
    this.addr = addr;
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!(arg instanceof ProcAddress)) {
      return false;
    }
    return (addr == ((ProcAddress) arg).addr);
  }
  public int hashCode() {
    return (int) addr;
  }
  public String toString() {
    return debugger.addressValueToString(addr);
  }
  public long getCIntegerAt(long offset, long numBytes, boolean isUnsigned) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readCInteger(addr + offset, numBytes, isUnsigned);
  }
  public Address getAddressAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readAddress(addr + offset);
  }
  public Address getCompOopAddressAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readCompOopAddress(addr + offset);
  }
  public boolean getJBooleanAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJBoolean(addr + offset);
  }
  public byte getJByteAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJByte(addr + offset);
  }
  public char getJCharAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJChar(addr + offset);
  }
  public double getJDoubleAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJDouble(addr + offset);
  }
  public float getJFloatAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJFloat(addr + offset);
  }
  public int getJIntAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJInt(addr + offset);
  }
  public long getJLongAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJLong(addr + offset);
  }
  public short getJShortAt(long offset) throws UnalignedAddressException, UnmappedAddressException {
    return debugger.readJShort(addr + offset);
  }
  public OopHandle getOopHandleAt(long offset)
    throws UnalignedAddressException, UnmappedAddressException, NotInHeapException {
    return debugger.readOopHandle(addr + offset);
  }
  public OopHandle getCompOopHandleAt(long offset)
    throws UnalignedAddressException, UnmappedAddressException, NotInHeapException {
    return debugger.readCompOopHandle(addr + offset);
  }
  public void setCIntegerAt(long offset, long numBytes, long value) {
    throw new DebuggerException("Unimplemented");
  }
  public void setAddressAt(long offset, Address value) {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJBooleanAt      (long offset, boolean value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJByteAt         (long offset, byte value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJCharAt         (long offset, char value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJDoubleAt       (long offset, double value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJFloatAt        (long offset, float value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJIntAt          (long offset, int value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJLongAt         (long offset, long value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setJShortAt        (long offset, short value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public void       setOopHandleAt     (long offset, OopHandle value)
    throws UnmappedAddressException, UnalignedAddressException {
    throw new DebuggerException("Unimplemented");
  }
  public Address    addOffsetTo       (long offset) throws UnsupportedOperationException {
    long value = addr + offset;
    if (value == 0) {
      return null;
    }
    return new ProcAddress(debugger, value);
  }
  public OopHandle  addOffsetToAsOopHandle(long offset) throws UnsupportedOperationException {
    long value = addr + offset;
    if (value == 0) {
      return null;
    }
    return new ProcOopHandle(debugger, value);
  }
  public long       minus(Address arg) {
    if (arg == null) {
      return addr;
    }
    return addr - ((ProcAddress) arg).addr;
  }
  public boolean    lessThan          (Address arg) {
    if (arg == null) {
      return false;
    }
    ProcAddress dbxArg = (ProcAddress) arg;
    if ((addr >= 0) && (dbxArg.addr < 0)) {
      return true;
    }
    if ((addr < 0) && (dbxArg.addr >= 0)) {
      return false;
    }
    return (addr < dbxArg.addr);
  }
  public boolean    lessThanOrEqual   (Address arg) {
    if (arg == null) {
      return false;
    }
    ProcAddress dbxArg = (ProcAddress) arg;
    if ((addr >= 0) && (dbxArg.addr < 0)) {
      return true;
    }
    if ((addr < 0) && (dbxArg.addr >= 0)) {
      return false;
    }
    return (addr <= dbxArg.addr);
  }
  public boolean    greaterThan       (Address arg) {
    if (arg == null) {
      return true;
    }
    ProcAddress dbxArg = (ProcAddress) arg;
    if ((addr >= 0) && (dbxArg.addr < 0)) {
      return false;
    }
    if ((addr < 0) && (dbxArg.addr >= 0)) {
      return true;
    }
    return (addr > dbxArg.addr);
  }
  public boolean    greaterThanOrEqual(Address arg) {
    if (arg == null) {
      return true;
    }
    ProcAddress dbxArg = (ProcAddress) arg;
    if ((addr >= 0) && (dbxArg.addr < 0)) {
      return false;
    }
    if ((addr < 0) && (dbxArg.addr >= 0)) {
      return true;
    }
    return (addr >= dbxArg.addr);
  }
  public Address    andWithMask(long mask) throws UnsupportedOperationException {
    long value = addr & mask;
    if (value == 0) {
      return null;
    }
    return new ProcAddress(debugger, value);
  }
  public Address    orWithMask(long mask) throws UnsupportedOperationException {
    long value = addr | mask;
    if (value == 0) {
      return null;
    }
    return new ProcAddress(debugger, value);
  }
  public Address    xorWithMask(long mask) throws UnsupportedOperationException {
    long value = addr ^ mask;
    if (value == 0) {
      return null;
    }
    return new ProcAddress(debugger, value);
  }
  long getValue() {
    return addr;
  }
  private static void check(boolean arg, String failMessage) {
    if (!arg) {
      System.err.println(failMessage + ": FAILED");
      System.exit(1);
    }
  }
  public static void main(String[] args) {
    ProcAddress p1 = new ProcAddress(null, 0x7FFFFFFFFFFFFFF0L);
    ProcAddress p2 = (ProcAddress) p1.addOffsetTo(10);
    ProcAddress n1 = (ProcAddress) p2.addOffsetTo(10);
    ProcAddress n2 = (ProcAddress) n1.addOffsetTo(10);
    check(p1.lessThan(p2), "lessThan 1");
    check(p1.lessThan(n1), "lessThan 2");
    check(p1.lessThan(n2), "lessThan 3");
    check(p2.lessThan(n1), "lessThan 4");
    check(p2.lessThan(n2), "lessThan 5");
    check(n1.lessThan(n2), "lessThan 6");
    check(!p1.lessThan(p1), "lessThan 7");
    check(!p2.lessThan(p2), "lessThan 8");
    check(!n1.lessThan(n1), "lessThan 9");
    check(!n2.lessThan(n2), "lessThan 10");
    check(!p2.lessThan(p1), "lessThan 11");
    check(!n1.lessThan(p1), "lessThan 12");
    check(!n2.lessThan(p1), "lessThan 13");
    check(!n1.lessThan(p2), "lessThan 14");
    check(!n2.lessThan(p2), "lessThan 15");
    check(!n2.lessThan(n1), "lessThan 16");
    check(p1.lessThanOrEqual(p1), "lessThanOrEqual 1");
    check(p2.lessThanOrEqual(p2), "lessThanOrEqual 2");
    check(n1.lessThanOrEqual(n1), "lessThanOrEqual 3");
    check(n2.lessThanOrEqual(n2), "lessThanOrEqual 4");
    check(p1.lessThanOrEqual(p2), "lessThanOrEqual 5");
    check(p1.lessThanOrEqual(n1), "lessThanOrEqual 6");
    check(p1.lessThanOrEqual(n2), "lessThanOrEqual 7");
    check(p2.lessThanOrEqual(n1), "lessThanOrEqual 8");
    check(p2.lessThanOrEqual(n2), "lessThanOrEqual 9");
    check(n1.lessThanOrEqual(n2), "lessThanOrEqual 10");
    check(!p2.lessThanOrEqual(p1), "lessThanOrEqual 11");
    check(!n1.lessThanOrEqual(p1), "lessThanOrEqual 12");
    check(!n2.lessThanOrEqual(p1), "lessThanOrEqual 13");
    check(!n1.lessThanOrEqual(p2), "lessThanOrEqual 14");
    check(!n2.lessThanOrEqual(p2), "lessThanOrEqual 15");
    check(!n2.lessThanOrEqual(n1), "lessThanOrEqual 16");
    check(n2.greaterThan(p1), "greaterThan 1");
    check(n2.greaterThan(p2), "greaterThan 2");
    check(n2.greaterThan(n1), "greaterThan 3");
    check(n1.greaterThan(p1), "greaterThan 4");
    check(n1.greaterThan(p2), "greaterThan 5");
    check(p2.greaterThan(p1), "greaterThan 6");
    check(!p1.greaterThan(p1), "greaterThan 7");
    check(!p2.greaterThan(p2), "greaterThan 8");
    check(!n1.greaterThan(n1), "greaterThan 9");
    check(!n2.greaterThan(n2), "greaterThan 10");
    check(!p1.greaterThan(n2), "greaterThan 11");
    check(!p2.greaterThan(n2), "greaterThan 12");
    check(!n1.greaterThan(n2), "greaterThan 13");
    check(!p1.greaterThan(n1), "greaterThan 14");
    check(!p2.greaterThan(n1), "greaterThan 15");
    check(!p1.greaterThan(p2), "greaterThan 16");
    check(p1.greaterThanOrEqual(p1), "greaterThanOrEqual 1");
    check(p2.greaterThanOrEqual(p2), "greaterThanOrEqual 2");
    check(n1.greaterThanOrEqual(n1), "greaterThanOrEqual 3");
    check(n2.greaterThanOrEqual(n2), "greaterThanOrEqual 4");
    check(n2.greaterThanOrEqual(p1), "greaterThanOrEqual 5");
    check(n2.greaterThanOrEqual(p2), "greaterThanOrEqual 6");
    check(n2.greaterThanOrEqual(n1), "greaterThanOrEqual 7");
    check(n1.greaterThanOrEqual(p1), "greaterThanOrEqual 8");
    check(n1.greaterThanOrEqual(p2), "greaterThanOrEqual 9");
    check(p2.greaterThanOrEqual(p1), "greaterThanOrEqual 10");
    check(!p1.greaterThanOrEqual(n2), "greaterThanOrEqual 11");
    check(!p2.greaterThanOrEqual(n2), "greaterThanOrEqual 12");
    check(!n1.greaterThanOrEqual(n2), "greaterThanOrEqual 13");
    check(!p1.greaterThanOrEqual(n1), "greaterThanOrEqual 14");
    check(!p2.greaterThanOrEqual(n1), "greaterThanOrEqual 15");
    check(!p1.greaterThanOrEqual(p2), "greaterThanOrEqual 16");
    System.err.println("ProcAddress: all tests passed successfully.");
  }
}
