public class BasicJDoubleField extends BasicField implements JDoubleField {
  public BasicJDoubleField(BasicTypeDataBase db, Type containingType, String name, Type type,
                           boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.equals(db.getJDoubleType())) {
      throw new WrongTypeException("Type of a BasicJDoubleField must be equal to TypeDataBase.getJDoubleType()");
    }
  }
  public double getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJDouble(addr);
  }
  public double getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getJDouble();
  }
}
