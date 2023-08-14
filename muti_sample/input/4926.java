public class JavacPathFileManager extends BaseFileManager implements PathFileManager {
    protected FileSystem defaultFileSystem;
    public JavacPathFileManager(Context context, boolean register, Charset charset) {
        super(charset);
        if (register)
            context.put(JavaFileManager.class, this);
        pathsForLocation = new HashMap<Location, PathsForLocation>();
        fileSystems = new HashMap<Path,FileSystem>();
        setContext(context);
    }
    @Override
    protected void setContext(Context context) {
        super.setContext(context);
        searchPaths = Paths.instance(context);
    }
    @Override
    public FileSystem getDefaultFileSystem() {
        if (defaultFileSystem == null)
            defaultFileSystem = FileSystems.getDefault();
        return defaultFileSystem;
    }
    @Override
    public void setDefaultFileSystem(FileSystem fs) {
        defaultFileSystem = fs;
    }
    @Override
    public void flush() throws IOException {
        contentCache.clear();
    }
    @Override
    public void close() throws IOException {
        for (FileSystem fs: fileSystems.values())
            fs.close();
    }
    @Override
    public ClassLoader getClassLoader(Location location) {
        nullCheck(location);
        Iterable<? extends Path> path = getLocation(location);
        if (path == null)
            return null;
        ListBuffer<URL> lb = new ListBuffer<URL>();
        for (Path p: path) {
            try {
                lb.append(p.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
        }
        return getClassLoader(lb.toArray(new URL[lb.size()]));
    }
    @Override
    public boolean isDefaultBootClassPath() {
        return searchPaths.isDefaultBootClassPath();
    }
    public boolean hasLocation(Location location) {
        return (getLocation(location) != null);
    }
    public Iterable<? extends Path> getLocation(Location location) {
        nullCheck(location);
        lazyInitSearchPaths();
        PathsForLocation path = pathsForLocation.get(location);
        if (path == null && !pathsForLocation.containsKey(location)) {
            setDefaultForLocation(location);
            path = pathsForLocation.get(location);
        }
        return path;
    }
    private Path getOutputLocation(Location location) {
        Iterable<? extends Path> paths = getLocation(location);
        return (paths == null ? null : paths.iterator().next());
    }
    public void setLocation(Location location, Iterable<? extends Path> searchPath)
            throws IOException
    {
        nullCheck(location);
        lazyInitSearchPaths();
        if (searchPath == null) {
            setDefaultForLocation(location);
        } else {
            if (location.isOutputLocation())
                checkOutputPath(searchPath);
            PathsForLocation pl = new PathsForLocation();
            for (Path p: searchPath)
                pl.add(p);  
            pathsForLocation.put(location, pl);
        }
    }
    private void checkOutputPath(Iterable<? extends Path> searchPath) throws IOException {
        Iterator<? extends Path> pathIter = searchPath.iterator();
        if (!pathIter.hasNext())
            throw new IllegalArgumentException("empty path for directory");
        Path path = pathIter.next();
        if (pathIter.hasNext())
            throw new IllegalArgumentException("path too long for directory");
        if (!isDirectory(path))
            throw new IOException(path + ": not a directory");
    }
    private void setDefaultForLocation(Location locn) {
        Collection<File> files = null;
        if (locn instanceof StandardLocation) {
            switch ((StandardLocation) locn) {
                case CLASS_PATH:
                    files = searchPaths.userClassPath();
                    break;
                case PLATFORM_CLASS_PATH:
                    files = searchPaths.bootClassPath();
                    break;
                case SOURCE_PATH:
                    files = searchPaths.sourcePath();
                    break;
                case CLASS_OUTPUT: {
                    String arg = options.get(D);
                    files = (arg == null ? null : Collections.singleton(new File(arg)));
                    break;
                }
                case SOURCE_OUTPUT: {
                    String arg = options.get(S);
                    files = (arg == null ? null : Collections.singleton(new File(arg)));
                    break;
                }
            }
        }
        PathsForLocation pl = new PathsForLocation();
        if (files != null) {
            for (File f: files)
                pl.add(f.toPath());
        }
        pathsForLocation.put(locn, pl);
    }
    private void lazyInitSearchPaths() {
        if (!inited) {
            setDefaultForLocation(PLATFORM_CLASS_PATH);
            setDefaultForLocation(CLASS_PATH);
            setDefaultForLocation(SOURCE_PATH);
            inited = true;
        }
    }
        private boolean inited = false;
    private Map<Location, PathsForLocation> pathsForLocation;
    private Paths searchPaths;
    private static class PathsForLocation extends LinkedHashSet<Path> {
        private static final long serialVersionUID = 6788510222394486733L;
    }
    @Override
    public Path getPath(FileObject fo) {
        nullCheck(fo);
        if (!(fo instanceof PathFileObject))
            throw new IllegalArgumentException();
        return ((PathFileObject) fo).getPath();
    }
    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        nullCheck(a);
        nullCheck(b);
        if (!(a instanceof PathFileObject))
            throw new IllegalArgumentException("Not supported: " + a);
        if (!(b instanceof PathFileObject))
            throw new IllegalArgumentException("Not supported: " + b);
        return ((PathFileObject) a).isSameFile((PathFileObject) b);
    }
    @Override
    public Iterable<JavaFileObject> list(Location location,
            String packageName, Set<Kind> kinds, boolean recurse)
            throws IOException {
        nullCheck(packageName);
        nullCheck(kinds);
        Iterable<? extends Path> paths = getLocation(location);
        if (paths == null)
            return List.nil();
        ListBuffer<JavaFileObject> results = new ListBuffer<JavaFileObject>();
        for (Path path : paths)
            list(path, packageName, kinds, recurse, results);
        return results.toList();
    }
    private void list(Path path, String packageName, final Set<Kind> kinds,
            boolean recurse, final ListBuffer<JavaFileObject> results)
            throws IOException {
        if (!Files.exists(path))
            return;
        final Path pathDir;
        if (isDirectory(path))
            pathDir = path;
        else {
            FileSystem fs = getFileSystem(path);
            if (fs == null)
                return;
            pathDir = fs.getRootDirectories().iterator().next();
        }
        String sep = path.getFileSystem().getSeparator();
        Path packageDir = packageName.isEmpty() ? pathDir
                : pathDir.resolve(packageName.replace(".", sep));
        if (!Files.exists(packageDir))
            return;
        int maxDepth = (recurse ? Integer.MAX_VALUE : 1);
        Set<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
        Files.walkFileTree(packageDir, opts, maxDepth,
                new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path name = dir.getFileName();
                if (name == null || SourceVersion.isIdentifier(name.toString())) 
                    return FileVisitResult.CONTINUE;
                else
                    return FileVisitResult.SKIP_SUBTREE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && kinds.contains(getKind(file.getFileName().toString()))) {
                    JavaFileObject fe =
                        PathFileObject.createDirectoryPathFileObject(
                            JavacPathFileManager.this, file, pathDir);
                    results.append(fe);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(
        Iterable<? extends Path> paths) {
        ArrayList<PathFileObject> result;
        if (paths instanceof Collection<?>)
            result = new ArrayList<PathFileObject>(((Collection<?>)paths).size());
        else
            result = new ArrayList<PathFileObject>();
        for (Path p: paths)
            result.add(PathFileObject.createSimplePathFileObject(this, nullCheck(p)));
        return result;
    }
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(Path... paths) {
        return getJavaFileObjectsFromPaths(Arrays.asList(nullCheck(paths)));
    }
    @Override
    public JavaFileObject getJavaFileForInput(Location location,
            String className, Kind kind) throws IOException {
        return getFileForInput(location, getRelativePath(className, kind));
    }
    @Override
    public FileObject getFileForInput(Location location,
            String packageName, String relativeName) throws IOException {
        return getFileForInput(location, getRelativePath(packageName, relativeName));
    }
    private JavaFileObject getFileForInput(Location location, String relativePath)
            throws IOException {
        for (Path p: getLocation(location)) {
            if (isDirectory(p)) {
                Path f = resolve(p, relativePath);
                if (Files.exists(f))
                    return PathFileObject.createDirectoryPathFileObject(this, f, p);
            } else {
                FileSystem fs = getFileSystem(p);
                if (fs != null) {
                    Path file = getPath(fs, relativePath);
                    if (Files.exists(file))
                        return PathFileObject.createJarPathFileObject(this, file);
                }
            }
        }
        return null;
    }
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
            String className, Kind kind, FileObject sibling) throws IOException {
        return getFileForOutput(location, getRelativePath(className, kind), sibling);
    }
    @Override
    public FileObject getFileForOutput(Location location, String packageName,
            String relativeName, FileObject sibling)
            throws IOException {
        return getFileForOutput(location, getRelativePath(packageName, relativeName), sibling);
    }
    private JavaFileObject getFileForOutput(Location location,
            String relativePath, FileObject sibling) {
        Path dir = getOutputLocation(location);
        if (dir == null) {
            if (location == CLASS_OUTPUT) {
                Path siblingDir = null;
                if (sibling != null && sibling instanceof PathFileObject) {
                    siblingDir = ((PathFileObject) sibling).getPath().getParent();
                }
                return PathFileObject.createSiblingPathFileObject(this,
                        siblingDir.resolve(getBaseName(relativePath)),
                        relativePath);
            } else if (location == SOURCE_OUTPUT) {
                dir = getOutputLocation(CLASS_OUTPUT);
            }
        }
        Path file;
        if (dir != null) {
            file = resolve(dir, relativePath);
            return PathFileObject.createDirectoryPathFileObject(this, file, dir);
        } else {
            file = getPath(getDefaultFileSystem(), relativePath);
            return PathFileObject.createSimplePathFileObject(this, file);
        }
    }
    @Override
    public String inferBinaryName(Location location, JavaFileObject fo) {
        nullCheck(fo);
        Iterable<? extends Path> paths = getLocation(location);
        if (paths == null) {
            return null;
        }
        if (!(fo instanceof PathFileObject))
            throw new IllegalArgumentException(fo.getClass().getName());
        return ((PathFileObject) fo).inferBinaryName(paths);
    }
    private FileSystem getFileSystem(Path p) throws IOException {
        FileSystem fs = fileSystems.get(p);
        if (fs == null) {
            fs = FileSystems.newFileSystem(p, null);
            fileSystems.put(p, fs);
        }
        return fs;
    }
    private Map<Path,FileSystem> fileSystems;
    private static String getRelativePath(String className, Kind kind) {
        return className.replace(".", "/") + kind.extension;
    }
    private static String getRelativePath(String packageName, String relativeName) {
        return packageName.replace(".", "/") + relativeName;
    }
    private static String getBaseName(String relativePath) {
        int lastSep = relativePath.lastIndexOf("/");
        return relativePath.substring(lastSep + 1); 
    }
    private static boolean isDirectory(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return attrs.isDirectory();
    }
    private static Path getPath(FileSystem fs, String relativePath) {
        return fs.getPath(relativePath.replace("/", fs.getSeparator()));
    }
    private static Path resolve(Path base, String relativePath) {
        FileSystem fs = base.getFileSystem();
        Path rp = fs.getPath(relativePath.replace("/", fs.getSeparator()));
        return base.resolve(rp);
    }
}
