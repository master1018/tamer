public class FloatField extends Field {
  public FloatField(FieldIdentifier id, long offset, boolean isVMField) {
    super(id, offset, isVMField);
  }
  public FloatField(sun.jvm.hotspot.types.JFloatField vmField, long startOffset) {
    super(new NamedFieldIdentifier(vmField.getName()), vmField.getOffset() + startOffset, true);
  }
  public FloatField(InstanceKlass holder, int fieldArrayIndex) {
    super(holder, fieldArrayIndex);
  }
  public float getValue(Oop obj) { return obj.getHandle().getJFloatAt(getOffset()); }
  public void setValue(Oop obj, float value) throws MutationException {
  }
}
