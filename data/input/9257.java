public class ZipFileAttributes implements BasicFileAttributes
{
    private final ZipFileSystem.Entry e;
    ZipFileAttributes(ZipFileSystem.Entry e) {
        this.e = e;
    }
    @Override
    public FileTime creationTime() {
        if (e.ctime != -1)
            return FileTime.fromMillis(e.ctime);
        return null;
    }
    @Override
    public boolean isDirectory() {
        return e.isDir();
    }
    @Override
    public boolean isOther() {
        return false;
    }
    @Override
    public boolean isRegularFile() {
        return !e.isDir();
    }
    @Override
    public FileTime lastAccessTime() {
        if (e.atime != -1)
            return FileTime.fromMillis(e.atime);
        return null;
    }
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.fromMillis(e.mtime);
    }
    @Override
    public long size() {
        return e.size;
    }
    @Override
    public boolean isSymbolicLink() {
        return false;
    }
    @Override
    public Object fileKey() {
        return null;
    }
    public long compressedSize() {
        return e.csize;
    }
    public long crc() {
        return e.crc;
    }
    public int method() {
        return e.method;
    }
    public byte[] extra() {
        if (e.extra != null)
            return Arrays.copyOf(e.extra, e.extra.length);
        return null;
    }
    public byte[] comment() {
        if (e.comment != null)
            return Arrays.copyOf(e.comment, e.comment.length);
        return null;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        Formatter fm = new Formatter(sb);
        if (creationTime() != null)
            fm.format("    creationTime    : %tc%n", creationTime().toMillis());
        else
            fm.format("    creationTime    : null%n");
        if (lastAccessTime() != null)
            fm.format("    lastAccessTime  : %tc%n", lastAccessTime().toMillis());
        else
            fm.format("    lastAccessTime  : null%n");
        fm.format("    lastModifiedTime: %tc%n", lastModifiedTime().toMillis());
        fm.format("    isRegularFile   : %b%n", isRegularFile());
        fm.format("    isDirectory     : %b%n", isDirectory());
        fm.format("    isSymbolicLink  : %b%n", isSymbolicLink());
        fm.format("    isOther         : %b%n", isOther());
        fm.format("    fileKey         : %s%n", fileKey());
        fm.format("    size            : %d%n", size());
        fm.format("    compressedSize  : %d%n", compressedSize());
        fm.format("    crc             : %x%n", crc());
        fm.format("    method          : %d%n", method());
        fm.close();
        return sb.toString();
    }
}
