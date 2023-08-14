public class InstrumentationImpl implements Instrumentation {
    private final     TransformerManager      mTransformerManager;
    private           TransformerManager      mRetransfomableTransformerManager;
    private final     long                    mNativeAgent;
    private final     boolean                 mEnvironmentSupportsRedefineClasses;
    private volatile  boolean                 mEnvironmentSupportsRetransformClassesKnown;
    private volatile  boolean                 mEnvironmentSupportsRetransformClasses;
    private final     boolean                 mEnvironmentSupportsNativeMethodPrefix;
    private
    InstrumentationImpl(long    nativeAgent,
                        boolean environmentSupportsRedefineClasses,
                        boolean environmentSupportsNativeMethodPrefix) {
        mTransformerManager                    = new TransformerManager(false);
        mRetransfomableTransformerManager      = null;
        mNativeAgent                           = nativeAgent;
        mEnvironmentSupportsRedefineClasses    = environmentSupportsRedefineClasses;
        mEnvironmentSupportsRetransformClassesKnown = false; 
        mEnvironmentSupportsRetransformClasses = false;      
        mEnvironmentSupportsNativeMethodPrefix = environmentSupportsNativeMethodPrefix;
    }
    public void
    addTransformer(ClassFileTransformer transformer) {
        addTransformer(transformer, false);
    }
    public synchronized void
    addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        if (transformer == null) {
            throw new NullPointerException("null passed as 'transformer' in addTransformer");
        }
        if (canRetransform) {
            if (!isRetransformClassesSupported()) {
                throw new UnsupportedOperationException(
                  "adding retransformable transformers is not supported in this environment");
            }
            if (mRetransfomableTransformerManager == null) {
                mRetransfomableTransformerManager = new TransformerManager(true);
            }
            mRetransfomableTransformerManager.addTransformer(transformer);
            if (mRetransfomableTransformerManager.getTransformerCount() == 1) {
                setHasRetransformableTransformers(mNativeAgent, true);
            }
        } else {
            mTransformerManager.addTransformer(transformer);
        }
    }
    public synchronized boolean
    removeTransformer(ClassFileTransformer transformer) {
        if (transformer == null) {
            throw new NullPointerException("null passed as 'transformer' in removeTransformer");
        }
        TransformerManager mgr = findTransformerManager(transformer);
        if (mgr != null) {
            mgr.removeTransformer(transformer);
            if (mgr.isRetransformable() && mgr.getTransformerCount() == 0) {
                setHasRetransformableTransformers(mNativeAgent, false);
            }
            return true;
        }
        return false;
    }
    public boolean
    isModifiableClass(Class<?> theClass) {
        if (theClass == null) {
            throw new NullPointerException(
                         "null passed as 'theClass' in isModifiableClass");
        }
        return isModifiableClass0(mNativeAgent, theClass);
    }
    public boolean
    isRetransformClassesSupported() {
        if (!mEnvironmentSupportsRetransformClassesKnown) {
            mEnvironmentSupportsRetransformClasses = isRetransformClassesSupported0(mNativeAgent);
            mEnvironmentSupportsRetransformClassesKnown = true;
        }
        return mEnvironmentSupportsRetransformClasses;
    }
    public void
    retransformClasses(Class<?>[] classes) {
        if (!isRetransformClassesSupported()) {
            throw new UnsupportedOperationException(
              "retransformClasses is not supported in this environment");
        }
        retransformClasses0(mNativeAgent, classes);
    }
    public boolean
    isRedefineClassesSupported() {
        return mEnvironmentSupportsRedefineClasses;
    }
    public void
    redefineClasses(ClassDefinition[]   definitions)
            throws  ClassNotFoundException {
        if (!isRedefineClassesSupported()) {
            throw new UnsupportedOperationException("redefineClasses is not supported in this environment");
        }
        if (definitions == null) {
            throw new NullPointerException("null passed as 'definitions' in redefineClasses");
        }
        for (int i = 0; i < definitions.length; ++i) {
            if (definitions[i] == null) {
                throw new NullPointerException("element of 'definitions' is null in redefineClasses");
            }
        }
        if (definitions.length == 0) {
            return; 
        }
        redefineClasses0(mNativeAgent, definitions);
    }
    public Class[]
    getAllLoadedClasses() {
        return getAllLoadedClasses0(mNativeAgent);
    }
    public Class[]
    getInitiatedClasses(ClassLoader loader) {
        return getInitiatedClasses0(mNativeAgent, loader);
    }
    public long
    getObjectSize(Object objectToSize) {
        if (objectToSize == null) {
            throw new NullPointerException("null passed as 'objectToSize' in getObjectSize");
        }
        return getObjectSize0(mNativeAgent, objectToSize);
    }
    public void
    appendToBootstrapClassLoaderSearch(JarFile jarfile) {
        appendToClassLoaderSearch0(mNativeAgent, jarfile.getName(), true);
    }
    public void
    appendToSystemClassLoaderSearch(JarFile jarfile) {
        appendToClassLoaderSearch0(mNativeAgent, jarfile.getName(), false);
    }
    public boolean
    isNativeMethodPrefixSupported() {
        return mEnvironmentSupportsNativeMethodPrefix;
    }
    public synchronized void
    setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        if (!isNativeMethodPrefixSupported()) {
            throw new UnsupportedOperationException(
                   "setNativeMethodPrefix is not supported in this environment");
        }
        if (transformer == null) {
            throw new NullPointerException(
                       "null passed as 'transformer' in setNativeMethodPrefix");
        }
        TransformerManager mgr = findTransformerManager(transformer);
        if (mgr == null) {
            throw new IllegalArgumentException(
                       "transformer not registered in setNativeMethodPrefix");
        }
        mgr.setNativeMethodPrefix(transformer, prefix);
        String[] prefixes = mgr.getNativeMethodPrefixes();
        setNativeMethodPrefixes(mNativeAgent, prefixes, mgr.isRetransformable());
    }
    private TransformerManager
    findTransformerManager(ClassFileTransformer transformer) {
        if (mTransformerManager.includesTransformer(transformer)) {
            return mTransformerManager;
        }
        if (mRetransfomableTransformerManager != null &&
                mRetransfomableTransformerManager.includesTransformer(transformer)) {
            return mRetransfomableTransformerManager;
        }
        return null;
    }
    private native boolean
    isModifiableClass0(long nativeAgent, Class<?> theClass);
    private native boolean
    isRetransformClassesSupported0(long nativeAgent);
    private native void
    setHasRetransformableTransformers(long nativeAgent, boolean has);
    private native void
    retransformClasses0(long nativeAgent, Class<?>[] classes);
    private native void
    redefineClasses0(long nativeAgent, ClassDefinition[]  definitions)
        throws  ClassNotFoundException;
    private native Class[]
    getAllLoadedClasses0(long nativeAgent);
    private native Class[]
    getInitiatedClasses0(long nativeAgent, ClassLoader loader);
    private native long
    getObjectSize0(long nativeAgent, Object objectToSize);
    private native void
    appendToClassLoaderSearch0(long nativeAgent, String jarfile, boolean bootLoader);
    private native void
    setNativeMethodPrefixes(long nativeAgent, String[] prefixes, boolean isRetransformable);
    static {
        System.loadLibrary("instrument");
    }
    private static void setAccessible(final AccessibleObject ao, final boolean accessible) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    ao.setAccessible(accessible);
                    return null;
                }});
    }
    private void
    loadClassAndStartAgent( String  classname,
                            String  methodname,
                            String  optionsString)
            throws Throwable {
        ClassLoader mainAppLoader   = ClassLoader.getSystemClassLoader();
        Class<?>    javaAgentClass  = mainAppLoader.loadClass(classname);
        Method m = null;
        NoSuchMethodException firstExc = null;
        boolean twoArgAgent = false;
        try {
            m = javaAgentClass.getDeclaredMethod( methodname,
                                 new Class[] {
                                     String.class,
                                     java.lang.instrument.Instrumentation.class
                                 }
                               );
            twoArgAgent = true;
        } catch (NoSuchMethodException x) {
            firstExc = x;
        }
        if (m == null) {
            try {
                m = javaAgentClass.getDeclaredMethod(methodname,
                                                 new Class[] { String.class });
            } catch (NoSuchMethodException x) {
            }
        }
        if (m == null) {
            try {
                m = javaAgentClass.getMethod( methodname,
                                 new Class[] {
                                     String.class,
                                     java.lang.instrument.Instrumentation.class
                                 }
                               );
                twoArgAgent = true;
            } catch (NoSuchMethodException x) {
            }
        }
        if (m == null) {
            try {
                m = javaAgentClass.getMethod(methodname,
                                             new Class[] { String.class });
            } catch (NoSuchMethodException x) {
                throw firstExc;
            }
        }
        setAccessible(m, true);
        if (twoArgAgent) {
            m.invoke(null, new Object[] { optionsString, this });
        } else {
            m.invoke(null, new Object[] { optionsString });
        }
        setAccessible(m, false);
    }
    private void
    loadClassAndCallPremain(    String  classname,
                                String  optionsString)
            throws Throwable {
        loadClassAndStartAgent( classname, "premain", optionsString );
    }
    private void
    loadClassAndCallAgentmain(  String  classname,
                                String  optionsString)
            throws Throwable {
        loadClassAndStartAgent( classname, "agentmain", optionsString );
    }
    private byte[]
    transform(  ClassLoader         loader,
                String              classname,
                Class               classBeingRedefined,
                ProtectionDomain    protectionDomain,
                byte[]              classfileBuffer,
                boolean             isRetransformer) {
        TransformerManager mgr = isRetransformer?
                                        mRetransfomableTransformerManager :
                                        mTransformerManager;
        if (mgr == null) {
            return null; 
        } else {
            return mgr.transform(   loader,
                                    classname,
                                    classBeingRedefined,
                                    protectionDomain,
                                    classfileBuffer);
        }
    }
}
