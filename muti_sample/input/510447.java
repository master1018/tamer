public class TestAssertions1 {
    boolean isTrue = true;   
    TestAssertions1() {
    }
    public void test() throws AssertionError {
        assert (isTrue == false);
    }
}
