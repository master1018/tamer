public class BasicJBooleanField extends BasicField implements JBooleanField {
  public BasicJBooleanField(BasicTypeDataBase db, Type containingType, String name, Type type,
                         boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJBooleanType())) {
      throw new WrongTypeException("Type of a BasicJBooleanField must be db.getJBooleanType()");
    }
  }
  public boolean getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJBoolean(addr);
  }
  public boolean getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJBoolean();
  }
}
