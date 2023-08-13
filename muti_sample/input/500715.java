class TouchDexLoader extends ClassLoader {
    private String path;
    private boolean initialized;
    public TouchDexLoader(String path, ClassLoader parent) {
        super(parent);
        if (path == null)
            throw new NullPointerException();
        this.path = path;
    }
    private void ensureInit() {
        if (initialized) {
            return;
        }
        initialized = true;
        mPaths = path.split(":");
        mFiles = new File[mPaths.length];
        mZips = new ZipFile[mPaths.length];
        mDexs = new DexFile[mPaths.length];
        boolean wantDex = 
            System.getProperty("android.vm.dexfile", "").equals("true");
        for (int i = 0; i < mPaths.length; i++) {
            File pathFile = new File(mPaths[i]);
            mFiles[i] = pathFile;
            if (pathFile.isFile()) {
                if (false) {    
                try {
                    mZips[i] = new ZipFile(pathFile);
                }
                catch (IOException ioex) {
                }
                }               
                if (wantDex) {
                    try {
                        mDexs[i] = new DexFile(pathFile);
                    }
                    catch (IOException ioex) {
                        System.err.println("Couldn't open " + mPaths[i]
                            + " as DEX");
                    }
                }
            } else {
                System.err.println("File not found: " + mPaths[i]);
            }
        }
        String pathList = System.getProperty("java.library.path", ".");
        String pathSep = System.getProperty("path.separator", ":");
        String fileSep = System.getProperty("file.separator", "/");
        mLibPaths = pathList.split(pathSep);
        for (int i = 0; i < mLibPaths.length; i++) {
            if (!mLibPaths[i].endsWith(fileSep))
                mLibPaths[i] += fileSep;
            if (false)
                System.out.println("Native lib path:  " + mLibPaths[i]);
        }
    }
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        ensureInit();
        byte[] data = null;
        int i;
        for (i = 0; i < mPaths.length; i++) {
            if (mDexs[i] != null) {
                String slashName = name.replace('.', '/');
                Class clazz = mDexs[i].loadClass(slashName, this);
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
            if (data != null) {
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex != -1) {
                    String packageName = name.substring(0, dotIndex);
                    synchronized (this) {
                        Package packageObj = getPackage(packageName);
                        if (packageObj == null) {
                            definePackage(packageName, null, null,
                                    null, null, null, null, null);
                        }
                    }
                }
                return defineClass(name, data, 0, data.length);
            }
        }
        throw new ClassNotFoundException(name + " in loader " + this);
    }
    protected URL findResource(String name) {
        ensureInit();
        byte[] data = null;
        int i;
        for (i = 0; i < mPaths.length; i++) {
            File pathFile = mFiles[i];
            ZipFile zip = mZips[i];
            if (zip != null) {
                if (isInArchive(zip, name)) {
                    try {
                        return new URL("jar:file:
                    }
                    catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (pathFile.isDirectory()) {
                File dataFile = new File(mPaths[i] + "/" + name);
                if (dataFile.exists()) {
                    try {
                        return new URL("file:" + name);
                    }
                    catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (pathFile.isFile()) {
            } else {
                System.err.println("TouchDexLoader: can't find '"
                    + mPaths[i] + "'");
            }
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
    private String[] mPaths;
    private File[] mFiles;
    private ZipFile[] mZips;
    private DexFile[] mDexs;
    private String[] mLibPaths;
}
