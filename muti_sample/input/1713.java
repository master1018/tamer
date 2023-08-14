public final class TestStackOverflow extends AbstractTest {
    public static void main(String[] args) {
        test(new TestStackOverflow(5));
    }
    private int value = -1;
    private int property;
    public TestStackOverflow(int value) {
        this.property = value;
    }
    public int getValue() {
        return this.property;
    }
    public boolean equals(Object object) {
        if (object instanceof TestStackOverflow) {
            TestStackOverflow test = (TestStackOverflow) object;
            return test.property == this.property;
        }
        return false;
    }
}
