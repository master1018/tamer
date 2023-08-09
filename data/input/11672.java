public class CIntField extends Field {
  public CIntField(sun.jvm.hotspot.types.CIntegerField vmField, long startOffset) {
    super(new NamedFieldIdentifier(vmField.getName()), vmField.getOffset() + startOffset, true);
    size       = vmField.getSize();
    isUnsigned = ((sun.jvm.hotspot.types.CIntegerType) vmField.getType()).isUnsigned();
  }
  private long size;
  private boolean isUnsigned;
  public long getValue(Oop obj) {
    return obj.getHandle().getCIntegerAt(getOffset(), size, isUnsigned);
  }
  public void setValue(Oop obj, long value) throws MutationException {
  }
}
