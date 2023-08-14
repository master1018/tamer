public abstract class FileLock {
    private final FileChannel channel;
    private final long position;
    private final long size;
    private final boolean shared;
    protected FileLock(FileChannel channel, long position, long size,
            boolean shared) {
        super();
        if (position < 0 || size < 0 || position + size < 0) {
            throw new IllegalArgumentException();
        }
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }
    public final FileChannel channel() {
        return channel;
    }
    public final long position() {
        return position;
    }
    public final long size() {
        return size;
    }
    public final boolean isShared() {
        return shared;
    }
    public final boolean overlaps(long start, long length) {
        final long end = position + size - 1;
        final long newEnd = start + length - 1;
        if (end < start || position > newEnd) {
            return false;
        }
        return true;
    }
    public abstract boolean isValid();
    public abstract void release() throws IOException;
    @Override
    @SuppressWarnings("nls")
    public final String toString() {
        StringBuilder buffer = new StringBuilder(64); 
        buffer.append("FileLock: [position=");
        buffer.append(position);
        buffer.append(", size=");
        buffer.append(size);
        buffer.append(", shared=");
        buffer.append(Boolean.toString(shared));
        buffer.append("]");
        return buffer.toString();
    }
}
