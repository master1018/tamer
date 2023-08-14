public final class WriteOnlyFileChannel extends FileChannelImpl {
    private boolean append = false;
    public WriteOnlyFileChannel(Object stream, int handle) {
        super(stream, handle);
    }
    public WriteOnlyFileChannel(Object stream, int handle, boolean isAppend) {
        super(stream, handle);
        append = isAppend;
    }
    public long position() throws IOException {
        return append ? size() : super.position();
    }
    public long transferTo(long position, long count, WritableByteChannel target)
            throws IOException {
        openCheck();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        }
        throw new NonReadableChannelException();
    }
    public long read(ByteBuffer[] buffers, int offset, int length)
            throws IOException {
        if (offset < 0 || length < 0 || offset + length > buffers.length) {
            throw new IndexOutOfBoundsException();
        }
        openCheck();
        throw new NonReadableChannelException();
    }
    public int read(ByteBuffer buffer) throws IOException {
        openCheck();
        throw new NonReadableChannelException();
    }
    public int read(ByteBuffer buffer, long position) throws IOException {
        if (null == buffer) {
            throw new NullPointerException();
        }
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        throw new NonReadableChannelException();
    }
    public MappedByteBuffer map(MapMode mode, long position, long size)
            throws IOException {
        openCheck();
        if (mode == null) {
            throw new NullPointerException();
        }
        if (position < 0 || size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        throw new NonReadableChannelException();
    }
    public int write(ByteBuffer buffer) throws IOException {
        if (append) {
            position(size());
        }
        return super.write(buffer);
    }
    protected final FileLock basicLock(long position, long size,
            boolean shared, boolean wait) throws IOException {
        if (shared) {
            throw new NonReadableChannelException();
        }
        return super.basicLock(position, size, shared, wait);
    }
}
