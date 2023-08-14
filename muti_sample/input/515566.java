public class ClassPathPackageInfoSource {
    private static final String CLASS_EXTENSION = ".class";
    private static final ClassLoader CLASS_LOADER
            = ClassPathPackageInfoSource.class.getClassLoader();
    private final SimpleCache<String, ClassPathPackageInfo> cache =
            new SimpleCache<String, ClassPathPackageInfo>() {
                @Override
                protected ClassPathPackageInfo load(String pkgName) {
                    return createPackageInfo(pkgName);
                }
            };
    private final String[] classPath;
    private static String[] apkPaths;
    private final Map<File, Set<String>> jarFiles = Maps.newHashMap();
    private ClassLoader classLoader;
    ClassPathPackageInfoSource() {
        classPath = getClassPath();
    }
    public static void setApkPaths(String[] apkPaths) {
        ClassPathPackageInfoSource.apkPaths = apkPaths;
    }
    public ClassPathPackageInfo getPackageInfo(String pkgName) {
        return cache.get(pkgName);
    }
    private ClassPathPackageInfo createPackageInfo(String packageName) {
        Set<String> subpackageNames = new TreeSet<String>();
        Set<String> classNames = new TreeSet<String>();
        Set<Class<?>> topLevelClasses = Sets.newHashSet();
        findClasses(packageName, classNames, subpackageNames);
        for (String className : classNames) {
            if (className.endsWith(".R") || className.endsWith(".Manifest")) {
                continue;
            }
            try {
                topLevelClasses.add(Class.forName(className, false,
                        (classLoader != null) ? classLoader : CLASS_LOADER));
            } catch (ClassNotFoundException e) {
                Log.w("ClassPathPackageInfoSource", "Cannot load class. "
                        + "Make sure it is in your apk. Class name: '" + className
                        + "'. Message: " + e.getMessage(), e);
            }
        }
        return new ClassPathPackageInfo(this, packageName, subpackageNames,
                topLevelClasses);
    }
    private void findClasses(String packageName, Set<String> classNames,
            Set<String> subpackageNames) {
        String packagePrefix = packageName + '.';
        String pathPrefix = packagePrefix.replace('.', '/');
        for (String entryName : classPath) {
            File classPathEntry = new File(entryName);
            if (classPathEntry.exists()) {
                try {
                    if (entryName.endsWith(".apk")) {
                        findClassesInApk(entryName, packageName, classNames, subpackageNames);
                    } else if ("true".equals(System.getProperty("android.vm.dexfile", "false"))) {
                        for (String apkPath : apkPaths) {
                            File file = new File(apkPath);
                            scanForApkFiles(file, packageName, classNames, subpackageNames);
                        }
                    } else if (entryName.endsWith(".jar")) {
                        findClassesInJar(classPathEntry, pathPrefix,
                                classNames, subpackageNames);
                    } else if (classPathEntry.isDirectory()) {
                        findClassesInDirectory(classPathEntry, packagePrefix, pathPrefix,
                                classNames, subpackageNames);
                    } else {
                        throw new AssertionError("Don't understand classpath entry " +
                                classPathEntry);
                    }
                } catch (IOException e) {
                    throw new AssertionError("Can't read classpath entry " +
                            entryName + ": " + e.getMessage());
                }
            }
        }
    }
    private void scanForApkFiles(File source, String packageName,
            Set<String> classNames, Set<String> subpackageNames) throws IOException {
        if (source.getPath().endsWith(".apk")) {
            findClassesInApk(source.getPath(), packageName, classNames, subpackageNames);
        } else {
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    scanForApkFiles(file, packageName, classNames, subpackageNames);
                }
            }
        }
    }
    private void findClassesInDirectory(File classDir,
            String packagePrefix, String pathPrefix, Set<String> classNames,
            Set<String> subpackageNames)
            throws IOException {
        File directory = new File(classDir, pathPrefix);
        if (directory.exists()) {
            for (File f : directory.listFiles()) {
                String name = f.getName();
                if (name.endsWith(CLASS_EXTENSION) && isToplevelClass(name)) {
                    classNames.add(packagePrefix + getClassName(name));
                } else if (f.isDirectory()) {
                    subpackageNames.add(packagePrefix + name);
                }
            }
        }
    }
    private void findClassesInJar(File jarFile, String pathPrefix,
            Set<String> classNames, Set<String> subpackageNames)
            throws IOException {
        Set<String> entryNames = getJarEntries(jarFile);
        if (!entryNames.contains(pathPrefix)) {
            return;
        }
        int prefixLength = pathPrefix.length();
        for (String entryName : entryNames) {
            if (entryName.startsWith(pathPrefix)) {
                if (entryName.endsWith(CLASS_EXTENSION)) {
                    int index = entryName.indexOf('/', prefixLength);
                    if (index >= 0) {
                        String p = entryName.substring(0, index).replace('/', '.');
                        subpackageNames.add(p);
                    } else if (isToplevelClass(entryName)) {
                        classNames.add(getClassName(entryName).replace('/', '.'));
                    }
                }
            }
        }
    }
    private void findClassesInApk(String apkPath, String packageName,
            Set<String> classNames, Set<String> subpackageNames)
            throws IOException {
        DexFile dexFile = null;
        try {
            dexFile = new DexFile(apkPath);
            Enumeration<String> apkClassNames = dexFile.entries();
            while (apkClassNames.hasMoreElements()) {
                String className = apkClassNames.nextElement();
                if (className.startsWith(packageName)) {
                    String subPackageName = packageName;
                    int lastPackageSeparator = className.lastIndexOf('.');
                    if (lastPackageSeparator > 0) {
                        subPackageName = className.substring(0, lastPackageSeparator);
                    }
                    if (subPackageName.length() > packageName.length()) {
                        subpackageNames.add(subPackageName);
                    } else if (isToplevelClass(className)) {
                        classNames.add(className);
                    }
                }
            }
        } catch (IOException e) {
            if (Config.LOGV) {
                Log.w("ClassPathPackageInfoSource",
                        "Error finding classes at apk path: " + apkPath, e);
            }
        } finally {
            if (dexFile != null) {
            }
        }
    }
    private Set<String> getJarEntries(File jarFile)
            throws IOException {
        Set<String> entryNames = jarFiles.get(jarFile);
        if (entryNames == null) {
            entryNames = Sets.newHashSet();
            ZipFile zipFile = new ZipFile(jarFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (entryName.endsWith(CLASS_EXTENSION)) {
                    entryNames.add(entryName);
                    int lastIndex = entryName.lastIndexOf('/');
                    do {
                        String packageName = entryName.substring(0, lastIndex + 1);
                        entryNames.add(packageName);
                        lastIndex = entryName.lastIndexOf('/', lastIndex - 1);
                    } while (lastIndex > 0);
                }
            }
            jarFiles.put(jarFile, entryNames);
        }
        return entryNames;
    }
    private static boolean isToplevelClass(String fileName) {
        return fileName.indexOf('$') < 0;
    }
    private static String getClassName(String className) {
        int classNameEnd = className.length() - CLASS_EXTENSION.length();
        return className.substring(0, classNameEnd);
    }
    private static String[] getClassPath() {
        String classPath = System.getProperty("java.class.path");
        String separator = System.getProperty("path.separator", ":");
        return classPath.split(Pattern.quote(separator));
    }
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
