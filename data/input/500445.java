public class TestAssertions {
    boolean isTrue = false;   
    TestAssertions() {
    }
    public void test() throws AssertionError {
        assert (isTrue == true);
    }
}
