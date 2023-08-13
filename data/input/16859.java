public class BasicOopField extends BasicField implements OopField {
  public BasicOopField(OopField oopf) {
    super(oopf);
  }
  public BasicOopField(BasicTypeDataBase db, Type containingType, String name, Type type,
                       boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!type.isOopType()) {
      throw new WrongTypeException("Type of a BasicOopField must be an oop type");
    }
  }
  public OopHandle getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getOopHandle(addr);
  }
  public OopHandle getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getOopHandle();
  }
}
