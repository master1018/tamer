public class Test6792161 {
    static Constructor test(Class cls) throws Exception {
        Class[] args= { String.class };
        try {
            return cls.getConstructor(args);
        } catch (NoSuchMethodException e) {}
        return cls.getConstructor(new Class[0]);
    }
    public static void main(final String[] args) throws Exception {
        try {
            for (int i = 0; i < 100000; i++) {
                Constructor ctor = test(Class.forName("Test6792161"));
            }
        } catch (NoSuchMethodException e) {}
    }
}
