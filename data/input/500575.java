public class File implements Serializable, Comparable<File> {
    private static final long serialVersionUID = 301077366599181567L;
    private static final String EMPTY_STRING = ""; 
    private static final Charset UTF8 = Charset.forName("UTF-8");
    public static final char separatorChar;
    public static final String separator;
    public static final char pathSeparatorChar;
    public static final String pathSeparator;
    private static int counter;
    private String path;
    transient byte[] pathBytes;
    static {
        separatorChar = System.getProperty("file.separator", "/").charAt(0); 
        pathSeparatorChar = System.getProperty("path.separator", ":").charAt(0); 
        separator = String.valueOf(separatorChar);
        pathSeparator = String.valueOf(pathSeparatorChar);
    }
    public File(File dir, String name) {
        this(dir == null ? null : dir.getPath(), name);
    }
    public File(String path) {
        init(path);
    }
    public File(String dirPath, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dirPath == null || dirPath.length() == 0) {
            init(name);
        } else if (name.length() == 0) {
            init(dirPath);
        } else {
            init(join(dirPath, name));
        }
    }
    public File(URI uri) {
        checkURI(uri);
        init(uri.getPath());
    }
    private void init(String dirtyPath) {
        this.path = fixSlashes(dirtyPath);
        if (path.length() > 0 && path.charAt(0) == separatorChar) { 
            this.pathBytes = newCString(path);
            return;
        }
        String userDir = AccessController.doPrivileged(
            new PriviAction<String>("user.dir")); 
        this.pathBytes = newCString(path.length() == 0 ? userDir : join(userDir, path));
    }
    private byte[] newCString(String s) {
        ByteBuffer buffer = UTF8.encode(s);
        int byteCount = buffer.limit() + 1;
        byte[] bytes = new byte[byteCount];
        buffer.get(bytes, 0, byteCount - 1);
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] == '\\') {
                bytes[i] = '/';
            }
        }
        return bytes;
    }
    private String fixSlashes(String origPath) {
        boolean lastWasSlash = false;
        char[] newPath = origPath.toCharArray();
        int length = newPath.length;
        int newLength = 0;
        for (int i = 0; i < length; ++i) {
            char ch = newPath[i];
            if (ch == '/') {
                if (!lastWasSlash) {
                    newPath[newLength++] = separatorChar;
                    lastWasSlash = true;
                }
            } else {
                newPath[newLength++] = ch;
                lastWasSlash = false;
            }
        }
        if (lastWasSlash && newLength > 1) {
            newLength--;
        }
        return (newLength != length) ? new String(newPath, 0, newLength) : origPath;
    }
    private String join(String prefix, String suffix) {
        int prefixLength = prefix.length();
        boolean haveSlash = (prefixLength > 0 && prefix.charAt(prefixLength - 1) == separatorChar);
        if (!haveSlash) {
            haveSlash = (suffix.length() > 0 && suffix.charAt(0) == separatorChar);
        }
        return haveSlash ? (prefix + suffix) : (prefix + separatorChar + suffix);
    }
    @SuppressWarnings("nls")
    private void checkURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(Msg.getString("K031a", uri));
        } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
            throw new IllegalArgumentException(Msg.getString("K031b", uri));
        }
        String temp = uri.getScheme();
        if (temp == null || !temp.equals("file")) {
            throw new IllegalArgumentException(Msg.getString("K031c", uri));
        }
        temp = uri.getRawPath();
        if (temp == null || temp.length() == 0) {
            throw new IllegalArgumentException(Msg.getString("K031d", uri));
        }
        if (uri.getRawAuthority() != null) {
            throw new IllegalArgumentException(Msg.getString("K031e",
                    new String[] { "authority", uri.toString() }));
        }
        if (uri.getRawQuery() != null) {
            throw new IllegalArgumentException(Msg.getString("K031e",
                    new String[] { "query", uri.toString() }));
        }
        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException(Msg.getString("K031e",
                    new String[] { "fragment", uri.toString() }));
        }
    }
    public static File[] listRoots() {
        return new File[] { new File("/") };
    }
    public boolean canRead() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isReadableImpl(pathBytes);
    }
    private native boolean isReadableImpl(byte[] filePath);
    public boolean canWrite() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return isWritableImpl(pathBytes);
    }
    private native boolean isWritableImpl(byte[] filePath);
    public int compareTo(File another) {
        return this.getPath().compareTo(another.getPath());
    }
    public boolean delete() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        return deleteImpl(pathBytes);
    }
    private native boolean deleteImpl(byte[] filePath);
    public void deleteOnExit() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        DeleteOnExit.getInstance().addFile(getAbsoluteName());
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof File)) {
            return false;
        }
        return path.equals(((File) obj).getPath());
    }
    public boolean exists() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return existsImpl(pathBytes);
    }
    private native boolean existsImpl(byte[] filePath);
    public String getAbsolutePath() {
        return Util.toUTF8String(pathBytes, 0, pathBytes.length - 1);
    }
    public File getAbsoluteFile() {
        return new File(this.getAbsolutePath());
    }
    public String getCanonicalPath() throws IOException {
        byte[] result = pathBytes;
        if(separatorChar == '/') {
            result = resolveLink(result, result.length, false);
            result = resolve(result);
        }
        int numSeparators = 1;
        for (int i = 0; i < result.length; i++) {
            if (result[i] == separatorChar) {
                numSeparators++;
            }
        }
        int sepLocations[] = new int[numSeparators];
        int rootLoc = 0;
        if (separatorChar != '/') {
            if (result[0] == '\\') {
                rootLoc = (result.length > 1 && result[1] == '\\') ? 1 : 0;
            } else {
                rootLoc = 2; 
            }
        }
        byte newResult[] = new byte[result.length + 1];
        int newLength = 0, lastSlash = 0, foundDots = 0;
        sepLocations[lastSlash] = rootLoc;
        for (int i = 0; i <= result.length; i++) {
            if (i < rootLoc) {
                newResult[newLength++] = result[i];
            } else {
                if (i == result.length || result[i] == separatorChar) {
                    if (i == result.length && foundDots == 0) {
                        break;
                    }
                    if (foundDots == 1) {
                        foundDots = 0;
                        continue;
                    }
                    if (foundDots > 1) {
                        lastSlash = lastSlash > (foundDots - 1) ? lastSlash
                                - (foundDots - 1) : 0;
                        newLength = sepLocations[lastSlash] + 1;
                        foundDots = 0;
                        continue;
                    }
                    sepLocations[++lastSlash] = newLength;
                    newResult[newLength++] = (byte) separatorChar;
                    continue;
                }
                if (result[i] == '.') {
                    foundDots++;
                    continue;
                }
                if (foundDots > 0) {
                    for (int j = 0; j < foundDots; j++) {
                        newResult[newLength++] = (byte) '.';
                    }
                }
                newResult[newLength++] = result[i];
                foundDots = 0;
            }
        }
        if (newLength > (rootLoc + 1)
                && newResult[newLength - 1] == separatorChar) {
            newLength--;
        }
        newResult[newLength] = 0;
        newResult = getCanonImpl(newResult);
        newLength = newResult.length;
        return Util.toUTF8String(newResult, 0, newLength);
    }
    private byte[] resolve(byte[] newResult) throws IOException {
        int last = 1, nextSize, linkSize;
        byte[] linkPath = newResult, bytes;
        boolean done, inPlace;
        for (int i = 1; i <= newResult.length; i++) {
            if (i == newResult.length || newResult[i] == separatorChar) {
                done = i >= newResult.length - 1;
                if (done && linkPath.length == 1) {
                    return newResult;
                }
                inPlace = false;
                if (linkPath == newResult) {
                    bytes = newResult;
                    if (!done) {
                        inPlace = true;
                        newResult[i] = '\0';
                    }
                } else {
                    nextSize = i - last + 1;
                    linkSize = linkPath.length;
                    if (linkPath[linkSize - 1] == separatorChar) {
                        linkSize--;
                    }
                    bytes = new byte[linkSize + nextSize];
                    System.arraycopy(linkPath, 0, bytes, 0, linkSize);
                    System.arraycopy(newResult, last - 1, bytes, linkSize,
                            nextSize);
                }
                if (done) {
                    return bytes;
                }
                linkPath = resolveLink(bytes, inPlace ? i : bytes.length, true);
                if (inPlace) {
                    newResult[i] = '/';
                }
                last = i + 1;
            }
        }
        throw new InternalError();
    }
    private byte[] resolveLink(byte[] pathBytes, int length,
            boolean resolveAbsolute) throws IOException {
        boolean restart = false;
        byte[] linkBytes, temp;
        do {
            linkBytes = getLinkImpl(pathBytes);
            if (linkBytes == pathBytes) {
                break;
            }
            if (linkBytes[0] == separatorChar) {
                restart = resolveAbsolute;
                pathBytes = linkBytes;
            } else {
                int last = length - 1;
                while (pathBytes[last] != separatorChar) {
                    last--;
                }
                last++;
                temp = new byte[last + linkBytes.length];
                System.arraycopy(pathBytes, 0, temp, 0, last);
                System.arraycopy(linkBytes, 0, temp, last, linkBytes.length);
                pathBytes = temp;
            }
            length = pathBytes.length;
        } while (existsImpl(pathBytes));
        if (restart) {
            return resolve(pathBytes);
        }
        return pathBytes;
    }
    private native byte[] getLinkImpl(byte[] filePath);
    public File getCanonicalFile() throws IOException {
        return new File(getCanonicalPath());
    }
    private native byte[] getCanonImpl(byte[] filePath);
    public String getName() {
        int separatorIndex = path.lastIndexOf(separator);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1,
                path.length());
    }
    public String getParent() {
        int length = path.length(), firstInPath = 0;
        if (separatorChar == '\\' && length > 2 && path.charAt(1) == ':') {
            firstInPath = 2;
        }
        int index = path.lastIndexOf(separatorChar);
        if (index == -1 && firstInPath > 0) {
            index = 2;
        }
        if (index == -1 || path.charAt(length - 1) == separatorChar) {
            return null;
        }
        if (path.indexOf(separatorChar) == index
                && path.charAt(firstInPath) == separatorChar) {
            return path.substring(0, index + 1);
        }
        return path.substring(0, index);
    }
    public File getParentFile() {
        String tempParent = getParent();
        if (tempParent == null) {
            return null;
        }
        return new File(tempParent);
    }
    public String getPath() {
        return path;
    }
    @Override
    public int hashCode() {
        return getPath().hashCode() ^ 1234321;
    }
    public boolean isAbsolute() {
        return path.length() > 0 && path.charAt(0) == separatorChar;
    }
    public boolean isDirectory() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isDirectoryImpl(pathBytes);
    }
    private native boolean isDirectoryImpl(byte[] filePath);
    public boolean isFile() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isFileImpl(pathBytes);
    }
    private native boolean isFileImpl(byte[] filePath);
    public boolean isHidden() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return getName().startsWith(".");
    }
    public long lastModified() {
        if (path.length() == 0) {
            return 0;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return lastModifiedImpl(pathBytes);
    }
    private native long lastModifiedImpl(byte[] filePath);
    public boolean setLastModified(long time) {
        if (path.length() == 0) {
            return false;
        }
        if (time < 0) {
            throw new IllegalArgumentException(Msg.getString("K006a")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return setLastModifiedImpl(pathBytes, time);
    }
    private native boolean setLastModifiedImpl(byte[] path, long time);
    public boolean setReadOnly() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return setReadOnlyImpl(pathBytes);
    }
    private native boolean setReadOnlyImpl(byte[] path);
    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return lengthImpl(pathBytes);
    }
    private native long lengthImpl(byte[] filePath);
    public String[] list() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (path.length() == 0) {
            return null;
        }
        return listImpl(pathBytes);
    }
    private native String[] listImpl(byte[] path);
    public String[] list(FilenameFilter filter) {
        String[] filenames = list();
        if (filter == null || filenames == null) {
            return filenames;
        }
        List<String> result = new ArrayList<String>(filenames.length);
        for (String filename : filenames) {
            if (filter.accept(this, filename)) {
                result.add(filename);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    public File[] listFiles() {
        return filenamesToFiles(list());
    }
    public File[] listFiles(FilenameFilter filter) {
        return filenamesToFiles(list(filter));
    }
    public File[] listFiles(FileFilter filter) {
        File[] files = listFiles();
        if (filter == null || files == null) {
            return files;
        }
        List<File> result = new ArrayList<File>(files.length);
        for (File file : files) {
            if (filter.accept(file)) {
                result.add(file);
            }
        }
        return result.toArray(new File[result.size()]);
    }
    private File[] filenamesToFiles(String[] filenames) {
        if (filenames == null) {
            return null;
        }
        int count = filenames.length;
        File[] result = new File[count];
        for (int i = 0; i < count; ++i) {
            result[i] = new File(this, filenames[i]);
        }
        return result;
    }
    public boolean mkdir() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return mkdirImpl(pathBytes);
    }
    private native boolean mkdirImpl(byte[] filePath);
    public boolean mkdirs() {
        if (exists()) {
            return false;
        }
        if (mkdir()) {
            return true;
        }
        String parentDir = getParent();
        if (parentDir == null) {
            return false;
        }
        return (new File(parentDir).mkdirs() && mkdir());
    }
    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (path.length() == 0) {
            throw new IOException(Msg.getString("KA012")); 
        }
        return createNewFileImpl(pathBytes);
    }
    private native boolean createNewFileImpl(byte[] filePath);
    public static File createTempFile(String prefix, String suffix)
            throws IOException {
        return createTempFile(prefix, suffix, null);
    }
    @SuppressWarnings("nls")
    public static File createTempFile(String prefix, String suffix,
            File directory) throws IOException {
        if (prefix.length() < 3) {
            throw new IllegalArgumentException(Msg.getString("K006b"));
        }
        String newSuffix = suffix == null ? ".tmp" : suffix;
        File tmpDirFile;
        if (directory == null) {
            String tmpDir = AccessController.doPrivileged(
                new PriviAction<String>("java.io.tmpdir", "."));
            tmpDirFile = new File(tmpDir);
        } else {
            tmpDirFile = directory;
        }
        File result;
        do {
            result = genTempFile(prefix, newSuffix, tmpDirFile);
        } while (!result.createNewFile());
        return result;
    }
    private static File genTempFile(String prefix, String suffix, File directory) {
        if (counter == 0) {
            int newInt = new SecureRandom().nextInt();
            counter = ((newInt / 65535) & 0xFFFF) + 0x2710;
        }
        StringBuilder newName = new StringBuilder();
        newName.append(prefix);
        newName.append(counter++);
        newName.append(suffix);
        return new File(directory, newName.toString());
    }
    public boolean renameTo(java.io.File dest) {
        if (path.length() == 0 || dest.path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
            security.checkWrite(dest.path);
        }
        return renameToImpl(pathBytes, dest.pathBytes);
    }
    private native boolean renameToImpl(byte[] pathExist, byte[] pathNew);
    @Override
    public String toString() {
        return path;
    }
    @SuppressWarnings("nls")
    public URI toURI() {
        String name = getAbsoluteName();
        try {
            if (!name.startsWith("/")) {
                return new URI("file", null, new StringBuilder(
                        name.length() + 1).append('/').append(name).toString(),
                        null, null);
            } else if (name.startsWith("
                return new URI("file", "", name, null); 
            }
            return new URI("file", null, name, null, null);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    @SuppressWarnings("nls")
    public URL toURL() throws java.net.MalformedURLException {
        String name = getAbsoluteName();
        if (!name.startsWith("/")) {
            return new URL(
                    "file", EMPTY_STRING, -1, new StringBuilder(name.length() + 1) 
                            .append('/').append(name).toString(), null);
        } else if (name.startsWith("
            return new URL("file:" + name); 
        }
        return new URL("file", EMPTY_STRING, -1, name, null);
    }
    private String getAbsoluteName() {
        File f = getAbsoluteFile();
        String name = f.getPath();
        if (f.isDirectory() && name.charAt(name.length() - 1) != separatorChar) {
            name = new StringBuilder(name.length() + 1).append(name)
                    .append('/').toString();
        }
        if (separatorChar != '/') { 
            name = name.replace(separatorChar, '/');
        }
        return name;
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeChar(separatorChar);
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        char inSeparator = stream.readChar();
        init(path.replace(inSeparator, separatorChar));
    }
}
