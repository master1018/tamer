class JNIHandleAccessor {
  private Address addr;
  private ObjectHeap heap;
  JNIHandleAccessor(Address addr, ObjectHeap heap) {
    this.addr = addr;
    this.heap = heap;
  }
  Oop getValue() {
    Address handle = addr.getAddressAt(0);
    if (handle == null) return null;
    return heap.newOop(handle.getOopHandleAt(0));
  }
  void setValue(Oop value) {
    Address handle = addr.getAddressAt(0);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(handle != null, "Must have valid global JNI handle for setting");
    }
    handle.setOopHandleAt(0, value.getHandle());
  }
}
