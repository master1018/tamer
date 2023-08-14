public final class ReadWriteFileChannel extends FileChannelImpl {
    public ReadWriteFileChannel(Object stream, int handle) {
        super(stream, handle);
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
        int mapMode;
        if (mode == MapMode.READ_ONLY) {
            mapMode = IMemorySystem.MMAP_READ_ONLY;
        } else if (mode == MapMode.READ_WRITE) {
            mapMode = IMemorySystem.MMAP_READ_WRITE;
        } else {
            mapMode = IMemorySystem.MMAP_WRITE_COPY;
        }
        return mapImpl(mapMode, position, size);
    }
}
