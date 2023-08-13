class ByteArrayBuilder {
    private static final int DEFAULT_CAPACITY = 8192;
    private static final LinkedList<SoftReference<Chunk>> sPool =
            new LinkedList<SoftReference<Chunk>>();
    private static final ReferenceQueue<Chunk> sQueue =
            new ReferenceQueue<Chunk>();
    private LinkedList<Chunk> mChunks;
    public ByteArrayBuilder() {
        mChunks = new LinkedList<Chunk>();
    }
    public synchronized void append(byte[] array, int offset, int length) {
        while (length > 0) {
            Chunk c = null;
            if (mChunks.isEmpty()) {
                c = obtainChunk(length);
                mChunks.addLast(c);
            } else {
                c = mChunks.getLast();
                if (c.mLength == c.mArray.length) {
                    c = obtainChunk(length);
                    mChunks.addLast(c);
                }
            }
            int amount = Math.min(length, c.mArray.length - c.mLength);
            System.arraycopy(array, offset, c.mArray, c.mLength, amount);
            c.mLength += amount;
            length -= amount;
            offset += amount;
        }
    }
    public synchronized Chunk getFirstChunk() {
        if (mChunks.isEmpty()) return null;
        return mChunks.removeFirst();
    }
    public synchronized boolean isEmpty() {
        return mChunks.isEmpty();
    }
    public synchronized int getByteSize() {
        int total = 0;
        ListIterator<Chunk> it = mChunks.listIterator(0);
        while (it.hasNext()) {
            Chunk c = it.next();
            total += c.mLength;
        }
        return total;
    }
    public synchronized void clear() {
        Chunk c = getFirstChunk();
        while (c != null) {
            c.release();
            c = getFirstChunk();
        }
    }
    private void processPoolLocked() {
        while (true) {
            SoftReference<Chunk> entry = (SoftReference<Chunk>) sQueue.poll();
            if (entry == null) {
                break;
            }
            sPool.remove(entry);
        }
    }
    private Chunk obtainChunk(int length) {
        if (length < DEFAULT_CAPACITY) {
            length = DEFAULT_CAPACITY;
        }
        synchronized (sPool) {
            processPoolLocked();
            if (!sPool.isEmpty()) {
                Chunk c = sPool.removeFirst().get();
                if (c != null) {
                    return c;
                }
            }
            return new Chunk(length);
        }
    }
    public static class Chunk {
        public byte[]  mArray;
        public int     mLength;
        public Chunk(int length) {
            mArray = new byte[length];
            mLength = 0;
        }
        public void release() {
            mLength = 0;
            synchronized (sPool) {
                sPool.offer(new SoftReference<Chunk>(this, sQueue));
                sPool.notifyAll();
            }
        }
    }
}
