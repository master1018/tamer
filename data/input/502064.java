public class AndroidJarLoader extends ClassLoader implements IAndroidClassLoader {
    public final static class ClassWrapper implements IClassDescriptor {
        private Class<?> mClass;
        public ClassWrapper(Class<?> clazz) {
            mClass = clazz;
        }
        public String getFullClassName() {
            return mClass.getCanonicalName();
        }
        public IClassDescriptor[] getDeclaredClasses() {
            Class<?>[] classes = mClass.getDeclaredClasses();
            IClassDescriptor[] iclasses = new IClassDescriptor[classes.length];
            for (int i = 0 ; i < classes.length ; i++) {
                iclasses[i] = new ClassWrapper(classes[i]);
            }
            return iclasses;
        }
        public IClassDescriptor getEnclosingClass() {
            return new ClassWrapper(mClass.getEnclosingClass());
        }
        public String getSimpleName() {
            return mClass.getSimpleName();
        }
        public IClassDescriptor getSuperclass() {
            return new ClassWrapper(mClass.getSuperclass());
        }
        @Override
        public boolean equals(Object clazz) {
            if (clazz instanceof ClassWrapper) {
                return mClass.equals(((ClassWrapper)clazz).mClass);
            }
            return super.equals(clazz);
        }
        @Override
        public int hashCode() {
            return mClass.hashCode();
        }
        public boolean isInstantiable() {
            int modifiers = mClass.getModifiers();
            return Modifier.isAbstract(modifiers) == false && Modifier.isPublic(modifiers) == true;
        }
        public Class<?> wrappedClass() {
            return mClass;
        }
    }
    private String mOsFrameworkLocation;
    private final HashMap<String, byte[]> mEntryCache = new HashMap<String, byte[]>();
    private final HashMap<String, Class<?> > mClassCache = new HashMap<String, Class<?> >();
    public AndroidJarLoader(String osFrameworkLocation) {
        super();
        mOsFrameworkLocation = osFrameworkLocation;
    }
    public String getSource() {
        return mOsFrameworkLocation;
    }
    public void preLoadClasses(String packageFilter, String taskLabel, IProgressMonitor monitor)
        throws IOException, InvalidAttributeValueException, ClassFormatError {
        String pathFilter = packageFilter.replaceAll("\\.", "/"); 
        SubMonitor progress = SubMonitor.convert(monitor, taskLabel == null ? "" : taskLabel, 100);
        FileInputStream fis = new FileInputStream(mOsFrameworkLocation);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry;       
        while ((entry = zis.getNextEntry()) != null) {
            String entryPath = entry.getName();
            if (!entryPath.endsWith(AndroidConstants.DOT_CLASS)) {
                continue;
            }
            if (pathFilter.length() > 0 && !entryPath.startsWith(pathFilter)) {
                continue;
            }
            String className = entryPathToClassName(entryPath);
            if (!mEntryCache.containsKey(className)) {
                long entrySize = entry.getSize();
                if (entrySize > Integer.MAX_VALUE) {
                    throw new InvalidAttributeValueException();
                }
                byte[] data = readZipData(zis, (int)entrySize);
                mEntryCache.put(className, data);
            }
            progress.setWorkRemaining(100);
            progress.worked(5);
            progress.subTask(String.format("Preload %1$s", className));
        }
    }
    public HashMap<String, ArrayList<IClassDescriptor>> findClassesDerivingFrom(
            String packageFilter,
            String[] superClasses)
            throws IOException, InvalidAttributeValueException, ClassFormatError {
        packageFilter = packageFilter.replaceAll("\\.", "/"); 
        HashMap<String, ArrayList<IClassDescriptor>> mClassesFound =
                new HashMap<String, ArrayList<IClassDescriptor>>();
        for (String className : superClasses) {
            mClassesFound.put(className, new ArrayList<IClassDescriptor>());
        }
        FileInputStream fis = new FileInputStream(mOsFrameworkLocation);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String entryPath = entry.getName();
            if (!entryPath.endsWith(AndroidConstants.DOT_CLASS)) {
                continue;
            }
            if (packageFilter.length() > 0 && !entryPath.startsWith(packageFilter)) {
                continue;
            }
            String className = entryPathToClassName(entryPath);
            Class<?> loaded_class = mClassCache.get(className);
            if (loaded_class == null) {
                byte[] data = mEntryCache.get(className);
                if (data == null) {    
                    long entrySize = entry.getSize();
                    if (entrySize > Integer.MAX_VALUE) {
                        throw new InvalidAttributeValueException();
                    }
                    data = readZipData(zis, (int)entrySize);
                }
                loaded_class = defineAndCacheClass(className, data);
            }
            for (Class<?> superClass = loaded_class.getSuperclass();
                    superClass != null;
                    superClass = superClass.getSuperclass()) {
                String superName = superClass.getCanonicalName();
                if (mClassesFound.containsKey(superName)) {
                    mClassesFound.get(superName).add(new ClassWrapper(loaded_class));
                    break;
                }
            }
        }
        return mClassesFound;
    }
    private String entryPathToClassName(String entryPath) {
        return entryPath.replaceFirst("\\.class$", "").replaceAll("[/\\\\]", "."); 
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Class<?> cached_class = mClassCache.get(name);
            if (cached_class == ClassNotFoundException.class) {
                throw new ClassNotFoundException(name);
            } else if (cached_class != null) {
                return cached_class;
            }
            byte[] data = loadClassData(name);
            if (data != null) {
                return defineAndCacheClass(name, data);
            } else {
                mClassCache.put(name, ClassNotFoundException.class);
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassNotFoundException(e.getMessage()); 
        }
    }
    private Class<?> defineAndCacheClass(String name, byte[] data) throws ClassFormatError {
        Class<?> cached_class;
        cached_class = defineClass(null, data, 0, data.length);
        if (cached_class != null) {
            mClassCache.put(name, cached_class);
            mEntryCache.remove(name);
        }
        return cached_class;
    }
    private synchronized byte[] loadClassData(String className)
            throws InvalidAttributeValueException, IOException {
        byte[] data = mEntryCache.get(className);
        if (data != null) {
            return data;
        }
        String entryName = className.replaceAll("\\.", "/") + AndroidConstants.DOT_CLASS; 
        FileInputStream fis = new FileInputStream(mOsFrameworkLocation);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String currEntryName = entry.getName();
            if (currEntryName.equals(entryName)) {
                long entrySize = entry.getSize();
                if (entrySize > Integer.MAX_VALUE) {
                    throw new InvalidAttributeValueException();
                }
                data = readZipData(zis, (int)entrySize);
                return data;
            }
        }
        return null;
    }
    private byte[] readZipData(ZipInputStream zis, int entrySize) throws IOException {
        int block_size = 1024;
        int data_size = entrySize < 1 ? block_size : entrySize; 
        int offset = 0;
        byte[] data = new byte[data_size];
        while(zis.available() != 0) {
            int count = zis.read(data, offset, data_size - offset);
            if (count < 0) {  
                break;
            }
            offset += count;
            if (entrySize >= 1 && offset >= entrySize) {  
                break;
            }
            if (offset >= data_size) {
                byte[] temp = new byte[data_size + block_size];
                System.arraycopy(data, 0, temp, 0, data_size);
                data_size += block_size;
                data = temp;
                block_size *= 2;
            }
        }
        if (offset < data_size) {
            byte[] temp = new byte[offset];
            if (offset > 0) {
                System.arraycopy(data, 0, temp, 0, offset);
            }
            data = temp;
        }
        return data;
    }
    public IClassDescriptor getClass(String className) throws ClassNotFoundException {
        try {
            return new ClassWrapper(loadClass(className));
        } catch (ClassNotFoundException e) {
            throw e;  
        }
    }
}
