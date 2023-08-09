public abstract class Pack200 {
    private static final String SYSTEM_PROPERTY_PACKER = "java.util.jar.Pack200.Packer"; 
    private static final String SYSTEM_PROPERTY_UNPACKER = "java.util.jar.Pack200.Unpacker"; 
    private Pack200() {
    }
    public static Pack200.Packer newPacker() {
        return (Packer) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        String className = System
                                .getProperty(SYSTEM_PROPERTY_PACKER,
                                        "org.apache.harmony.pack200.Pack200PackerAdapter"); 
                        try {
                            return ClassLoader.getSystemClassLoader()
                                    .loadClass(className).newInstance();
                        } catch (Exception e) {
                            throw new Error("Can't load class " + className, e);
                        }
                    }
                });
    }
    public static Pack200.Unpacker newUnpacker() {
        return (Unpacker) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        String className = System
                                .getProperty(SYSTEM_PROPERTY_UNPACKER,
                                        "org.apache.harmony.unpack200.Pack200UnpackerAdapter");
                        try {
                            return ClassLoader.getSystemClassLoader()
                                    .loadClass(className).newInstance();
                        } catch (Exception e) {
                            throw new Error("Can't load class " + className, e);
                        }
                    }
                });
    }
    public static interface Packer {
        static final String CLASS_ATTRIBUTE_PFX = "pack.class.attribute."; 
        static final String CODE_ATTRIBUTE_PFX = "pack.code.attribute."; 
        static final String DEFLATE_HINT = "pack.deflate.hint";
        static final String EFFORT = "pack.effort";
        static final String ERROR = "error";
        static final String FALSE = "false";
        static final String FIELD_ATTRIBUTE_PFX = "pack.field.attribute.";
        static final String KEEP = "keep";
        static final String KEEP_FILE_ORDER = "pack.keep.file.order";
        static final String LATEST = "latest";
        static final String METHOD_ATTRIBUTE_PFX = "pack.method.attribute.";
        static final String MODIFICATION_TIME = "pack.modification.time";
        static final String PASS = "pass";
        static final String PASS_FILE_PFX = "pack.pass.file.";
        static final String PROGRESS = "pack.progress";
        static final String SEGMENT_LIMIT = "pack.segment.limit";
        static final String STRIP = "strip";
        static final String TRUE = "true";
        static final String UNKNOWN_ATTRIBUTE = "pack.unknown.attribute";
        SortedMap<String, String> properties();
        void pack(JarFile in, OutputStream out) throws IOException;
        void pack(JarInputStream in, OutputStream out) throws IOException;
        void addPropertyChangeListener(PropertyChangeListener listener);
        void removePropertyChangeListener(PropertyChangeListener listener);
    }
    public static interface Unpacker {
        static final String DEFLATE_HINT = "unpack.deflate.hint";
        static final String FALSE = "false";
        static final String KEEP = "keep";
        static final String PROGRESS = "unpack.progress";
        static final String TRUE = "true";
        SortedMap<String, String> properties();
        void unpack(InputStream in, JarOutputStream out) throws IOException;
        void unpack(File in, JarOutputStream out) throws IOException;
        void addPropertyChangeListener(PropertyChangeListener listener);
        void removePropertyChangeListener(PropertyChangeListener listener);
    }
}
