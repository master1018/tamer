public final class TestFieldAccess extends AbstractTest {
    public static void main(String[] args) {
        test(new TestFieldAccess(5));
    }
    private int value = -1;
    private int property;
    public TestFieldAccess(int value) {
        this.property = value;
    }
    public int getValue() {
        return this.property;
    }
}
