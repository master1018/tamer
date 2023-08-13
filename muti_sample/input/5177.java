public class BasicArrayType extends BasicType implements ArrayType {
  private Type elementType;
  private int  length;
  public BasicArrayType(String name, Type elementType, int sizeInBytes) {
    this(name, elementType, sizeInBytes, 0, 0);
  }
  private BasicArrayType(String name, Type elementType, int sizeInBytes, int length, int cvAttributes) {
    super(name, sizeInBytes, cvAttributes);
    this.elementType = elementType;
    this.length      = length;
  }
  public ArrayType asArray() { return this; }
  public Type getElementType() { return elementType; }
  public int  getLength()      { return length; }
  Type resolveTypes(BasicCDebugInfoDataBase db, ResolveListener listener) {
    super.resolveTypes(db, listener);
    elementType = db.resolveType(this, elementType, listener, "resolving array element type");
    if (!((BasicType) elementType).isLazy()) {
      length = getSize() / elementType.getSize();
    }
    return this;
  }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
    if (f == null) {
      v.enterType(this, a);
      for (int i = 0; i < getLength(); i++) {
        ((BasicType) getElementType()).iterateObject(a.addOffsetTo(i * getElementType().getSize()),
                                                     v,
                                                     new BasicIndexableFieldIdentifier(getElementType(), i));
      }
      v.exitType();
    } else {
      v.doArray(f, a);
    }
  }
  protected Type createCVVariant(int cvAttributes) {
    return new BasicArrayType(getName(), getElementType(), getSize(), getLength(), cvAttributes);
  }
  public void visit(TypeVisitor v) {
    v.doArrayType(this);
  }
}
