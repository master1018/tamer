public class TestPersistenceDelegate {
    private static final XMLEncoder ENCODER = new XMLEncoder(System.out);
    public static void main(String[] args) throws InterruptedException {
        Class<?> type = TestPersistenceDelegate.class;
        test(type, DefaultPersistenceDelegate.class);
        ENCODER.setPersistenceDelegate(type, new BeanPersistenceDelegate());
        test(type, BeanPersistenceDelegate.class);
        ENCODER.setPersistenceDelegate(type, null);
        test(type, DefaultPersistenceDelegate.class);
        test(Bean.class, BeanPersistenceDelegate.class);
        test(BeanPersistenceDelegate.class, BeanPersistenceDelegate.class);
    }
    private static void test(Class<?> type, Class<? extends PersistenceDelegate> expected) {
        PersistenceDelegate actual = ENCODER.getPersistenceDelegate(type);
        if ((actual == null) && (expected != null)) {
            throw new Error("expected delegate is not found");
        }
        if ((actual != null) && !actual.getClass().equals(expected)) {
            throw new Error("found unexpected delegate");
        }
    }
}
