public class FileKey {
    private long st_dev;    
    private long st_ino;    
    private FileKey() { }
    public static FileKey create(FileDescriptor fd) {
        FileKey fk = new FileKey();
        try {
            fk.init(fd);
        } catch (IOException ioe) {
            throw new Error(ioe);
        }
        return fk;
    }
    public int hashCode() {
        return (int)(st_dev ^ (st_dev >>> 32)) +
               (int)(st_ino ^ (st_ino >>> 32));
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof FileKey))
            return false;
        FileKey other = (FileKey)obj;
        if ((this.st_dev != other.st_dev) ||
            (this.st_ino != other.st_ino)) {
            return false;
        }
        return true;
    }
    private native void init(FileDescriptor fd) throws IOException;
    private static native void initIDs();
    static {
        initIDs();
    }
}
