class UnboundedFifoByteBuffer {
    protected byte[] buffer;
    protected int head;
    protected int tail;
    public UnboundedFifoByteBuffer() {
        this(32);
    }
    public UnboundedFifoByteBuffer(int initialSize) {
        if (initialSize <= 0) {
            throw new IllegalArgumentException("The size must be greater than 0");
        }
        buffer = new byte[initialSize + 1];
        head = 0;
        tail = 0;
    }
    public int size() {
        int size = 0;
        if (tail < head) {
            size = buffer.length - head + tail;
        } else {
            size = tail - head;
        }
        return size;
    }
    public boolean isEmpty() {
        return (size() == 0);
    }
    public boolean add(final byte b) {
        if (size() + 1 >= buffer.length) {
            byte[] tmp = new byte[((buffer.length - 1) * 2) + 1];
            int j = 0;
            for (int i = head; i != tail;) {
                tmp[j] = buffer[i];
                buffer[i] = 0;
                j++;
                i++;
                if (i == buffer.length) {
                    i = 0;
                }
            }
            buffer = tmp;
            head = 0;
            tail = j;
        }
        buffer[tail] = b;
        tail++;
        if (tail >= buffer.length) {
            tail = 0;
        }
        return true;
    }
    public byte get() {
        if (isEmpty()) {
            throw new IllegalStateException("The buffer is already empty");
        }
        return buffer[head];
    }
    public byte remove() {
        if (isEmpty()) {
            throw new IllegalStateException("The buffer is already empty");
        }
        byte element = buffer[head];
        head++;
        if (head >= buffer.length) {
            head = 0;
        }
        return element;
    }
    private int increment(int index) {
        index++;
        if (index >= buffer.length) {
            index = 0;
        }
        return index;
    }
    private int decrement(int index) {
        index--;
        if (index < 0) {
            index = buffer.length - 1;
        }
        return index;
    }
    public Iterator iterator() {
        return new Iterator() {
            private int index = head;
            private int lastReturnedIndex = -1;
            public boolean hasNext() {
                return index != tail;
            }
            public Object next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                lastReturnedIndex = index;
                index = increment(index);
                return new Byte(buffer[lastReturnedIndex]);
            }
            public void remove() {
                if (lastReturnedIndex == -1) {
                    throw new IllegalStateException();
                }
                if (lastReturnedIndex == head) {
                    UnboundedFifoByteBuffer.this.remove();
                    lastReturnedIndex = -1;
                    return;
                }
                int i = lastReturnedIndex + 1;
                while (i != tail) {
                    if (i >= buffer.length) {
                        buffer[i - 1] = buffer[0];
                        i = 0;
                    } else {
                        buffer[i - 1] = buffer[i];
                        i++;
                    }
                }
                lastReturnedIndex = -1;
                tail = decrement(tail);
                buffer[tail] = 0;
                index = decrement(index);
            }
        };
    }
}