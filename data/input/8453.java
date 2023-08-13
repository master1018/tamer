class Win32OopHandle extends Win32Address implements OopHandle {
  Win32OopHandle(Win32Debugger debugger, long addr) {
    super(debugger, addr);
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!(arg instanceof Win32OopHandle)) {
      return false;
    }
    return (addr == ((Win32Address) arg).addr);
  }
  public Address    addOffsetTo       (long offset) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("addOffsetTo not applicable to OopHandles (interior object pointers not allowed)");
  }
  public Address    andWithMask(long mask) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("andWithMask not applicable to OopHandles (i.e., anything but C addresses)");
  }
  public Address    orWithMask(long mask) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("orWithMask not applicable to OopHandles (i.e., anything but C addresses)");
  }
  public Address    xorWithMask(long mask) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("xorWithMask not applicable to OopHandles (i.e., anything but C addresses)");
  }
}
