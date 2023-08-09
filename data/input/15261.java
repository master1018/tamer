public class TestBeanInfo implements Runnable {
    private static final String[] SEARCH_PATH = { "infos" }; 
    public static void main(String[] args) throws InterruptedException {
        TestBeanInfo test = new TestBeanInfo();
        test.run();
        ThreadGroup group = new ThreadGroup("$$$"); 
        Thread thread = new Thread(group, test);
        thread.start();
        thread.join();
    }
    private static void test(Class<?> type, Class<? extends BeanInfo> expected) {
        BeanInfo actual;
        try {
            actual = Introspector.getBeanInfo(type);
            type = actual.getClass();
            Field field = type.getDeclaredField("targetBeanInfoRef"); 
            field.setAccessible(true);
            Reference ref = (Reference) field.get(actual);
            actual = (BeanInfo) ref.get();
        }
        catch (Exception exception) {
            throw new Error("unexpected error", exception);
        }
        if ((actual == null) && (expected != null)) {
            throw new Error("expected info is not found");
        }
        if ((actual != null) && !actual.getClass().equals(expected)) {
            throw new Error("found unexpected info");
        }
    }
    private boolean passed;
    public void run() {
        if (this.passed) {
            SunToolkit.createNewAppContext();
        }
        Introspector.flushCaches();
        test(FirstBean.class, FirstBeanBeanInfo.class);
        test(SecondBean.class, null);
        test(ThirdBean.class, null);
        test(ThirdBeanBeanInfo.class, ThirdBeanBeanInfo.class);
        Introspector.setBeanInfoSearchPath(SEARCH_PATH);
        Introspector.flushCaches();
        test(FirstBean.class, FirstBeanBeanInfo.class);
        test(SecondBean.class, SecondBeanBeanInfo.class);
        test(ThirdBean.class, null);
        test(ThirdBeanBeanInfo.class, ThirdBeanBeanInfo.class);
        this.passed = true;
    }
}
