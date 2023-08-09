public class BasicJLongField extends BasicField implements JLongField {
  public BasicJLongField(BasicTypeDataBase db, Type containingType, String name, Type type,
                          boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJLongType())) {
      throw new WrongTypeException("Type of a BasicJLongField must be equal to TypeDataBase.getJLongType()");
    }
  }
  public long getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJLong(addr);
  }
  public long getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJLong();
  }
}
