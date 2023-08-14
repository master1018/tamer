public class Test6625450 {
    public static void main(String[] args) {
        test(false, 0, Integer.MAX_VALUE);
        test(false, 0, 1);
        test(true, 0, -1);
        test(true, 0, Integer.MIN_VALUE);
        test(false, Integer.MAX_VALUE, 0);
        test(false, 1, 0);
        test(true, -1, 0);
        test(true, Integer.MIN_VALUE, 0);
    }
    private static final TitledBorder BORDER = new TitledBorder("123");
    private static final Component COMPONENT = new Component() {
    };
    private static void test(boolean expected, int width, int height) {
        try {
            BORDER.getBaseline(COMPONENT, width, height);
            if (expected) {
                throw new Error("expected IllegalArgumentException");
            }
        }
        catch (IllegalArgumentException exception) {
            if (!expected) {
                throw new Error("unexpected exception", exception);
            }
        }
    }
}
