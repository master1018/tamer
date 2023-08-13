public class NormalClass {
    public NormalClass() { }
    private NormalClass(String arg1) { }
    protected NormalClass(String arg1, String arg2) throws NormalException { }
    NormalClass(String arg1, String arg2, String arg3) { }
    public static void staticMethod() { }
    public synchronized void syncMethod() { }
    public void notSyncMethod() { }
    boolean packageProtectedMethod() { return false; }
    private void privateMethod() { }
    protected String protectedMethod() { return null; }
    public void throwsMethod() throws NormalException { }
    public native void nativeMethod();
    public void notNativeMethod() { }
    public final void finalMethod() { }
    public final String FINAL_FIELD = "";
    public static String STATIC_FIELD;
    public volatile String VOLATILE_FIELD;
    public transient String TRANSIENT_FIELD;
    String PACAKGE_FIELD;
    private String PRIVATE_FIELD;
    protected String PROTECTED_FIELD;
    public class InnerClass {
        public class InnerInnerClass {
            private String innerInnerClassData;
        }
        private String innerClassData;
    }
    public interface InnerInterface {
        void doSomething();
    }
}
