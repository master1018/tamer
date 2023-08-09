public class AtomicReferenceArray<E> implements java.io.Serializable {
    private static final long serialVersionUID = -6209656149925076980L;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(Object[].class);
    private static final int shift;
    private final Object[] array;
    static {
        int scale = unsafe.arrayIndexScale(Object[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }
    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);
        return byteOffset(i);
    }
    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }
    public AtomicReferenceArray(int length) {
        array = new Object[length];
    }
    public AtomicReferenceArray(E[] array) {
        this.array = array.clone();
    }
    public final int length() {
        return array.length;
    }
    public final E get(int i) {
        return getRaw(checkedByteOffset(i));
    }
    private E getRaw(long offset) {
        return (E) unsafe.getObjectVolatile(array, offset);
    }
    public final void set(int i, E newValue) {
        unsafe.putObjectVolatile(array, checkedByteOffset(i), newValue);
    }
    public final void lazySet(int i, E newValue) {
        unsafe.putOrderedObject(array, checkedByteOffset(i), newValue);
    }
    public final E getAndSet(int i, E newValue) {
        long offset = checkedByteOffset(i);
        while (true) {
            E current = (E) getRaw(offset);
            if (compareAndSetRaw(offset, current, newValue))
                return current;
        }
    }
    public final boolean compareAndSet(int i, E expect, E update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }
    private boolean compareAndSetRaw(long offset, E expect, E update) {
        return unsafe.compareAndSwapObject(array, offset, expect, update);
    }
    public final boolean weakCompareAndSet(int i, E expect, E update) {
        return compareAndSet(i, expect, update);
    }
    public String toString() {
           int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }
}
