public class DoubleField extends Field {
  public DoubleField(FieldIdentifier id, long offset, boolean isVMField) {
    super(id, offset, isVMField);
  }
  public DoubleField(sun.jvm.hotspot.types.JDoubleField vmField, long startOffset) {
    super(new NamedFieldIdentifier(vmField.getName()), vmField.getOffset() + startOffset, true);
  }
  public DoubleField(InstanceKlass holder, int fieldArrayIndex) {
    super(holder, fieldArrayIndex);
  }
  public double getValue(Oop obj) { return obj.getHandle().getJDoubleAt(getOffset()); }
  public void setValue(Oop obj, double value) throws MutationException {
  }
}
