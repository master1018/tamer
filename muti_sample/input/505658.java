public class ITestImpl implements ITest {
    public void doit() {
    }
    public void doit(int i) {
    }
    public native void doitNative();
    public int test(int a) {
        if (a == 999) return 195;
        return 0;
    }
    public int testArgsOrder(int a, int b) {
        return a / b;
    }
}