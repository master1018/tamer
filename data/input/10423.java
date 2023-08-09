abstract class PathFileObject implements JavaFileObject {
    private JavacPathFileManager fileManager;
    private Path path;
    static PathFileObject createDirectoryPathFileObject(JavacPathFileManager fileManager,
            final Path path, final Path dir) {
        return new PathFileObject(fileManager, path) {
            @Override
            String inferBinaryName(Iterable<? extends Path> paths) {
                return toBinaryName(dir.relativize(path));
            }
        };
    }
    static PathFileObject createJarPathFileObject(JavacPathFileManager fileManager,
            final Path path) {
        return new PathFileObject(fileManager, path) {
            @Override
            String inferBinaryName(Iterable<? extends Path> paths) {
                return toBinaryName(path);
            }
        };
    }
    static PathFileObject createSiblingPathFileObject(JavacPathFileManager fileManager,
            final Path path, final String relativePath) {
        return new PathFileObject(fileManager, path) {
            @Override
            String inferBinaryName(Iterable<? extends Path> paths) {
                return toBinaryName(relativePath, "/");
            }
        };
    }
    static PathFileObject createSimplePathFileObject(JavacPathFileManager fileManager,
            final Path path) {
        return new PathFileObject(fileManager, path) {
            @Override
            String inferBinaryName(Iterable<? extends Path> paths) {
                Path absPath = path.toAbsolutePath();
                for (Path p: paths) {
                    Path ap = p.toAbsolutePath();
                    if (absPath.startsWith(ap)) {
                        try {
                            Path rp = ap.relativize(absPath);
                            if (rp != null) 
                                return toBinaryName(rp);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
                return null;
            }
        };
    }
    protected PathFileObject(JavacPathFileManager fileManager, Path path) {
        fileManager.getClass(); 
        path.getClass();        
        this.fileManager = fileManager;
        this.path = path;
    }
    abstract String inferBinaryName(Iterable<? extends Path> paths);
    Path getPath() {
        return path;
    }
    @Override
    public Kind getKind() {
        return BaseFileManager.getKind(path.getFileName().toString());
    }
    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        simpleName.getClass();
        if (kind == Kind.OTHER && getKind() != kind) {
            return false;
        }
        String sn = simpleName + kind.extension;
        String pn = path.getFileName().toString();
        if (pn.equals(sn)) {
            return true;
        }
        if (pn.equalsIgnoreCase(sn)) {
            try {
                return path.toRealPath(LinkOption.NOFOLLOW_LINKS).getFileName().toString().equals(sn);
            } catch (IOException e) {
            }
        }
        return false;
    }
    @Override
    public NestingKind getNestingKind() {
        return null;
    }
    @Override
    public Modifier getAccessLevel() {
        return null;
    }
    @Override
    public URI toUri() {
        return path.toUri();
    }
    @Override
    public String getName() {
        return path.toString();
    }
    @Override
    public InputStream openInputStream() throws IOException {
        return Files.newInputStream(path);
    }
    @Override
    public OutputStream openOutputStream() throws IOException {
        ensureParentDirectoriesExist();
        return Files.newOutputStream(path);
    }
    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        CharsetDecoder decoder = fileManager.getDecoder(fileManager.getEncodingName(), ignoreEncodingErrors);
        return new InputStreamReader(openInputStream(), decoder);
    }
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        CharBuffer cb = fileManager.getCachedContent(this);
        if (cb == null) {
            InputStream in = openInputStream();
            try {
                ByteBuffer bb = fileManager.makeByteBuffer(in);
                JavaFileObject prev = fileManager.log.useSource(this);
                try {
                    cb = fileManager.decode(bb, ignoreEncodingErrors);
                } finally {
                    fileManager.log.useSource(prev);
                }
                fileManager.recycleByteBuffer(bb);
                if (!ignoreEncodingErrors) {
                    fileManager.cache(this, cb);
                }
            } finally {
                in.close();
            }
        }
        return cb;
    }
    @Override
    public Writer openWriter() throws IOException {
        ensureParentDirectoriesExist();
        return new OutputStreamWriter(Files.newOutputStream(path), fileManager.getEncodingName());
    }
    @Override
    public long getLastModified() {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return -1;
        }
    }
    @Override
    public boolean delete() {
        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public boolean isSameFile(PathFileObject other) {
        try {
            return Files.isSameFile(path, other.path);
        } catch (IOException e) {
            return false;
        }
    }
    @Override
    public boolean equals(Object other) {
        return (other instanceof PathFileObject && path.equals(((PathFileObject) other).path));
    }
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path + "]";
    }
    private void ensureParentDirectoriesExist() throws IOException {
        Path parent = path.getParent();
        if (parent != null)
            Files.createDirectories(parent);
    }
    private long size() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }
    protected static String toBinaryName(Path relativePath) {
        return toBinaryName(relativePath.toString(),
                relativePath.getFileSystem().getSeparator());
    }
    protected static String toBinaryName(String relativePath, String sep) {
        return removeExtension(relativePath).replace(sep, ".");
    }
    protected static String removeExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return (lastDot == -1 ? fileName : fileName.substring(0, lastDot));
    }
}
