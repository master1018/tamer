public class DefineClassByteBuffer {
    static void test(ClassLoader cl) throws Exception {
        Class<?> c = Class.forName("TestClass", true, cl);
        if (!"TestClass".equals(c.getName())) {
            throw new RuntimeException("Got wrong class: " + c);
        }
        if (c.getClassLoader() != cl) {
            throw new RuntimeException("TestClass defined by wrong classloader: " + c.getClassLoader());
        }
    }
    public static void main(String arg[]) throws Exception {
        DummyClassLoader[] cls = new DummyClassLoader[DummyClassLoader.MAX_TYPE];
        for (int i = 0; i < cls.length; i++) {
            cls[i] = new DummyClassLoader(i);
        }
        for (int i = 0; i < cls.length; i++) {
            test(cls[i]);
        }
    }
    public static class DummyClassLoader extends ClassLoader {
        public static final String CLASS_NAME = "TestClass";
        public static final int MAPPED_BUFFER = 0;
        public static final int DIRECT_BUFFER = 1;
        public static final int ARRAY_BUFFER = 2;
        public static final int WRAPPED_BUFFER = 3;
        public static final int READ_ONLY_ARRAY_BUFFER = 4;
        public static final int READ_ONLY_DIRECT_BUFFER = 5;
        public static final int DUP_ARRAY_BUFFER = 6;
        public static final int DUP_DIRECT_BUFFER = 7;
        public static final int MAX_TYPE = 7;
        int loaderType;
        DummyClassLoader(int loaderType) {
            this.loaderType = loaderType;
        }
        static ByteBuffer[] buffers = new ByteBuffer[MAX_TYPE + 1];
        static ByteBuffer readClassFile(String name) {
            try {
                File f = new File(System.getProperty("test.classes", "."),
                                  name);
                FileInputStream fin = new FileInputStream(f);
                FileChannel fc = fin.getChannel();
                return fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Can't open file: " + name, e);
            } catch (IOException e) {
                throw new RuntimeException("Can't open file: " + name, e);
            }
        }
        static {
            buffers[MAPPED_BUFFER] = readClassFile(CLASS_NAME + ".class");
            byte[] array = new byte[buffers[MAPPED_BUFFER].limit()];
            buffers[MAPPED_BUFFER].get(array).flip();
            buffers[DIRECT_BUFFER] = ByteBuffer.allocateDirect(array.length);
            buffers[DIRECT_BUFFER].put(array).flip();
            buffers[ARRAY_BUFFER] = ByteBuffer.allocate(array.length);
            buffers[ARRAY_BUFFER].put(array).flip();
            buffers[WRAPPED_BUFFER] = ByteBuffer.wrap(array);
            buffers[READ_ONLY_ARRAY_BUFFER] = buffers[ARRAY_BUFFER].asReadOnlyBuffer();
            buffers[READ_ONLY_DIRECT_BUFFER] = buffers[DIRECT_BUFFER].asReadOnlyBuffer();
            buffers[DUP_ARRAY_BUFFER] = buffers[ARRAY_BUFFER].duplicate();
            buffers[DUP_DIRECT_BUFFER] = buffers[DIRECT_BUFFER].duplicate();
        }
        protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
        {
            Class<?> c;
            if (!"TestClass".equals(name)) {
                c = super.loadClass(name, resolve);
            } else {
                c = findClass(name);
                if (resolve) {
                    resolveClass(c);
                }
            }
            return c;
        }
        protected Class<?> findClass(String name)
            throws ClassNotFoundException
        {
            if (!"TestClass".equals(name)) {
                throw new ClassNotFoundException("Unexpected class: " + name);
            }
            return defineClass(name, buffers[loaderType], null);
        }
    } 
} 
