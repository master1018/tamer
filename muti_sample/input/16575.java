public class BasicNarrowOopField extends BasicOopField implements NarrowOopField {
  private static final boolean DEBUG = false;
  public BasicNarrowOopField (OopField oopf) {
    super(oopf);
  }
  public BasicNarrowOopField(BasicTypeDataBase db, Type containingType, String name, Type type,
                       boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);
    if (DEBUG) {
      System.out.println(" name " + name + " type " + type + " isStatic " + isStatic + " offset " + offset + " static addr " + staticFieldAddress);
    }
    if (!type.isOopType()) {
      throw new WrongTypeException("Type of a BasicOopField must be an oop type");
    }
  }
  public OopHandle getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getNarrowOopHandle(addr);
  }
  public OopHandle getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getNarrowOopHandle();
  }
}
