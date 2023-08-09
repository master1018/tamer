public class BasicFieldWrapper implements Field {
  protected Field field;
  public BasicFieldWrapper(Field field) {
    this.field = field;
  }
  public String getName() {
    return field.getName();
  }
  public Type getType() {
    return field.getType();
  }
  public long getSize() {
    return field.getSize();
  }
  public boolean isStatic() {
    return field.isStatic();
  }
  public long getOffset() throws WrongTypeException {
    return field.getOffset();
  }
  public Address getStaticFieldAddress() throws WrongTypeException {
    return field.getStaticFieldAddress();
  }
  public boolean    getJBoolean (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJBoolean(addr);
  }
  public byte       getJByte    (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJByte(addr);
  }
  public char       getJChar    (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJChar(addr);
  }
  public double     getJDouble  (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJDouble(addr);
  }
  public float      getJFloat   (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJFloat(addr);
  }
  public int        getJInt     (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJInt(addr);
  }
  public long       getJLong    (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJLong(addr);
  }
  public short      getJShort   (Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJShort(addr);
  }
  public long       getCInteger (Address addr, CIntegerType type) throws UnmappedAddressException, UnalignedAddressException {
    return field.getCInteger(addr, type);
  }
  public Address    getAddress  (Address addr) throws UnmappedAddressException, UnalignedAddressException {
    return field.getAddress(addr);
  }
  public OopHandle  getOopHandle(Address addr)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    return field.getOopHandle(addr);
  }
  public OopHandle  getNarrowOopHandle(Address addr)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    return field.getNarrowOopHandle(addr);
  }
  public boolean    getJBoolean () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJBoolean();
  }
  public byte       getJByte    () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJByte();
  }
  public char       getJChar    () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJChar();
  }
  public double     getJDouble  () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJDouble();
  }
  public float      getJFloat   () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJFloat();
  }
  public int        getJInt     () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJInt();
  }
  public long       getJLong    () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJLong();
  }
  public short      getJShort   () throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getJShort();
  }
  public long       getCInteger (CIntegerType type) throws UnmappedAddressException, UnalignedAddressException {
    return field.getCInteger(type);
  }
  public Address    getAddress  () throws UnmappedAddressException, UnalignedAddressException {
    return field.getAddress();
  }
  public OopHandle  getOopHandle()
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    return field.getOopHandle();
  }
  public OopHandle  getNarrowOopHandle()
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    return field.getNarrowOopHandle();
  }
}
