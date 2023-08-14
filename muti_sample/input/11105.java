public class BasicCIntegerField extends BasicField implements CIntegerField {
  private CIntegerType intType;
  public BasicCIntegerField(BasicTypeDataBase db, Type containingType, String name, Type type,
                            boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (!(type instanceof CIntegerType)) {
      throw new WrongTypeException("Type of a BasicCIntegerField must be a CIntegerType");
    }
    intType = (CIntegerType) type;
  }
  public boolean isUnsigned() {
    return intType.isUnsigned();
  }
  public long getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getCInteger(addr, intType);
  }
  public long getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getCInteger(intType);
  }
}
