public class IntField extends Field {
  public IntField(FieldIdentifier id, long offset, boolean isVMField) {
    super(id, offset, isVMField);
  }
  public IntField(sun.jvm.hotspot.types.JIntField vmField, long startOffset) {
    super(new NamedFieldIdentifier(vmField.getName()), vmField.getOffset() + startOffset, true);
  }
  public IntField(InstanceKlass holder, int fieldArrayIndex) {
    super(holder, fieldArrayIndex);
  }
  public int getValue(Oop obj) {
    if (!isVMField() && !obj.isInstance() && !obj.isArray()) {
      throw new InternalError(obj.toString());
    }
    return obj.getHandle().getJIntAt(getOffset());
  }
  public void setValue(Oop obj, int value) throws MutationException {
  }
}
