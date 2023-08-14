public class BasicJCharField extends BasicField implements JCharField {
  public BasicJCharField(BasicTypeDataBase db, Type containingType, String name, Type type,
                         boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJCharType())) {
      throw new WrongTypeException("Type of a BasicJCharField must be db.getJCharType()");
    }
  }
  public char getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJChar(addr);
  }
  public char getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJChar();
  }
}
