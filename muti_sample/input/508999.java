public class FileInputStream extends InputStream implements Closeable {
    FileDescriptor fd;
    private FileChannel channel;
    boolean innerFD;
    private IFileSystem fileSystem = Platform.getFileSystem();
    private static class RepositioningLock {
    }
    private Object repositioningLock = new RepositioningLock();
    public FileInputStream(File file) throws FileNotFoundException {
        super();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            String filePath = (null == file ? null : file.getPath());
            security.checkRead(filePath);
        }
        if (file == null) {
            throw new NullPointerException(Msg.getString("KA001")); 
        }
        fd = new FileDescriptor();
        fd.readOnly = true;
        fd.descriptor = fileSystem.open(file.pathBytes, IFileSystem.O_RDONLY);
        innerFD = true;
    }
    public FileInputStream(FileDescriptor fd) {
        super();
        if (fd == null) {
            throw new NullPointerException();
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(fd);
        }
        this.fd = fd;
        innerFD = false;
    }
    public FileInputStream(String fileName) throws FileNotFoundException {
        this(null == fileName ? (File) null : new File(fileName));
    }
    @Override
    public int available() throws IOException {
        openCheck();
        return fileSystem.ioctlAvailable(fd.descriptor);
    }
    @Override
    public void close() throws IOException {
        synchronized (this) {
            if (channel != null && channel.isOpen()) {
                channel.close();
                channel = null;
            }
            if (fd != null && fd.descriptor >= 0 && innerFD) {
                fileSystem.close(fd.descriptor);
                fd.descriptor = -1;
            }
        }
    }
    @Override
    protected void finalize() throws IOException {
        close();
    }
    public FileChannel getChannel() {
        synchronized(this) {
            if (channel == null) {
                channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                        IFileSystem.O_RDONLY);
            }
            return channel;
        }
    }
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }
    @Override
    public int read() throws IOException {
        byte[] readed = new byte[1];
        int result = read(readed, 0, 1);
        return result == -1 ? -1 : readed[0] & 0xff;
    }
    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((count | offset) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (0 == count) {
            return 0;
        }
        openCheck();
        synchronized (repositioningLock) {
            return (int) fileSystem.read(fd.descriptor, buffer, offset, count);
        }
    }
    @Override
    public long skip(long count) throws IOException {
        openCheck();
        if (count == 0) {
            return 0;
        }
        if (count < 0) {
            throw new IOException(Msg.getString("KA013")); 
        }
        synchronized (repositioningLock) {
            fileSystem.seek(fd.descriptor, count, IFileSystem.SEEK_CUR);
            return count;
        }
    }
    private synchronized void openCheck() throws IOException {
        if (fd.descriptor < 0) {
            throw new IOException();
        }
    }
}
