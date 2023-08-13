public class DexClassLoader extends ClassLoader {
    private static final boolean VERBOSE_DEBUG = false;
    private final String mRawDexPath;
    private final String mRawLibPath;
    private final String mDexOutputPath;
    private boolean mInitialized;
    private File[] mFiles;              
    private ZipFile[] mZips;            
    private DexFile[] mDexs;            
    private String[] mLibPaths;
    public DexClassLoader(String dexPath, String dexOutputDir, String libPath,
        ClassLoader parent) {
        super(parent);
        if (dexPath == null || dexOutputDir == null)
            throw new NullPointerException();
        mRawDexPath = dexPath;
        mDexOutputPath = dexOutputDir;
        mRawLibPath = libPath;
    }
    private synchronized void ensureInit() {
        if (mInitialized) {
            return;
        }
        String[] dexPathList;
        mInitialized = true;
        dexPathList = mRawDexPath.split(":");
        int length = dexPathList.length;
        mFiles = new File[length];
        mZips = new ZipFile[length];
        mDexs = new DexFile[length];
        for (int i = 0; i < length; i++) {
            File pathFile = new File(dexPathList[i]);
            mFiles[i] = pathFile;
            if (pathFile.isFile()) {
                try {
                    mZips[i] = new ZipFile(pathFile);
                } catch (IOException ioex) {
                    System.out.println("Failed opening '" + pathFile
                        + "': " + ioex);
                }
                try {
                    String outputName =
                        generateOutputName(dexPathList[i], mDexOutputPath);
                    mDexs[i] = DexFile.loadDex(dexPathList[i], outputName, 0);
                } catch (IOException ioex) {
                    System.out.println("Failed loadDex '" + pathFile
                        + "': " + ioex);
                }
            } else {
                if (VERBOSE_DEBUG)
                    System.out.println("Not found: " + pathFile.getPath());
            }
        }
        String pathList = System.getProperty("java.library.path", ".");
        String pathSep = System.getProperty("path.separator", ":");
        String fileSep = System.getProperty("file.separator", "/");
        if (mRawLibPath != null) {
            if (pathList.length() > 0) {
                pathList += pathSep + mRawLibPath;
            }
            else {
                pathList = mRawLibPath;
            }
        }
        mLibPaths = pathList.split(pathSep);
        length = mLibPaths.length;
        for (int i = 0; i < length; i++) {
            if (!mLibPaths[i].endsWith(fileSep))
                mLibPaths[i] += fileSep;
            if (VERBOSE_DEBUG)
                System.out.println("Native lib path " +i+ ":  " + mLibPaths[i]);
        }
    }
    private static String generateOutputName(String sourcePathName,
        String outputDir) {
        StringBuilder newStr = new StringBuilder(80);
        newStr.append(outputDir);
        if (!outputDir.endsWith("/"))
            newStr.append("/");
        String sourceFileName;
        int lastSlash = sourcePathName.lastIndexOf("/");
        if (lastSlash < 0)
            sourceFileName = sourcePathName;
        else
            sourceFileName = sourcePathName.substring(lastSlash+1);
        int lastDot = sourceFileName.lastIndexOf(".");
        if (lastDot < 0)
            newStr.append(sourceFileName);
        else
            newStr.append(sourceFileName, 0, lastDot);
        newStr.append(".dex");
        if (VERBOSE_DEBUG)
            System.out.println("Output file will be " + newStr.toString());
        return newStr.toString();
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ensureInit();
        if (VERBOSE_DEBUG)
            System.out.println("DexClassLoader " + this
                + ": findClass '" + name + "'");
        byte[] data = null;
        int length = mFiles.length;
        for (int i = 0; i < length; i++) {
            if (VERBOSE_DEBUG)
                System.out.println("  Now searching: " + mFiles[i].getPath());
            if (mDexs[i] != null) {
                String slashName = name.replace('.', '/');
                Class clazz = mDexs[i].loadClass(slashName, this);
                if (clazz != null) {
                    if (VERBOSE_DEBUG)
                        System.out.println("    found");
                    return clazz;
                }
            }
        }
        throw new ClassNotFoundException(name + " in loader " + this);
    }
    @Override
    protected URL findResource(String name) {
        ensureInit();
        int length = mFiles.length;
        for (int i = 0; i < length; i++) {
            File pathFile = mFiles[i];
            ZipFile zip = mZips[i];
            if (zip.getEntry(name) != null) {
                if (VERBOSE_DEBUG)
                    System.out.println("  found " + name + " in " + pathFile);
                try {
                    return new URL("jar:" + pathFile.toURL() + "!/" + name);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (VERBOSE_DEBUG)
            System.out.println("  resource " + name + " not found");
        return null;
    }
    @Override
    protected String findLibrary(String libname) {
        ensureInit();
        String fileName = System.mapLibraryName(libname);
        for (int i = 0; i < mLibPaths.length; i++) {
            String pathName = mLibPaths[i] + fileName;
            File test = new File(pathName);
            if (test.exists()) {
                if (VERBOSE_DEBUG)
                    System.out.println("  found " + libname);
                return pathName;
            }
        }
        if (VERBOSE_DEBUG)
            System.out.println("  library " + libname + " not found");
        return null;
    }
    @Override
    protected Package getPackage(String name) {
        if (name != null && !"".equals(name)) {
            synchronized(this) {
                Package pack = super.getPackage(name);
                if (pack == null) {
                    pack = definePackage(name, "Unknown", "0.0", "Unknown",
                            "Unknown", "0.0", "Unknown", null);
                }
                return pack;
            }
        }
        return null;
    }
}
