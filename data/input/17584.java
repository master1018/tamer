public class AtomicReference<V>  implements java.io.Serializable {
    private static final long serialVersionUID = -1848883965231344442L;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    static {
      try {
        valueOffset = unsafe.objectFieldOffset
            (AtomicReference.class.getDeclaredField("value"));
      } catch (Exception ex) { throw new Error(ex); }
    }
    private volatile V value;
    public AtomicReference(V initialValue) {
        value = initialValue;
    }
    public AtomicReference() {
    }
    public final V get() {
        return value;
    }
    public final void set(V newValue) {
        value = newValue;
    }
    public final void lazySet(V newValue) {
        unsafe.putOrderedObject(this, valueOffset, newValue);
    }
    public final boolean compareAndSet(V expect, V update) {
        return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }
    public final boolean weakCompareAndSet(V expect, V update) {
        return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }
    public final V getAndSet(V newValue) {
        while (true) {
            V x = get();
            if (compareAndSet(x, newValue))
                return x;
        }
    }
    public String toString() {
        return String.valueOf(get());
    }
}
