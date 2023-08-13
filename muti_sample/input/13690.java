public class Test4508780 implements Runnable {
    private static final Class[] COMPILE = {
            Bean.class,
            Bean2.class,
            Bean3.class,
            Bean4.class,
    };
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) test();
    }
    private static void test() {
        test("Bean", "Bean2", "Bean3", "Bean4");
        test("Bean4", "Bean", "Bean2", "Bean3");
        test("Bean3", "Bean4", "Bean", "Bean2");
        test("Bean2", "Bean3", "Bean4", "Bean");
        Introspector.flushCaches();
    }
    private static void test(String... names) {
        new Thread(new Test4508780(names)).start();
    }
    private final ClassLoader loader = new SimpleClassLoader();
    private final String[] names;
    private Test4508780(String... names) {
        this.names = names;
    }
    public void run() {
        for (String name : this.names) {
            Object bean;
            try {
                bean = this.loader.loadClass(name).newInstance();
            } catch (Exception exception) {
                throw new Error("could not instantiate bean: " + name, exception);
            }
            if (this.loader != bean.getClass().getClassLoader()) {
                throw new Error("bean class loader is not equal to default one");
            }
            PropertyDescriptor[] pds = getPropertyDescriptors(bean);
            for (PropertyDescriptor pd : pds) {
                Class type = pd.getPropertyType();
                Method setter = pd.getWriteMethod();
                Method getter = pd.getReadMethod();
                if (type.equals(String.class)) {
                    executeMethod(setter, bean, "Foo");
                } else if (type.equals(int.class)) {
                    executeMethod(setter, bean, Integer.valueOf(1));
                }
                executeMethod(getter, bean);
            }
        }
    }
    private static void executeMethod(Method method, Object bean, Object... args) {
        if (method == null) {
            throw new Error("method is null");
        }
        if (bean == null) {
            throw new Error("target bean is null");
        }
        try {
            method.invoke(bean, args);
        } catch (Exception exception) {
            throw new Error("could not execute method: " + method, exception);
        }
    }
    private static PropertyDescriptor[] getPropertyDescriptors(Object object) {
        Class type = object.getClass();
        synchronized (System.out) {
            System.out.println(type);
            ClassLoader loader = type.getClassLoader();
            while (loader != null) {
                System.out.println(" - loader: " + loader);
                loader = loader.getParent();
            }
        }
        try {
            return Introspector.getBeanInfo(type).getPropertyDescriptors();
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}
