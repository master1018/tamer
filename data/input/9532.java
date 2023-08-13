public class DefaultObjectVisitor implements ObjectVisitor {
  public void enterType(Type type, Address objectAddress) {}
  public void exitType() {}
  public void doBit(FieldIdentifier f, long val) {}
  public void doInt(FieldIdentifier f, long val) {}
  public void doEnum(FieldIdentifier f, long val, String enumName) {}
  public void doFloat(FieldIdentifier f, float val) {}
  public void doDouble(FieldIdentifier f, double val) {}
  public void doPointer(FieldIdentifier f, Address val) {}
  public void doArray(FieldIdentifier f, Address val) {}
  public void doRef(FieldIdentifier f, Address val) {}
  public void doCompound(FieldIdentifier f, Address addressOfEmbeddedCompoundObject) {}
}
