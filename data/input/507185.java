public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {
    public static IntBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        return BufferFactory.newIntBuffer(capacity);
    }
    public static IntBuffer wrap(int[] array) {
        return wrap(array, 0, array.length);
    }
    public static IntBuffer wrap(int[] array, int start, int len) {
        if (array == null) {
            throw new NullPointerException();
        }
        if (start < 0 || len < 0 || (long) len + (long) start > array.length) {
            throw new IndexOutOfBoundsException();
        }
        IntBuffer buf = BufferFactory.newIntBuffer(array);
        buf.position = start;
        buf.limit = start + len;
        return buf;
    }
    IntBuffer(int capacity) {
        super(capacity);
        _elementSizeShift = 2;
    }
    public final int[] array() {
        return protectedArray();
    }
    public final int arrayOffset() {
        return protectedArrayOffset();
    }
    @Override Object _array() {
        if (hasArray()) {
            return array();
        }
        return null;
    }
    @Override int _arrayOffset() {
        if (hasArray()) {
            return arrayOffset();
        }
        return 0;
    }
    public abstract IntBuffer asReadOnlyBuffer();
    public abstract IntBuffer compact();
    public int compareTo(IntBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        int thisInt, otherInt;
        while (compareRemaining > 0) {
            thisInt = get(thisPos);
            otherInt = otherBuffer.get(otherPos);
            if (thisInt != otherInt) {
                return thisInt < otherInt ? -1 : 1;
            }
            thisPos++;
            otherPos++;
            compareRemaining--;
        }
        return remaining() - otherBuffer.remaining();
    }
    public abstract IntBuffer duplicate();
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IntBuffer)) {
            return false;
        }
        IntBuffer otherBuffer = (IntBuffer) other;
        if (remaining() != otherBuffer.remaining()) {
            return false;
        }
        int myPosition = position;
        int otherPosition = otherBuffer.position;
        boolean equalSoFar = true;
        while (equalSoFar && (myPosition < limit)) {
            equalSoFar = get(myPosition++) == otherBuffer.get(otherPosition++);
        }
        return equalSoFar;
    }
    public abstract int get();
    public IntBuffer get(int[] dest) {
        return get(dest, 0, dest.length);
    }
    public IntBuffer get(int[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long) len + (long) off > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = off; i < off + len; i++) {
            dest[i] = get();
        }
        return this;
    }
    public abstract int get(int index);
    public final boolean hasArray() {
        return protectedHasArray();
    }
    @Override
    public int hashCode() {
        int myPosition = position;
        int hash = 0;
        while (myPosition < limit) {
            hash = hash + get(myPosition++);
        }
        return hash;
    }
    public abstract boolean isDirect();
    public abstract ByteOrder order();
    abstract int[] protectedArray();
    abstract int protectedArrayOffset();
    abstract boolean protectedHasArray();
    public abstract IntBuffer put(int i);
    public final IntBuffer put(int[] src) {
        return put(src, 0, src.length);
    }
    public IntBuffer put(int[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long) len + (long) off > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = off; i < off + len; i++) {
            put(src[i]);
        }
        return this;
    }
    public IntBuffer put(IntBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        int[] contents = new int[src.remaining()];
        src.get(contents);
        put(contents);
        return this;
    }
    public abstract IntBuffer put(int index, int i);
    public abstract IntBuffer slice();
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getClass().getName());
        buf.append(", status: capacity="); 
        buf.append(capacity());
        buf.append(" position="); 
        buf.append(position());
        buf.append(" limit="); 
        buf.append(limit());
        return buf.toString();
    }
}
