public abstract class FileChannelImpl extends FileChannel {
    private static final IFileSystem fileSystem = Platform.getFileSystem();
    private static final int ALLOC_GRANULARITY;
    static {
        try {
            ALLOC_GRANULARITY = fileSystem.getAllocGranularity();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
    private final int handle;
    private final LockManager lockManager = new LockManager();
    private static class RepositioningLock {}
    private final Object repositioningLock = new RepositioningLock();
    private final Object stream;
    public FileChannelImpl(Object stream, int handle) {
        super();
        this.handle = handle;
        this.stream = stream;
    }
    protected final void openCheck() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }
    protected void implCloseChannel() throws IOException {
        if (stream instanceof Closeable) {
            ((Closeable) stream).close();
        }
    }
    protected FileLock basicLock(long position, long size, boolean shared,
            boolean wait) throws IOException {
        if ((position < 0) || (size < 0)) {
            throw new IllegalArgumentException(Messages.getString("nio.0A")); 
        }
        int lockType = shared ? IFileSystem.SHARED_LOCK_TYPE
                : IFileSystem.EXCLUSIVE_LOCK_TYPE;
        FileLock pendingLock = new FileLockImpl(this, position, size, shared);
        lockManager.addLock(pendingLock);
        if (fileSystem.lock(handle, position, size, lockType, wait)) {
            return pendingLock;
        }
        lockManager.removeLock(pendingLock);
        return null;
    }
    public final FileLock lock(long position, long size, boolean shared)
            throws IOException {
        openCheck();
        FileLock resultLock = null;
        {
            boolean completed = false;
            try {
                begin();
                resultLock = basicLock(position, size, shared, true);
                completed = true;
            } finally {
                end(completed);
            }
        }
        return resultLock;
    }
    public final FileLock tryLock(long position, long size, boolean shared)
            throws IOException {
        openCheck();
        return basicLock(position, size, shared, false);
    }
    void release(FileLock lock) throws IOException {
        openCheck();
        fileSystem.unlock(handle, lock.position(), lock.size());
        lockManager.removeLock(lock);
    }
    public void force(boolean metadata) throws IOException {
        openCheck();
        if (metadata) {
            fileSystem.fflush(handle, metadata);
        }
    }
    public abstract MappedByteBuffer map(MapMode mode, long position, long size)
            throws IOException;
    protected final MappedByteBuffer mapImpl(int mapMode, long position,
            long size) throws IOException {
        if (position + size > size()) {
            fileSystem.truncate(handle, position + size);
        }
        long alignment = position - position % ALLOC_GRANULARITY;
        int offset = (int) (position - alignment);
        PlatformAddress address = PlatformAddressFactory.allocMap(handle,
                alignment, size + offset, mapMode);
        MappedByteBuffer buffer = null;
        try {
            buffer = MappedByteBufferFactory.getBuffer(address, mapMode, size,
                    offset);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return buffer;
    }
    public long position() throws IOException {
        openCheck();
        return fileSystem.seek(handle, 0L, IFileSystem.SEEK_CUR);
    }
    public FileChannel position(long newPosition) throws IOException {
        openCheck();
        if (newPosition < 0) {
            throw new IllegalArgumentException(Messages.getString("nio.0B")); 
        }
        synchronized (repositioningLock) {
            fileSystem.seek(handle, newPosition, IFileSystem.SEEK_SET);
        }
        return this;
    }
    public int read(ByteBuffer buffer, long position) throws IOException {
        if (null == buffer) {
            throw new NullPointerException();
        }
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        openCheck();
        if (!buffer.hasRemaining()) {
            return 0;
        }
        synchronized (repositioningLock) {
            int bytesRead = 0;
            long preReadPosition = position();
            position(position);
            try {
                bytesRead = read(buffer);
            } finally {
                position(preReadPosition);
            }
            return bytesRead;
        }
    }
    public int read(ByteBuffer buffer) throws IOException {
        openCheck();
        if (!buffer.hasRemaining()) {
            return 0;
        }
        boolean completed = false;
        int bytesRead = 0;
        synchronized (repositioningLock) {
            if (buffer.isDirect()) {
                DirectBuffer directBuffer = (DirectBuffer) buffer;
                int address = directBuffer.getEffectiveAddress().toInt();
                try {
                    begin();
                    bytesRead = (int) fileSystem.readDirect(handle, address,
                            buffer.position(), buffer.remaining());
                    completed = true;
                } finally {
                    end(completed && bytesRead >= 0);
                }
            } else {
                try {
                    begin();
                    bytesRead = (int) fileSystem.read(handle, buffer.array(),
                            buffer.arrayOffset() + buffer.position(), buffer
                                    .remaining());
                    completed = true;
                } finally {
                    end(completed && bytesRead >= 0);
                }
            }
            if (bytesRead > 0) {
                buffer.position(buffer.position() + bytesRead);
            }
        }
        return bytesRead;
    }
    public long read(ByteBuffer[] buffers, int offset, int length)
            throws IOException {
        int count = 0;
        if (offset < 0 || length < 0 || offset + length > buffers.length) {
            throw new IndexOutOfBoundsException();
        }
        openCheck();
        for (int i = offset; i < offset + length; i++) {
            count += buffers[i].remaining();
        }
        if (0 == count) {
            return 0;
        }
        if (size() == 0) {
            return -1;
        }
        ByteBuffer[] directBuffers = new ByteBuffer[length];
        int[] handles = new int[length];
        int[] offsets = new int[length];
        int[] lengths = new int[length];
        for (int i = 0; i < length; i++) {
            ByteBuffer buffer = buffers[i + offset];
            if (!buffer.isDirect()) {
                buffer = ByteBuffer.allocateDirect(buffer.remaining());
                directBuffers[i] = buffer;
                offsets[i] = 0;
            } else {
                offsets[i] = buffer.position();
            }
            handles[i] = ((DirectBuffer) buffer).getEffectiveAddress().toInt();
            lengths[i] = buffer.remaining();
        }
        long bytesRead = 0;
        {
            boolean completed = false;
            try {
                begin();
                synchronized (repositioningLock) {
                    bytesRead = fileSystem.readv(handle, handles, offsets,
                            lengths, length);
                }
                completed = true;
            } finally {
                end(completed);
            }
        }
        int end = offset + length;
        long bytesRemaining = bytesRead;
        for (int i = offset; i < end && bytesRemaining > 0; i++) {
            if (buffers[i].isDirect()) {
                if (lengths[i] < bytesRemaining) {
                    int pos = buffers[i].limit();
                    buffers[i].position(pos);
                    bytesRemaining -= lengths[i];
                } else {
                    int pos = (int) bytesRemaining;
                    buffers[i].position(pos);
                    break;
                }
            } else {
                ByteBuffer buf = directBuffers[i - offset];
                if (bytesRemaining < buf.remaining()) {
                    int pos = buf.position();
                    buffers[i].put(buf);
                    buffers[i].position(pos + (int) bytesRemaining);
                    bytesRemaining = 0;
                } else {
                    bytesRemaining -= buf.remaining();
                    buffers[i].put(buf);
                }
            }
        }
        return bytesRead;
    }
    public long size() throws IOException {
        openCheck();
        synchronized (repositioningLock) {
            long currentPosition = fileSystem.seek(handle, 0L,
                    IFileSystem.SEEK_CUR);
            long endOfFilePosition = fileSystem.seek(handle, 0L,
                    IFileSystem.SEEK_END);
            fileSystem.seek(handle, currentPosition, IFileSystem.SEEK_SET);
            return endOfFilePosition;
        }
    }
    public long transferFrom(ReadableByteChannel src, long position, long count)
            throws IOException {
        openCheck();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        }
        if (position < 0 || count < 0 || count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        if (position > size()) {
            return 0;
        }
        ByteBuffer buffer = null;
        try {
            if (src instanceof FileChannel) {
                FileChannel fileSrc = (FileChannel) src;
                long size = fileSrc.size();
                long filePosition = fileSrc.position();
                count = Math.min(count, size - filePosition);
                buffer = fileSrc.map(MapMode.READ_ONLY, filePosition, count);
                fileSrc.position(filePosition + count);
            } else {
                buffer = ByteBuffer.allocateDirect((int) count);
                src.read(buffer);
                buffer.flip();
            }
            return write(buffer, position);
        } finally {
            if (buffer != null) {
                ((DirectBuffer) buffer).free();
            }
        }
    }
    public long transferTo(long position, long count, WritableByteChannel target)
            throws IOException {
        openCheck();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        }
        if (target instanceof ReadOnlyFileChannel) {
            throw new NonWritableChannelException();
        }
        if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        }
        if (count == 0 || position >= size()) {
            return 0;
        }
        ByteBuffer buffer = null;
        count = Math.min(count, size() - position);
        if (target instanceof SocketChannelImpl) {
            return kernelTransfer(handle, ((SocketChannelImpl) target).getFD(),
                    position, count);
        }
        try {
            buffer = map(MapMode.READ_ONLY, position, count);
            return target.write(buffer);
        } finally {
            if (buffer != null) {
                ((DirectBuffer) buffer).free();
            }
        }
    }
    private long kernelTransfer(int l, FileDescriptor fd, long position,
            long count) throws IOException {
        boolean completed = false;
        try {
            begin();
            long ret = fileSystem.transfer(l, fd, position, count);
            completed = true;
            return ret;
        } finally {
            end(completed);
        }
    }
    public FileChannel truncate(long size) throws IOException {
        openCheck();
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        if (size < size()) {
            synchronized (repositioningLock) {
                long position = position();
                fileSystem.truncate(handle, size);
                position(position > size ? size : position);
            }
        }
        return this;
    }
    public int write(ByteBuffer buffer, long position) throws IOException {
        if (null == buffer) {
            throw new NullPointerException();
        }
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        openCheck();
        if (!buffer.hasRemaining()) {
            return 0;
        }
        int bytesWritten = 0;
        synchronized (repositioningLock) {
            long preWritePosition = position();
            position(position);
            try {
                bytesWritten = writeImpl(buffer);
            } finally {
                position(preWritePosition);
            }
        }
        return bytesWritten;
    }
    public int write(ByteBuffer buffer) throws IOException {
        openCheck();
        return writeImpl(buffer);
    }
    private int writeImpl(ByteBuffer buffer) throws IOException {
        int bytesWritten;
        boolean completed = false;
        synchronized (repositioningLock) {
            if (buffer.isDirect()) {
                DirectBuffer directBuffer = (DirectBuffer) buffer;
                int address = directBuffer.getEffectiveAddress().toInt();
                try {
                    begin();
                    bytesWritten = (int) fileSystem.writeDirect(handle,
                            address, buffer.position(), buffer.remaining());
                    completed = true;
                } finally {
                    end(completed);
                }
            } else {
                try {
                    begin();
                    bytesWritten = (int) fileSystem.write(handle, buffer
                            .array(), buffer.arrayOffset() + buffer.position(),
                            buffer.remaining());
                    completed = true;
                } finally {
                    end(completed);
                }
            }
            if (bytesWritten > 0) {
                buffer.position(buffer.position() + bytesWritten);
            }
        }
        return bytesWritten;
    }
    public long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException {
        if (offset < 0 || length < 0 || (offset + length) > buffers.length) {
            throw new IndexOutOfBoundsException();
        }
        openCheck();
        int count = 0;
        for (int i = offset; i < offset + length; i++) {
            count += buffers[i].remaining();
        }
        if (0 == count) {
            return 0;
        }
        int[] handles = new int[length];
        int[] offsets = new int[length];
        int[] lengths = new int[length];
        DirectBuffer[] allocatedBufs = new DirectBuffer[length];
        for (int i = 0; i < length; i++) {
            ByteBuffer buffer = buffers[i + offset];
            if (!buffer.isDirect()) {
                ByteBuffer directBuffer = ByteBuffer.allocateDirect(buffer
                        .remaining());
                directBuffer.put(buffer);
                directBuffer.flip();
                buffer = directBuffer;
                allocatedBufs[i] = (DirectBuffer) directBuffer;
                offsets[i] = 0;
            } else {
                offsets[i] = buffer.position();
                allocatedBufs[i] = null;
            }
            handles[i] = ((DirectBuffer) buffer).getEffectiveAddress().toInt();
            lengths[i] = buffer.remaining();
        }
        long bytesWritten = 0;
        boolean completed = false;
        synchronized (repositioningLock) {
            try {
                begin();
                bytesWritten = fileSystem.writev(handle, handles, offsets,
                        lengths, length);
                completed = true;
            } finally {
                end(completed);
                for (int i = 0; i < length; ++i) {
                    if (allocatedBufs[i] != null) {
                        allocatedBufs[i].free();
                    }
                }
            }
        }
        long bytesRemaining = bytesWritten;
        for (int i = offset; i < length + offset; i++) {
            if (bytesRemaining > buffers[i].remaining()) {
                int pos = buffers[i].limit();
                buffers[i].position(pos);
                bytesRemaining -= buffers[i].remaining();
            } else {
                int pos = buffers[i].position() + (int) bytesRemaining;
                buffers[i].position(pos);
                break;
            }
        }
        return bytesWritten;
    }
    public int getHandle() {
        return handle;
    }
}
