abstract class NativeDispatcher
{
    abstract int read(FileDescriptor fd, long address, int len)
        throws IOException;
    int pread(FileDescriptor fd, long address, int len,
                             long position, Object lock) throws IOException
    {
        throw new IOException("Operation Unsupported");
    }
    abstract long readv(FileDescriptor fd, long address, int len)
        throws IOException;
    abstract int write(FileDescriptor fd, long address, int len)
        throws IOException;
    int pwrite(FileDescriptor fd, long address, int len,
                             long position, Object lock) throws IOException
    {
        throw new IOException("Operation Unsupported");
    }
    abstract long writev(FileDescriptor fd, long address, int len)
        throws IOException;
    abstract void close(FileDescriptor fd) throws IOException;
    void preClose(FileDescriptor fd) throws IOException {
    }
}
