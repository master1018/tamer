public class Paths {
    protected static final Context.Key<Paths> pathsKey =
        new Context.Key<Paths>();
    public static Paths instance(Context context) {
        Paths instance = context.get(pathsKey);
        if (instance == null)
            instance = new Paths(context);
        return instance;
    }
    private Log log;
    private Options options;
    private Lint lint;
    private FSInfo fsInfo;
    protected Paths(Context context) {
        context.put(pathsKey, this);
        pathsForLocation = new HashMap<Location,Path>(16);
        setContext(context);
    }
    void setContext(Context context) {
        log = Log.instance(context);
        options = Options.instance(context);
        lint = Lint.instance(context);
        fsInfo = FSInfo.instance(context);
    }
    private boolean warn;
    private Map<Location, Path> pathsForLocation;
    private boolean inited = false; 
    private File defaultBootClassPathRtJar = null;
    private boolean isDefaultBootClassPath;
    Path getPathForLocation(Location location) {
        Path path = pathsForLocation.get(location);
        if (path == null)
            setPathForLocation(location, null);
        return pathsForLocation.get(location);
    }
    void setPathForLocation(Location location, Iterable<? extends File> path) {
        Path p;
        if (path == null) {
            if (location == CLASS_PATH)
                p = computeUserClassPath();
            else if (location == PLATFORM_CLASS_PATH)
                p = computeBootClassPath(); 
            else if (location == ANNOTATION_PROCESSOR_PATH)
                p = computeAnnotationProcessorPath();
            else if (location == SOURCE_PATH)
                p = computeSourcePath();
            else
                p = null;
        } else {
            if (location == PLATFORM_CLASS_PATH) {
                defaultBootClassPathRtJar = null;
                isDefaultBootClassPath = false;
            }
            p = new Path();
            for (File f: path)
                p.addFile(f, warn); 
        }
        pathsForLocation.put(location, p);
    }
    public boolean isDefaultBootClassPath() {
        lazy();
        return isDefaultBootClassPath;
    }
    protected void lazy() {
        if (!inited) {
            warn = lint.isEnabled(Lint.LintCategory.PATH);
            pathsForLocation.put(PLATFORM_CLASS_PATH, computeBootClassPath());
            pathsForLocation.put(CLASS_PATH, computeUserClassPath());
            pathsForLocation.put(SOURCE_PATH, computeSourcePath());
            inited = true;
        }
    }
    public Collection<File> bootClassPath() {
        lazy();
        return Collections.unmodifiableCollection(getPathForLocation(PLATFORM_CLASS_PATH));
    }
    public Collection<File> userClassPath() {
        lazy();
        return Collections.unmodifiableCollection(getPathForLocation(CLASS_PATH));
    }
    public Collection<File> sourcePath() {
        lazy();
        Path p = getPathForLocation(SOURCE_PATH);
        return p == null || p.size() == 0
            ? null
            : Collections.unmodifiableCollection(p);
    }
    boolean isDefaultBootClassPathRtJar(File file) {
        return file.equals(defaultBootClassPathRtJar);
    }
    private static Iterable<File> getPathEntries(String path) {
        return getPathEntries(path, null);
    }
    private static Iterable<File> getPathEntries(String path, File emptyPathDefault) {
        ListBuffer<File> entries = new ListBuffer<File>();
        int start = 0;
        while (start <= path.length()) {
            int sep = path.indexOf(File.pathSeparatorChar, start);
            if (sep == -1)
                sep = path.length();
            if (start < sep)
                entries.add(new File(path.substring(start, sep)));
            else if (emptyPathDefault != null)
                entries.add(emptyPathDefault);
            start = sep + 1;
        }
        return entries;
    }
    private class Path extends LinkedHashSet<File> {
        private static final long serialVersionUID = 0;
        private boolean expandJarClassPaths = false;
        private Set<File> canonicalValues = new HashSet<File>();
        public Path expandJarClassPaths(boolean x) {
            expandJarClassPaths = x;
            return this;
        }
        private File emptyPathDefault = null;
        public Path emptyPathDefault(File x) {
            emptyPathDefault = x;
            return this;
        }
        public Path() { super(); }
        public Path addDirectories(String dirs, boolean warn) {
            boolean prev = expandJarClassPaths;
            expandJarClassPaths = true;
            try {
                if (dirs != null)
                    for (File dir : getPathEntries(dirs))
                        addDirectory(dir, warn);
                return this;
            } finally {
                expandJarClassPaths = prev;
            }
        }
        public Path addDirectories(String dirs) {
            return addDirectories(dirs, warn);
        }
        private void addDirectory(File dir, boolean warn) {
            if (!dir.isDirectory()) {
                if (warn)
                    log.warning(Lint.LintCategory.PATH,
                            "dir.path.element.not.found", dir);
                return;
            }
            File[] files = dir.listFiles();
            if (files == null)
                return;
            for (File direntry : files) {
                if (isArchive(direntry))
                    addFile(direntry, warn);
            }
        }
        public Path addFiles(String files, boolean warn) {
            if (files != null) {
                for (File file : getPathEntries(files, emptyPathDefault))
                    addFile(file, warn);
            }
            return this;
        }
        public Path addFiles(String files) {
            return addFiles(files, warn);
        }
        public void addFile(File file, boolean warn) {
            if (contains(file)) {
                return;
            }
            if (! fsInfo.exists(file)) {
                if (warn) {
                    log.warning(Lint.LintCategory.PATH,
                            "path.element.not.found", file);
                }
                super.add(file);
                return;
            }
            File canonFile = fsInfo.getCanonicalFile(file);
            if (canonicalValues.contains(canonFile)) {
                return;
            }
            if (fsInfo.isFile(file)) {
                if (!isArchive(file)) {
                    try {
                        ZipFile z = new ZipFile(file);
                        z.close();
                        if (warn) {
                            log.warning(Lint.LintCategory.PATH,
                                    "unexpected.archive.file", file);
                        }
                    } catch (IOException e) {
                        if (warn) {
                            log.warning(Lint.LintCategory.PATH,
                                    "invalid.archive.file", file);
                        }
                        return;
                    }
                }
            }
            super.add(file);
            canonicalValues.add(canonFile);
            if (expandJarClassPaths && fsInfo.isFile(file))
                addJarClassPath(file, warn);
        }
        private void addJarClassPath(File jarFile, boolean warn) {
            try {
                for (File f: fsInfo.getJarClassPath(jarFile)) {
                    addFile(f, warn);
                }
            } catch (IOException e) {
                log.error("error.reading.file", jarFile, JavacFileManager.getMessage(e));
            }
        }
    }
    private Path computeBootClassPath() {
        defaultBootClassPathRtJar = null;
        Path path = new Path();
        String bootclasspathOpt = options.get(BOOTCLASSPATH);
        String endorseddirsOpt = options.get(ENDORSEDDIRS);
        String extdirsOpt = options.get(EXTDIRS);
        String xbootclasspathPrependOpt = options.get(XBOOTCLASSPATH_PREPEND);
        String xbootclasspathAppendOpt = options.get(XBOOTCLASSPATH_APPEND);
        path.addFiles(xbootclasspathPrependOpt);
        if (endorseddirsOpt != null)
            path.addDirectories(endorseddirsOpt);
        else
            path.addDirectories(System.getProperty("java.endorsed.dirs"), false);
        if (bootclasspathOpt != null) {
            path.addFiles(bootclasspathOpt);
        } else {
            String files = System.getProperty("sun.boot.class.path");
            path.addFiles(files, false);
            File rt_jar = new File("rt.jar");
            for (File file : getPathEntries(files)) {
                if (new File(file.getName()).equals(rt_jar))
                    defaultBootClassPathRtJar = file;
            }
        }
        path.addFiles(xbootclasspathAppendOpt);
        if (extdirsOpt != null)
            path.addDirectories(extdirsOpt);
        else
            path.addDirectories(System.getProperty("java.ext.dirs"), false);
        isDefaultBootClassPath =
                (xbootclasspathPrependOpt == null) &&
                (bootclasspathOpt == null) &&
                (xbootclasspathAppendOpt == null);
        return path;
    }
    private Path computeUserClassPath() {
        String cp = options.get(CLASSPATH);
        if (cp == null) cp = System.getProperty("env.class.path");
        if (cp == null && System.getProperty("application.home") == null)
            cp = System.getProperty("java.class.path");
        if (cp == null) cp = ".";
        return new Path()
            .expandJarClassPaths(true)        
            .emptyPathDefault(new File("."))  
            .addFiles(cp);
    }
    private Path computeSourcePath() {
        String sourcePathArg = options.get(SOURCEPATH);
        if (sourcePathArg == null)
            return null;
        return new Path().addFiles(sourcePathArg);
    }
    private Path computeAnnotationProcessorPath() {
        String processorPathArg = options.get(PROCESSORPATH);
        if (processorPathArg == null)
            return null;
        return new Path().addFiles(processorPathArg);
    }
    private Path sourceSearchPath;
    public Collection<File> sourceSearchPath() {
        if (sourceSearchPath == null) {
            lazy();
            Path sourcePath = getPathForLocation(SOURCE_PATH);
            Path userClassPath = getPathForLocation(CLASS_PATH);
            sourceSearchPath = sourcePath != null ? sourcePath : userClassPath;
        }
        return Collections.unmodifiableCollection(sourceSearchPath);
    }
    private Path classSearchPath;
    public Collection<File> classSearchPath() {
        if (classSearchPath == null) {
            lazy();
            Path bootClassPath = getPathForLocation(PLATFORM_CLASS_PATH);
            Path userClassPath = getPathForLocation(CLASS_PATH);
            classSearchPath = new Path();
            classSearchPath.addAll(bootClassPath);
            classSearchPath.addAll(userClassPath);
        }
        return Collections.unmodifiableCollection(classSearchPath);
    }
    private Path otherSearchPath;
    Collection<File> otherSearchPath() {
        if (otherSearchPath == null) {
            lazy();
            Path userClassPath = getPathForLocation(CLASS_PATH);
            Path sourcePath = getPathForLocation(SOURCE_PATH);
            if (sourcePath == null)
                otherSearchPath = userClassPath;
            else {
                otherSearchPath = new Path();
                otherSearchPath.addAll(userClassPath);
                otherSearchPath.addAll(sourcePath);
            }
        }
        return Collections.unmodifiableCollection(otherSearchPath);
    }
    private boolean isArchive(File file) {
        String n = file.getName().toLowerCase();
        return fsInfo.isFile(file)
            && (n.endsWith(".jar") || n.endsWith(".zip"));
    }
    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            URL url = fileToURL(new File(st.nextToken()));
            if (url != null) {
                urls[count++] = url;
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }
    private static URL fileToURL(File file) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            name = file.getAbsolutePath();
        }
        name = name.replace(File.separatorChar, '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        if (!file.isFile()) {
            name = name + "/";
        }
        try {
            return new URL("file", "", name);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(file.toString());
        }
    }
}
