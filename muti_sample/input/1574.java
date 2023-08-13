public class BasicJShortField extends BasicField implements JShortField {
  public BasicJShortField(BasicTypeDataBase db, Type containingType, String name, Type type,
                          boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJShortType())) {
      throw new WrongTypeException("Type of a BasicJShortField must be equal to TypeDataBase.getJShortType()");
    }
  }
  public short getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJShort(addr);
  }
  public short getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJShort();
  }
}
