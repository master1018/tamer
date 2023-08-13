public class PathClassLoader extends ClassLoader {
    private final String path;
    private final String libPath;
    private boolean initialized;
    private String[] mPaths;
    private File[] mFiles;
    private ZipFile[] mZips;
    private DexFile[] mDexs;
    private String[] mLibPaths;
    public PathClassLoader(String path, ClassLoader parent) {
        this(path, null, parent);
    }
    public PathClassLoader(String path, String libPath, ClassLoader parent) {
        super(parent);
        if (path == null)
            throw new NullPointerException();
        this.path = path;
        this.libPath = libPath;
    }
    private synchronized void ensureInit() {
        if (initialized) {
            return;
        }
        initialized = true;
        mPaths = path.split(":");
        int length = mPaths.length;
        mFiles = new File[length];
        mZips = new ZipFile[length];
        mDexs = new DexFile[length];
        boolean wantDex =
            System.getProperty("android.vm.dexfile", "").equals("true");
        for (int i = 0; i < length; i++) {
            File pathFile = new File(mPaths[i]);
            mFiles[i] = pathFile;
            if (pathFile.isFile()) {
                try {
                    mZips[i] = new ZipFile(pathFile);
                }
                catch (IOException ioex) {
                }
                if (wantDex) {
                    try {
                        mDexs[i] = new DexFile(pathFile);
                    }
                    catch (IOException ioex) {}
                }
            }
        }
        String pathList = System.getProperty("java.library.path", ".");
        String pathSep = System.getProperty("path.separator", ":");
        String fileSep = System.getProperty("file.separator", "/");
        if (libPath != null) {
            if (pathList.length() > 0) {
                pathList += pathSep + libPath;
            }
            else {
                pathList = libPath;
            }
        }
        mLibPaths = pathList.split(pathSep);
        length = mLibPaths.length;
        for (int i = 0; i < length; i++) {
            if (!mLibPaths[i].endsWith(fileSep))
                mLibPaths[i] += fileSep;
            if (false)
                System.out.println("Native lib path:  " + mLibPaths[i]);
        }
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        ensureInit();
        byte[] data = null;
        int length = mPaths.length;
        for (int i = 0; i < length; i++) {
            if (mDexs[i] != null) {
                Class clazz = mDexs[i].loadClassBinaryName(name, this);
                if (clazz != null)
                    return clazz;
            } else if (mZips[i] != null) {
                String fileName = name.replace('.', '/') + ".class";
                data = loadFromArchive(mZips[i], fileName);
            } else {
                File pathFile = mFiles[i];
                if (pathFile.isDirectory()) {
                    String fileName =
                        mPaths[i] + "/" + name.replace('.', '/') + ".class";
                    data = loadFromDirectory(fileName);
                } else {
                }
            }
        }
        throw new ClassNotFoundException(name + " in loader " + this);
    }
    @Override
    protected URL findResource(String name) {
        ensureInit();
        int length = mPaths.length;
        for (int i = 0; i < length; i++) {
            URL result = findResource(name, i);
            if(result != null) {
                return result;
            }
        }
        return null;
    }
    @Override
    protected Enumeration<URL> findResources(String resName) {
        ensureInit();
        int length = mPaths.length;
        ArrayList<URL> results = new ArrayList<URL>();
        for (int i = 0; i < length; i++) {
            URL result = findResource(resName, i);
            if(result != null) {
                results.add(result);
            }
        }
        return new EnumerateListArray<URL>(results);
    }
    private URL findResource(String name, int i) {
        File pathFile = mFiles[i];
        ZipFile zip = mZips[i];
        if (zip != null) {
            if (isInArchive(zip, name)) {
                try {
                    return new URL("jar:" + pathFile.toURL() + "!/" + name);
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (pathFile.isDirectory()) {
            File dataFile = new File(mPaths[i] + "/" + name);
            if (dataFile.exists()) {
                try {
                    return dataFile.toURL();
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (pathFile.isFile()) {
        } else {
            System.err.println("PathClassLoader: can't find '"
                + mPaths[i] + "'");
        }
        return null;
    }
    private byte[] loadFromDirectory(String path) {
        RandomAccessFile raf;
        byte[] fileData;
        try {
            raf = new RandomAccessFile(path, "r");
        }
        catch (FileNotFoundException fnfe) {
            return null;
        }
        try {
            fileData = new byte[(int) raf.length()];
            raf.read(fileData);
            raf.close();
        }
        catch (IOException ioe) {
            System.err.println("Error reading from " + path);
            fileData = null;
        }
        return fileData;
    }
    private byte[] loadFromArchive(ZipFile zip, String name) {
        ZipEntry entry;
        entry = zip.getEntry(name);
        if (entry == null)
            return null;
        ByteArrayOutputStream byteStream;
        InputStream stream;
        int count;
        try {
            stream = zip.getInputStream(entry);
            byteStream = new ByteArrayOutputStream((int) entry.getSize());
            byte[] buf = new byte[4096];
            while ((count = stream.read(buf)) > 0)
                byteStream.write(buf, 0, count);
            stream.close();
        }
        catch (IOException ioex) {
            return null;
        }
        return byteStream.toByteArray();
    }
    private boolean isInArchive(ZipFile zip, String name) {
        return zip.getEntry(name) != null;
    }
    protected String findLibrary(String libname) {
        ensureInit();
        String fileName = System.mapLibraryName(libname);
        for (int i = 0; i < mLibPaths.length; i++) {
            String pathName = mLibPaths[i] + fileName;
            File test = new File(pathName);
            if (test.exists())
                return pathName;
        }
        return null;
    }
    @Override
    protected Package getPackage(String name) {
        if (name != null && !"".equals(name)) {
            synchronized(this) {
                Package pack = super.getPackage(name);
                if (pack == null) {
                    pack = definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
                }
                return pack;
            }
        }
        return null;
    }
    private static class EnumerateListArray<T> implements Enumeration<T> {
        private final ArrayList mList;
        private int i = 0;
        EnumerateListArray(ArrayList list) {
            mList = list;
        }
        public boolean hasMoreElements() {
            return i < mList.size();
        }
        public T nextElement() {
            if (i >= mList.size())
                throw new NoSuchElementException();
            return (T) mList.get(i++);
        }
    };
    public String toString () {
        return getClass().getName() + "[" + path + "]";
    }
}
