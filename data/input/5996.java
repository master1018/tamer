public class BasicVoidType extends BasicType implements VoidType {
  public BasicVoidType() {
    super("void", 0);
  }
  public VoidType asVoid() { return this; }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {}
  protected Type createCVVariant(int cvAttributes) {
    System.err.println("WARNING: Should not attempt to create const/volatile variants for void type");
    return this;
  }
  public void visit(TypeVisitor v) {
    v.doVoidType(this);
  }
}
