public class BasicIntType extends BasicType implements IntType {
  private boolean unsigned;
  public BasicIntType(String name, int size, boolean unsigned) {
    this(name, size, unsigned, 0);
  }
  protected BasicIntType(String name, int size, boolean unsigned, int cvAttributes) {
    super(name, size, cvAttributes);
    this.unsigned = unsigned;
  }
  public IntType asInt()      { return this; }
  public int     getIntSize() { return getSize(); }
  public boolean isUnsigned() { return unsigned; }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
    v.doInt(f, a.getCIntegerAt(0, getSize(), isUnsigned()));
  }
  protected Type createCVVariant(int cvAttributes) {
    return new BasicIntType(getName(), getSize(), isUnsigned(), cvAttributes);
  }
  public void visit(TypeVisitor v) {
    v.doIntType(this);
  }
}
