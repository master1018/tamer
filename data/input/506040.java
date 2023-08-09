public class TestBaseClass implements Runnable {
    private final String mArg;
    public TestBaseClass() {
        throw new RuntimeException("Stub");
    }
    public TestBaseClass(String arg) {
        mArg = arg;
    }
    public String getArg() {
        return mArg;
    }
    @SuppressWarnings("unused")
    public void run() {
    }
}
