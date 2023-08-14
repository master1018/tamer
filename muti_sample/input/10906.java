public class NarrowOopField extends OopField {
  public NarrowOopField(FieldIdentifier id, long offset, boolean isVMField) {
    super(id, offset, isVMField);
  }
  public NarrowOopField(sun.jvm.hotspot.types.OopField vmField, long startOffset) {
    super(new NamedFieldIdentifier(vmField.getName()), vmField.getOffset() + startOffset, true);
  }
  public NarrowOopField(InstanceKlass holder, int fieldArrayIndex) {
    super(holder, fieldArrayIndex);
  }
  public Oop getValue(Oop obj) {
    return obj.getHeap().newOop(getValueAsOopHandle(obj));
  }
  public OopHandle getValueAsOopHandle(Oop obj) {
    return obj.getHandle().getCompOopHandleAt(getOffset());
  }
  public void setValue(Oop obj) throws MutationException {
  }
}
