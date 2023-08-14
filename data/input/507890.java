public class AtomicReferenceArray<E> implements java.io.Serializable {
    private static final long serialVersionUID = -6209656149925076980L;
    private static final Unsafe unsafe = UnsafeAccess.THE_ONE;
    private static final int base = unsafe.arrayBaseOffset(Object[].class);
    private static final int scale = unsafe.arrayIndexScale(Object[].class);
    private final Object[] array;
    private long rawIndex(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);
        return base + (long) i * scale;
    }
    public AtomicReferenceArray(int length) {
        array = new Object[length];
        if (length > 0)
            unsafe.putObjectVolatile(array, rawIndex(0), null);
    }
    public AtomicReferenceArray(E[] array) {
        if (array == null)
            throw new NullPointerException();
        int length = array.length;
        this.array = new Object[length];
        if (length > 0) {
            int last = length-1;
            for (int i = 0; i < last; ++i)
                this.array[i] = array[i];
            E e = array[last];
            unsafe.putObjectVolatile(this.array, rawIndex(last), e);
        }
    }
    public final int length() {
        return array.length;
    }
    public final E get(int i) {
        return (E) unsafe.getObjectVolatile(array, rawIndex(i));
    }
    public final void set(int i, E newValue) {
        unsafe.putObjectVolatile(array, rawIndex(i), newValue);
    }
    public final E getAndSet(int i, E newValue) {
        while (true) {
            E current = get(i);
            if (compareAndSet(i, current, newValue))
                return current;
        }
    }
    public final boolean compareAndSet(int i, E expect, E update) {
        return unsafe.compareAndSwapObject(array, rawIndex(i),
                                         expect, update);
    }
    public final boolean weakCompareAndSet(int i, E expect, E update) {
        return compareAndSet(i, expect, update);
    }
    public String toString() {
        if (array.length > 0) 
            get(0);
        return Arrays.toString(array);
    }
}
