public class FileChannelFactory {
    public static FileChannel getFileChannel(Object stream, int fd, int mode) {
        switch (mode) {
            case IFileSystem.O_RDONLY:
                return new ReadOnlyFileChannel(stream, fd);
            case IFileSystem.O_WRONLY:
                return new WriteOnlyFileChannel(stream, fd);
            case IFileSystem.O_RDWR:
                return new ReadWriteFileChannel(stream, fd);
            case IFileSystem.O_RDWRSYNC:
                return new ReadWriteFileChannel(stream, fd);
            case IFileSystem.O_APPEND:
                return new WriteOnlyFileChannel(stream, fd, true);
            default:
                throw new RuntimeException(Messages.getString("nio.09", mode)); 
        }
    }
}
