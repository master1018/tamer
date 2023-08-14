public class BasicJFloatField extends BasicField implements JFloatField {
  public BasicJFloatField(BasicTypeDataBase db, Type containingType, String name, Type type,
                          boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJFloatType())) {
      throw new WrongTypeException("Type of a BasicJFloatField must be equal to TypeDataBase.getJFloatType()");
    }
  }
  public float getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJFloat(addr);
  }
  public float getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJFloat();
  }
}
