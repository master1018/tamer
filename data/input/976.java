public class Test6660539 implements Runnable {
    private static final String NAME = "$$$";
    public static void main(String[] args) throws Exception {
        for (PropertyDescriptor pd : getPropertyDescriptors()) {
            pd.setDisplayName(NAME);
        }
        ThreadGroup group = new ThreadGroup(NAME);
        Thread thread = new Thread(group, new Test6660539());
        thread.start();
        thread.join();
    }
    public void run() {
        SunToolkit.createNewAppContext();
        for (PropertyDescriptor pd : getPropertyDescriptors()) {
            if (pd.getDisplayName().equals(NAME))
                throw new Error("shared BeanInfo cache");
        }
    }
    private static PropertyDescriptor[] getPropertyDescriptors() {
        try {
            BeanInfo info = Introspector.getBeanInfo(Test6660539.class);
            return info.getPropertyDescriptors();
        }
        catch (IntrospectionException exception) {
            throw new Error("unexpected", exception);
        }
    }
}
