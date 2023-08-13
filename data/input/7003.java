public class CharField extends Field {
  public CharField(FieldIdentifier id, long offset, boolean isVMField) {
    super(id, offset, isVMField);
  }
  public CharField(sun.jvm.hotspot.types.JCharField vmField, long startOffset) {
    super(new NamedFieldIdentifier(vmField.getName()), vmField.getOffset() + startOffset, true);
  }
  public CharField(InstanceKlass holder, int fieldArrayIndex) {
    super(holder, fieldArrayIndex);
  }
  public char getValue(Oop obj) { return obj.getHandle().getJCharAt(getOffset()); }
  public void setValue(Oop obj, char value) throws MutationException {
  }
}
