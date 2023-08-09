public class Test5102804 {
    private static final String BEAN_NAME = "Test5102804$Example";
    private static final String BEAN_INFO_NAME = BEAN_NAME + "BeanInfo";
    public static void main(String[] args) {
        if (!isCollectible(getReference()))
            throw new Error("Reference is not collected");
    }
    private static Reference getReference() {
        try {
            ClassLoader loader = new Loader();
            Class type = Class.forName(BEAN_NAME, true, loader);
            if (!type.getClassLoader().equals(loader)) {
                throw new Error("Wrong class loader");
            }
            BeanInfo info = Introspector.getBeanInfo(type);
            if (0 != info.getDefaultPropertyIndex()) {
                throw new Error("Wrong bean info found");
            }
            return new WeakReference<Class>(type);
        }
        catch (IntrospectionException exception) {
            throw new Error("Introspection Error", exception);
        }
        catch (ClassNotFoundException exception) {
            throw new Error("Class Not Found", exception);
        }
    }
    private static boolean isCollectible(Reference reference) {
        int[] array = new int[10];
        while (true) {
            try {
                array = new int[array.length + array.length / 3];
            }
            catch (OutOfMemoryError error) {
                return null == reference.get();
            }
        }
    }
    private static final class Loader extends URLClassLoader {
        Loader() {
            super(new URL[] {
                    Test5102804.class.getProtectionDomain().getCodeSource().getLocation()
            });
        }
        @Override
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c == null) {
                if (BEAN_NAME.equals(name) || BEAN_INFO_NAME.equals(name)) {
                    c = findClass(name);
                }
                else try {
                    c = getParent().loadClass(name);
                }
                catch (ClassNotFoundException exception) {
                    c = findClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
    public static final class Example {
        private int value;
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
    }
    public static final class ExampleBeanInfo extends SimpleBeanInfo {
        @Override
        public int getDefaultPropertyIndex() {
            return 0;
        }
        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return new PropertyDescriptor[] {
                        new PropertyDescriptor("value", Class.forName(BEAN_NAME))
                };
            }
            catch (ClassNotFoundException exception) {
                return null;
            }
            catch (IntrospectionException exception) {
                return null;
            }
        }
    }
}
