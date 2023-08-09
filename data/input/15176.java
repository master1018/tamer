public class BasicDoubleType extends BasicType implements DoubleType {
  public BasicDoubleType(String name, int size) {
    this(name, size, 0);
  }
  private BasicDoubleType(String name, int size, int cvAttributes) {
    super(name, size, cvAttributes);
  }
  public DoubleType asDouble() { return this; }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
    v.doDouble(f, a.getJDoubleAt(0));
  }
  protected Type createCVVariant(int cvAttributes) {
    return new BasicDoubleType(getName(), getSize(), cvAttributes);
  }
  public void visit(TypeVisitor v) {
    v.doDoubleType(this);
  }
}
