public abstract class MappedByteBuffer extends ByteBuffer {
    final DirectByteBuffer wrapped;
    private int mapMode;
    MappedByteBuffer(ByteBuffer directBuffer) {
        super(directBuffer.capacity);
        if (!directBuffer.isDirect()) {
            throw new IllegalArgumentException();
        }
        this.wrapped = (DirectByteBuffer) directBuffer;
    }
    MappedByteBuffer(PlatformAddress addr, int capa, int offset, int mode) {
        super(capa);
        mapMode = mode;
        switch (mapMode) {
            case IMemorySystem.MMAP_READ_ONLY:
                wrapped = new ReadOnlyDirectByteBuffer(addr, capa, offset);
                break;
            case IMemorySystem.MMAP_READ_WRITE:
            case IMemorySystem.MMAP_WRITE_COPY:
                wrapped = new ReadWriteDirectByteBuffer(addr, capa, offset);
                break;
            default:
                throw new IllegalArgumentException();
        }
        addr.autoFree();
    }
    public final boolean isLoaded() {
        return ((MappedPlatformAddress) ((DirectBuffer) wrapped)
                .getBaseAddress()).mmapIsLoaded();
    }
    public final MappedByteBuffer load() {
        ((MappedPlatformAddress) ((DirectBuffer) wrapped).getBaseAddress())
                .mmapLoad();
        return this;
    }
    public final MappedByteBuffer force() {
        if (mapMode == IMemorySystem.MMAP_READ_WRITE) {
            ((MappedPlatformAddress) ((DirectBuffer) wrapped).getBaseAddress())
                    .mmapFlush();
        }
        return this;
    }
}
