public final class ReadOnlyFileChannel extends FileChannelImpl {
    public ReadOnlyFileChannel(Object stream, int handle) {
        super(stream, handle);
    }
    public final int write(ByteBuffer buffer, long position) throws IOException {
        if (null == buffer) {
            throw new NullPointerException();
        }
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        throw new NonWritableChannelException();
    }
    public final int write(ByteBuffer buffer) throws IOException {
        openCheck();
        throw new NonWritableChannelException();
    }
    public final long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException {
        if (offset < 0 || length < 0 || (offset + length) > buffers.length) {
            throw new IndexOutOfBoundsException();
        }
        openCheck();
        throw new NonWritableChannelException();
    }
    public final FileChannel truncate(long size) throws IOException {
        openCheck();
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        throw new NonWritableChannelException();
    }
    public final long transferFrom(ReadableByteChannel src, long position,
            long count) throws IOException {
        openCheck();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        }
        throw new NonWritableChannelException();
    }
    public final MappedByteBuffer map(MapMode mode, long position, long size)
            throws IOException {
        openCheck();
        if (mode == null) {
            throw new NullPointerException();
        }
        if (position < 0 || size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        if (mode != MapMode.READ_ONLY) {
            throw new NonWritableChannelException();
        }
        return super.mapImpl(IMemorySystem.MMAP_READ_ONLY, position, size);
    }
    public final void force(boolean metadata) throws IOException {
        openCheck();
        return;
    }
    protected final FileLock basicLock(long position, long size,
            boolean shared, boolean wait) throws IOException {
        if (!shared) {
            throw new NonWritableChannelException();
        }
        return super.basicLock(position, size, shared, true);
    }
}
