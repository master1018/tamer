public class FileOutputStream extends OutputStream implements Closeable {
    FileDescriptor fd;
    boolean innerFD;
    private FileChannel channel;
    private IFileSystem fileSystem = Platform.getFileSystem();
    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }
    public FileOutputStream(File file, boolean append)
            throws FileNotFoundException {
        super();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(file.getPath());
        }
        fd = new FileDescriptor();
        fd.descriptor = fileSystem.open(file.pathBytes,
                append ? IFileSystem.O_APPEND : IFileSystem.O_WRONLY);
        innerFD = true;
        channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                append ? IFileSystem.O_APPEND : IFileSystem.O_WRONLY);
    }
    public FileOutputStream(FileDescriptor fd) {
        super();
        if (fd == null) {
            throw new NullPointerException(Msg.getString("K006c")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(fd);
        }
        this.fd = fd;
        innerFD = false;
        channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                IFileSystem.O_WRONLY);
    }
    public FileOutputStream(String filename) throws FileNotFoundException {
        this(filename, false);
    }
    public FileOutputStream(String filename, boolean append)
            throws FileNotFoundException {
        this(new File(filename), append);
    }
    @Override
    public void close() throws IOException {
        if (fd == null) {
            return;
        }
        if (channel != null) {
            synchronized (channel) {
                if (channel.isOpen() && fd.descriptor >= 0) {
                    channel.close();
                }
            }
        }
        synchronized (this) {
            if (fd.descriptor >= 0 && innerFD) {
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
        return channel;
    }
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }
    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((count | offset) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (count == 0) {
            return;
        }
        openCheck();
        fileSystem.write(fd.descriptor, buffer, offset, count);
    }
    @Override
    public void write(int oneByte) throws IOException {
        openCheck();
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) oneByte;
        fileSystem.write(fd.descriptor, byteArray, 0, 1);
    }
    private synchronized void openCheck() throws IOException {
        if (fd.descriptor < 0) {
            throw new IOException();
        }
    }
}
