public class File
    implements Serializable, Comparable<File>
{
    static private FileSystem fs = FileSystem.getFileSystem();
    private String path;
    private transient int prefixLength;
    int getPrefixLength() {
        return prefixLength;
    }
    public static final char separatorChar = fs.getSeparator();
    public static final String separator = "" + separatorChar;
    public static final char pathSeparatorChar = fs.getPathSeparator();
    public static final String pathSeparator = "" + pathSeparatorChar;
    private File(String pathname, int prefixLength) {
        this.path = pathname;
        this.prefixLength = prefixLength;
    }
    private File(String child, File parent) {
        assert parent.path != null;
        assert (!parent.path.equals(""));
        this.path = fs.resolve(parent.path, child);
        this.prefixLength = parent.prefixLength;
    }
    public File(String pathname) {
        if (pathname == null) {
            throw new NullPointerException();
        }
        this.path = fs.normalize(pathname);
        this.prefixLength = fs.prefixLength(this.path);
    }
    public File(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.equals("")) {
                this.path = fs.resolve(fs.getDefaultParent(),
                                       fs.normalize(child));
            } else {
                this.path = fs.resolve(fs.normalize(parent),
                                       fs.normalize(child));
            }
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }
    public File(File parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.path.equals("")) {
                this.path = fs.resolve(fs.getDefaultParent(),
                                       fs.normalize(child));
            } else {
                this.path = fs.resolve(parent.path,
                                       fs.normalize(child));
            }
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }
    public File(URI uri) {
        if (!uri.isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        if (uri.isOpaque())
            throw new IllegalArgumentException("URI is not hierarchical");
        String scheme = uri.getScheme();
        if ((scheme == null) || !scheme.equalsIgnoreCase("file"))
            throw new IllegalArgumentException("URI scheme is not \"file\"");
        if (uri.getAuthority() != null)
            throw new IllegalArgumentException("URI has an authority component");
        if (uri.getFragment() != null)
            throw new IllegalArgumentException("URI has a fragment component");
        if (uri.getQuery() != null)
            throw new IllegalArgumentException("URI has a query component");
        String p = uri.getPath();
        if (p.equals(""))
            throw new IllegalArgumentException("URI path component is empty");
        p = fs.fromURIPath(p);
        if (File.separatorChar != '/')
            p = p.replace('/', File.separatorChar);
        this.path = fs.normalize(p);
        this.prefixLength = fs.prefixLength(this.path);
    }
    public String getName() {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }
    public String getParent() {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) {
            if ((prefixLength > 0) && (path.length() > prefixLength))
                return path.substring(0, prefixLength);
            return null;
        }
        return path.substring(0, index);
    }
    public File getParentFile() {
        String p = this.getParent();
        if (p == null) return null;
        return new File(p, this.prefixLength);
    }
    public String getPath() {
        return path;
    }
    public boolean isAbsolute() {
        return fs.isAbsolute(this);
    }
    public String getAbsolutePath() {
        return fs.resolve(this);
    }
    public File getAbsoluteFile() {
        String absPath = getAbsolutePath();
        return new File(absPath, fs.prefixLength(absPath));
    }
    public String getCanonicalPath() throws IOException {
        return fs.canonicalize(fs.resolve(this));
    }
    public File getCanonicalFile() throws IOException {
        String canonPath = getCanonicalPath();
        return new File(canonPath, fs.prefixLength(canonPath));
    }
    private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/')
            p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/"))
            p = "/" + p;
        if (!p.endsWith("/") && isDirectory)
            p = p + "/";
        return p;
    }
    @Deprecated
    public URL toURL() throws MalformedURLException {
        return new URL("file", "", slashify(getAbsolutePath(), isDirectory()));
    }
    public URI toURI() {
        try {
            File f = getAbsoluteFile();
            String sp = slashify(f.getPath(), f.isDirectory());
            if (sp.startsWith("
                sp = "
            return new URI("file", null, sp, null);
        } catch (URISyntaxException x) {
            throw new Error(x);         
        }
    }
    public boolean canRead() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return fs.checkAccess(this, FileSystem.ACCESS_READ);
    }
    public boolean canWrite() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.checkAccess(this, FileSystem.ACCESS_WRITE);
    }
    public boolean exists() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_EXISTS) != 0);
    }
    public boolean isDirectory() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_DIRECTORY)
                != 0);
    }
    public boolean isFile() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_REGULAR) != 0);
    }
    public boolean isHidden() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_HIDDEN) != 0);
    }
    public long lastModified() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return fs.getLastModifiedTime(this);
    }
    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return fs.getLength(this);
    }
    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) security.checkWrite(path);
        return fs.createFileExclusively(path);
    }
    public boolean delete() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        return fs.delete(this);
    }
    public void deleteOnExit() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        DeleteOnExitHook.add(path);
    }
    public String[] list() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return fs.list(this);
    }
    public String[] list(FilenameFilter filter) {
        String names[] = list();
        if ((names == null) || (filter == null)) {
            return names;
        }
        List<String> v = new ArrayList<>();
        for (int i = 0 ; i < names.length ; i++) {
            if (filter.accept(this, names[i])) {
                v.add(names[i]);
            }
        }
        return v.toArray(new String[v.size()]);
    }
    public File[] listFiles() {
        String[] ss = list();
        if (ss == null) return null;
        int n = ss.length;
        File[] fs = new File[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new File(ss[i], this);
        }
        return fs;
    }
    public File[] listFiles(FilenameFilter filter) {
        String ss[] = list();
        if (ss == null) return null;
        ArrayList<File> files = new ArrayList<>();
        for (String s : ss)
            if ((filter == null) || filter.accept(this, s))
                files.add(new File(s, this));
        return files.toArray(new File[files.size()]);
    }
    public File[] listFiles(FileFilter filter) {
        String ss[] = list();
        if (ss == null) return null;
        ArrayList<File> files = new ArrayList<>();
        for (String s : ss) {
            File f = new File(s, this);
            if ((filter == null) || filter.accept(f))
                files.add(f);
        }
        return files.toArray(new File[files.size()]);
    }
    public boolean mkdir() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.createDirectory(this);
    }
    public boolean mkdirs() {
        if (exists()) {
            return false;
        }
        if (mkdir()) {
            return true;
        }
        File canonFile = null;
        try {
            canonFile = getCanonicalFile();
        } catch (IOException e) {
            return false;
        }
        File parent = canonFile.getParentFile();
        return (parent != null && (parent.mkdirs() || parent.exists()) &&
                canonFile.mkdir());
    }
    public boolean renameTo(File dest) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
            security.checkWrite(dest.path);
        }
        return fs.rename(this, dest);
    }
    public boolean setLastModified(long time) {
        if (time < 0) throw new IllegalArgumentException("Negative time");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.setLastModifiedTime(this, time);
    }
    public boolean setReadOnly() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.setReadOnly(this);
    }
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.setPermission(this, FileSystem.ACCESS_WRITE, writable, ownerOnly);
    }
    public boolean setWritable(boolean writable) {
        return setWritable(writable, true);
    }
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.setPermission(this, FileSystem.ACCESS_READ, readable, ownerOnly);
    }
    public boolean setReadable(boolean readable) {
        return setReadable(readable, true);
    }
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return fs.setPermission(this, FileSystem.ACCESS_EXECUTE, executable, ownerOnly);
    }
    public boolean setExecutable(boolean executable) {
        return setExecutable(executable, true);
    }
    public boolean canExecute() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExec(path);
        }
        return fs.checkAccess(this, FileSystem.ACCESS_EXECUTE);
    }
    public static File[] listRoots() {
        return fs.listRoots();
    }
    public long getTotalSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(path);
        }
        return fs.getSpace(this, FileSystem.SPACE_TOTAL);
    }
    public long getFreeSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(path);
        }
        return fs.getSpace(this, FileSystem.SPACE_FREE);
    }
    public long getUsableSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(path);
        }
        return fs.getSpace(this, FileSystem.SPACE_USABLE);
    }
    private static class TempDirectory {
        private TempDirectory() { }
        private static final File tmpdir = new File(fs.normalize(AccessController
            .doPrivileged(new GetPropertyAction("java.io.tmpdir"))));
        static File location() {
            return tmpdir;
        }
        private static final SecureRandom random = new SecureRandom();
        static File generateFile(String prefix, String suffix, File dir) {
            long n = random.nextLong();
            if (n == Long.MIN_VALUE) {
                n = 0;      
            } else {
                n = Math.abs(n);
            }
            return new File(dir, prefix + Long.toString(n) + suffix);
        }
    }
    public static File createTempFile(String prefix, String suffix,
                                      File directory)
        throws IOException
    {
        if (prefix.length() < 3)
            throw new IllegalArgumentException("Prefix string too short");
        if (suffix == null)
            suffix = ".tmp";
        File tmpdir = (directory != null) ? directory : TempDirectory.location();
        SecurityManager sm = System.getSecurityManager();
        File f;
        do {
            f = TempDirectory.generateFile(prefix, suffix, tmpdir);
            if (sm != null) {
                try {
                    sm.checkWrite(f.getPath());
                } catch (SecurityException se) {
                    if (directory == null)
                        throw new SecurityException("Unable to create temporary file");
                    throw se;
                }
            }
        } while (!fs.createFileExclusively(f.getPath()));
        return f;
    }
    public static File createTempFile(String prefix, String suffix)
        throws IOException
    {
        return createTempFile(prefix, suffix, null);
    }
    public int compareTo(File pathname) {
        return fs.compare(this, pathname);
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof File)) {
            return compareTo((File)obj) == 0;
        }
        return false;
    }
    public int hashCode() {
        return fs.hashCode(this);
    }
    public String toString() {
        return getPath();
    }
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();
        s.writeChar(this.separatorChar); 
    }
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        ObjectInputStream.GetField fields = s.readFields();
        String pathField = (String)fields.get("path", null);
        char sep = s.readChar(); 
        if (sep != separatorChar)
            pathField = pathField.replace(sep, separatorChar);
        this.path = fs.normalize(pathField);
        this.prefixLength = fs.prefixLength(this.path);
    }
    private static final long serialVersionUID = 301077366599181567L;
    private volatile transient Path filePath;
    public Path toPath() {
        Path result = filePath;
        if (result == null) {
            synchronized (this) {
                result = filePath;
                if (result == null) {
                    result = FileSystems.getDefault().getPath(path);
                    filePath = result;
                }
            }
        }
        return result;
    }
}
