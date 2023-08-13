public class FancyLoader extends ClassLoader {
    static final String CLASS_PATH = "classes-ex/";
    static final String DEX_FILE = "test-ex.jar";
    private Class mDexClass;
    private Object mDexFile;
    public FancyLoader(ClassLoader parent) {
        super(parent);
        try {
            mDexClass = parent.loadClass("dalvik/system/DexFile");
        } catch (ClassNotFoundException cnfe) {
        }
    }
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        if (mDexClass != null) {
            return findClassDalvik(name);
        } else {
            return findClassNonDalvik(name);
        }
    }
    private Class<?> findClassDalvik(String name)
        throws ClassNotFoundException {
        if (mDexFile == null) {
            synchronized (FancyLoader.class) {
                Constructor ctor;
                try {
                    ctor = mDexClass.getConstructor(new Class[] {String.class});
                } catch (NoSuchMethodException nsme) {
                    throw new ClassNotFoundException("getConstructor failed",
                        nsme);
                }
                try {
                    mDexFile = ctor.newInstance(DEX_FILE);
                } catch (InstantiationException ie) {
                    throw new ClassNotFoundException("newInstance failed", ie);
                } catch (IllegalAccessException iae) {
                    throw new ClassNotFoundException("newInstance failed", iae);
                } catch (InvocationTargetException ite) {
                    throw new ClassNotFoundException("newInstance failed", ite);
                }
            }
        }
        Method meth;
        try {
            meth = mDexClass.getMethod("loadClass",
                    new Class[] { String.class, ClassLoader.class });
        } catch (NoSuchMethodException nsme) {
            throw new ClassNotFoundException("getMethod failed", nsme);
        }
        try {
            meth.invoke(mDexFile, name, this);
        } catch (IllegalAccessException iae) {
            throw new ClassNotFoundException("loadClass failed", iae);
        } catch (InvocationTargetException ite) {
            throw new ClassNotFoundException("loadClass failed",
                ite.getCause());
        }
        return null;
    }
    private Class<?> findClassNonDalvik(String name)
        throws ClassNotFoundException {
        String pathName = CLASS_PATH + name + ".class";
        File path = new File(pathName);
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException fnfe) {
            throw new ClassNotFoundException("Not found: " + pathName);
        }
        byte[] fileData;
        try {
            fileData = new byte[(int) raf.length()];
            raf.readFully(fileData);
        } catch (IOException ioe) {
            throw new ClassNotFoundException("Read error: " + pathName);
        } finally {
            try {
                raf.close();
            } catch (IOException ioe) {
            }
        }
        try {
            return defineClass(name, fileData, 0, fileData.length);
        } catch (Throwable th) {
            throw new ClassNotFoundException("defineClass failed", th);
        }
    }
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        Class res;
        res = findLoadedClass(name);
        if (res != null) {
            System.out.println("FancyLoader.loadClass: "
                + name + " already loaded");
            if (resolve)
                resolveClass(res);
            return res;
        }
        try {
            res = findClass(name);
            if (resolve)
                resolveClass(res);
        }
        catch (ClassNotFoundException e) {
        }
        res = super.loadClass(name, resolve);   
        return res;
    }
}
