public class AsmGenerator {
    private final Log mLog;
    private final String mOsDestJar;
    private final Class<?>[] mInjectClasses;
    private final Set<String> mStubMethods;
    private Map<String, ClassReader> mKeep;
    private Map<String, ClassReader> mDeps;
    private int mRenameCount;
    private final HashMap<String, String> mRenameClasses;
    private HashSet<String> mClassesNotRenamed;
    private HashMap<String, Set<String>> mDeleteReturns;
    public AsmGenerator(Log log, String osDestJar,
            Class<?>[] injectClasses,
            String[] stubMethods,
            String[] renameClasses, String[] deleteReturns) {
        mLog = log;
        mOsDestJar = osDestJar;
        mInjectClasses = injectClasses != null ? injectClasses : new Class<?>[0];
        mStubMethods = stubMethods != null ? new HashSet<String>(Arrays.asList(stubMethods)) :
                                             new HashSet<String>();
        mRenameClasses = new HashMap<String, String>();
        mClassesNotRenamed = new HashSet<String>();
        int n = renameClasses == null ? 0 : renameClasses.length;
        for (int i = 0; i < n; i += 2) {
            assert i + 1 < n;
            String oldFqcn = binaryToInternalClassName(renameClasses[i]);
            String newFqcn = binaryToInternalClassName(renameClasses[i + 1]);
            mRenameClasses.put(oldFqcn, newFqcn);
            mClassesNotRenamed.add(oldFqcn);
        }
        mDeleteReturns = new HashMap<String, Set<String>>();
        if (deleteReturns != null) {
            Set<String> returnTypes = null;
            String renamedClass = null;
            for (String className : deleteReturns) {
                if (className == null) {
                    if (returnTypes != null) {
                        mDeleteReturns.put(renamedClass, returnTypes);
                    }
                    renamedClass = null;
                    continue;
                }
                if (renamedClass == null) {
                    renamedClass = binaryToInternalClassName(className);
                    continue;
                }
                if (returnTypes == null) {
                    returnTypes = new HashSet<String>();
                }
                returnTypes.add(binaryToInternalClassName(className));
            }
        }
    }
    public Set<String> getClassesNotRenamed() {
        return mClassesNotRenamed;
    }
    String binaryToInternalClassName(String className) {
        if (className == null) {
            return null;
        } else {
            return className.replace('.', '/');
        }
    }
    public void setKeep(Map<String, ClassReader> keep) {
        mKeep = keep;
    }
    public void setDeps(Map<String, ClassReader> deps) {
        mDeps = deps;
    }
    public Map<String, ClassReader> getKeep() {
        return mKeep;
    }
    public Map<String, ClassReader> getDeps() {
        return mDeps;
    }
    public void generate() throws FileNotFoundException, IOException {
        TreeMap<String, byte[]> all = new TreeMap<String, byte[]>();
        for (Class<?> clazz : mInjectClasses) {
            String name = classToEntryPath(clazz);
            InputStream is = ClassLoader.getSystemResourceAsStream(name);
            ClassReader cr = new ClassReader(is);
            byte[] b = transform(cr, true );
            name = classNameToEntryPath(transformName(cr.getClassName()));
            all.put(name, b);
        }
        for (Entry<String, ClassReader> entry : mDeps.entrySet()) {
            ClassReader cr = entry.getValue();
            byte[] b = transform(cr, true );
            String name = classNameToEntryPath(transformName(cr.getClassName()));
            all.put(name, b);
        }
        for (Entry<String, ClassReader> entry : mKeep.entrySet()) {
            ClassReader cr = entry.getValue();
            byte[] b = transform(cr, true );
            String name = classNameToEntryPath(transformName(cr.getClassName()));
            all.put(name, b);
        }
        mLog.info("# deps classes: %d", mDeps.size());
        mLog.info("# keep classes: %d", mKeep.size());
        mLog.info("# renamed     : %d", mRenameCount);
        createJar(new FileOutputStream(mOsDestJar), all);
        mLog.info("Created JAR file %s", mOsDestJar);
    }
    void createJar(FileOutputStream outStream, Map<String,byte[]> all) throws IOException {
        JarOutputStream jar = new JarOutputStream(outStream);
        for (Entry<String, byte[]> entry : all.entrySet()) {
            String name = entry.getKey();
            JarEntry jar_entry = new JarEntry(name);
            jar.putNextEntry(jar_entry);
            jar.write(entry.getValue());
            jar.closeEntry();
        }
        jar.flush();
        jar.close();
    }
    String classNameToEntryPath(String className) {
        return className.replaceAll("\\.", "/").concat(".class");
    }
    private String classToEntryPath(Class<?> clazz) {
        String name = "";
        Class<?> parent;
        while ((parent = clazz.getEnclosingClass()) != null) {
            name = "$" + clazz.getSimpleName() + name;
            clazz = parent;
        }
        return classNameToEntryPath(clazz.getCanonicalName() + name);        
    }
    byte[] transform(ClassReader cr, boolean stubNativesOnly) {
        boolean hasNativeMethods = hasNativeMethods(cr);
        String className = cr.getClassName();
        String newName = transformName(className);
        if (newName != className) {
            mRenameCount++;
            mClassesNotRenamed.remove(className);
        }
        mLog.debug("Transform %s%s%s%s", className,
                newName == className ? "" : " (renamed to " + newName + ")",
                hasNativeMethods ? " -- has natives" : "",
                stubNativesOnly ? " -- stub natives only" : "");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor rv = cw;
        if (newName != className) {
            rv = new RenameClassAdapter(cw, className, newName);
        }
        TransformClassAdapter cv = new TransformClassAdapter(mLog, mStubMethods, 
                mDeleteReturns.get(className),
                newName, rv,
                stubNativesOnly, stubNativesOnly || hasNativeMethods);
        cr.accept(cv, 0 );
        return cw.toByteArray();
    }
    String transformName(String className) {
        String newName = mRenameClasses.get(className);
        if (newName != null) {
            return newName;
        }
        int pos = className.indexOf('$');
        if (pos > 0) {
            String base = className.substring(0, pos);
            newName = mRenameClasses.get(base);
            if (newName != null) {
                return newName + className.substring(pos);
            }
        }
        return className;
    }
    boolean hasNativeMethods(ClassReader cr) {
        ClassHasNativeVisitor cv = new ClassHasNativeVisitor();
        cr.accept(cv, 0 );
        return cv.hasNativeMethods();
    }
}
