public class BasicJByteField extends BasicField implements JByteField {
  public BasicJByteField(BasicTypeDataBase db, Type containingType, String name, Type type,
                         boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJByteType())) {
      throw new WrongTypeException("Type of a BasicJByteField must be db.getJByteType()");
    }
  }
  public byte getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJByte(addr);
  }
  public byte getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJByte();
  }
}
