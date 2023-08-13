public class BasicJIntField extends BasicField implements JIntField {
  public BasicJIntField(BasicTypeDataBase db, Type containingType, String name, Type type,
                          boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJIntType())) {
      throw new WrongTypeException("Type of a BasicJIntField must be equal to TypeDataBase.getJIntType()");
    }
  }
  public int getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJInt(addr);
  }
  public int getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJInt();
  }
}
