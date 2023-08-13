class TestProtected {
    static protected int testP() {
        return 888;
    }
}
public class T_invoke_static_18  {
    public int run() {
        return TestProtected.testP();
    }
}