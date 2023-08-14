public class DefaultOopVisitor implements OopVisitor {
  private Oop obj;
  public void prologue()                        {}
  public void epilogue()                        {}
  public void setObj(Oop obj) {
    this.obj = obj;
  }
  public Oop getObj() {
    return obj;
  }
  public void doOop(OopField field, boolean isVMField)         {}
  public void doOop(NarrowOopField field, boolean isVMField)   {}
  public void doByte(ByteField field, boolean isVMField)       {}
  public void doChar(CharField field, boolean isVMField)       {}
  public void doBoolean(BooleanField field, boolean isVMField) {}
  public void doShort(ShortField field, boolean isVMField)     {}
  public void doInt(IntField field, boolean isVMField)         {}
  public void doLong(LongField field, boolean isVMField)       {}
  public void doFloat(FloatField field, boolean isVMField)     {}
  public void doDouble(DoubleField field, boolean isVMField)   {}
  public void doCInt(CIntField field, boolean isVMField)       {}
}
