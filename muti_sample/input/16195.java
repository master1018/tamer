public class Test4968523 {
    public static void main(String[] args) {
        String[] names = {"time"};
        test(Date.class, new DefaultPersistenceDelegate(names));
        test(null, new DefaultPersistenceDelegate());
    }
    private static void test(Class<?> type, PersistenceDelegate pd) {
        Encoder encoder1 = new Encoder();
        Encoder encoder2 = new XMLEncoder(System.out);
        PersistenceDelegate pd1 = encoder1.getPersistenceDelegate(type);
        PersistenceDelegate pd2 = encoder2.getPersistenceDelegate(type);
        encoder1.setPersistenceDelegate(type, pd);
        if (pd1 == encoder1.getPersistenceDelegate(type))
            throw new Error("first persistence delegate is not changed");
        if (pd2 != encoder2.getPersistenceDelegate(type))
            throw new Error("second persistence delegate is changed");
    }
}
