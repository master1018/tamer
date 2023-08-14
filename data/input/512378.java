public abstract class ShortBuffer extends Buffer implements
        Comparable<ShortBuffer> {
    public static ShortBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        return BufferFactory.newShortBuffer(capacity);
    }
    public static ShortBuffer wrap(short[] array) {
        return wrap(array, 0, array.length);
    }
    public static ShortBuffer wrap(short[] array, int start, int len) {
        if (array == null) {
            throw new NullPointerException();
        }
        if (start < 0 || len < 0 || (long) start + (long) len > array.length) {
            throw new IndexOutOfBoundsException();
        }
        ShortBuffer buf = BufferFactory.newShortBuffer(array);
        buf.position = start;
        buf.limit = start + len;
        return buf;
    }
    ShortBuffer(int capacity) {
        super(capacity);
        _elementSizeShift = 1;
    }
    public final short[] array() {
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
    public abstract ShortBuffer asReadOnlyBuffer();
    public abstract ShortBuffer compact();
    public int compareTo(ShortBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        short thisByte, otherByte;
        while (compareRemaining > 0) {
            thisByte = get(thisPos);
            otherByte = otherBuffer.get(otherPos);
            if (thisByte != otherByte) {
                return thisByte < otherByte ? -1 : 1;
            }
            thisPos++;
            otherPos++;
            compareRemaining--;
        }
        return remaining() - otherBuffer.remaining();
    }
    public abstract ShortBuffer duplicate();
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer otherBuffer = (ShortBuffer) other;
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
    public abstract short get();
    public ShortBuffer get(short[] dest) {
        return get(dest, 0, dest.length);
    }
    public ShortBuffer get(short[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long) off + (long) len > length) {
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
    public abstract short get(int index);
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
    abstract short[] protectedArray();
    abstract int protectedArrayOffset();
    abstract boolean protectedHasArray();
    public abstract ShortBuffer put(short s);
    public final ShortBuffer put(short[] src) {
        return put(src, 0, src.length);
    }
    public ShortBuffer put(short[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long) off + (long) len > length) {
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
    public ShortBuffer put(ShortBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        short[] contents = new short[src.remaining()];
        src.get(contents);
        put(contents);
        return this;
    }
    public abstract ShortBuffer put(int index, short s);
    public abstract ShortBuffer slice();
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
