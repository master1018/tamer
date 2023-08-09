public class TestInnerClass {
    private final String mArg;
    private class InnerPrivClass {
    }
    public class InnerPubClass {
    }
    private static final class InnerStaticClass {
    }
    public TestInnerClass() {
        mArg = null;
    }
    public TestInnerClass(String arg) {
        mArg = arg;
    }
    public String getArg() {
        return mArg;
    }
    public InnerPubClass getInnerPubClass() {
        return new InnerPubClass();
    }
}
