public abstract class Buffer {
    final static int UNSET_MARK = -1;
    final int capacity;
    int limit;
    int mark = UNSET_MARK;
    int position = 0;
    int _elementSizeShift;
    Object _array() {
        return null;
    }
    int _arrayOffset() {
        return 0;
    }
    int effectiveDirectAddress = 0;
    Buffer(int capacity) {
        super();
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = this.limit = capacity;
    }
    public final int capacity() {
        return capacity;
    }
    public final Buffer clear() {
        position = 0;
        mark = UNSET_MARK;
        limit = capacity;
        return this;
    }
    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = UNSET_MARK;
        return this;
    }
    public final boolean hasRemaining() {
        return position < limit;
    }
    public abstract boolean isReadOnly();
    public final int limit() {
        return limit;
    }
    public final Buffer limit(int newLimit) {
        if (newLimit < 0 || newLimit > capacity) {
            throw new IllegalArgumentException();
        }
        limit = newLimit;
        if (position > newLimit) {
            position = newLimit;
        }
        if ((mark != UNSET_MARK) && (mark > newLimit)) {
            mark = UNSET_MARK;
        }
        return this;
    }
    public final Buffer mark() {
        mark = position;
        return this;
    }
    public final int position() {
        return position;
    }
    public final Buffer position(int newPosition) {
        if (newPosition < 0 || newPosition > limit) {
            throw new IllegalArgumentException();
        }
        position = newPosition;
        if ((mark != UNSET_MARK) && (mark > position)) {
            mark = UNSET_MARK;
        }
        return this;
    }
    public final int remaining() {
        return limit - position;
    }
    public final Buffer reset() {
        if (mark == UNSET_MARK) {
            throw new InvalidMarkException();
        }
        position = mark;
        return this;
    }
    public final Buffer rewind() {
        position = 0;
        mark = UNSET_MARK;
        return this;
    }
}
