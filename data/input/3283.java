public final class TestBox {
    private static final Integer OBJECT = Integer.valueOf(-123);
    public static void main(String[] args) {
        TestEncoder.test(
                new Box(BoxLayout.LINE_AXIS),
                new Box(BoxLayout.PAGE_AXIS) {
                    @Override
                    public FlowLayout getLayout() {
                        return new FlowLayout() {
                            private final Object axis = OBJECT;
                        };
                    }
                },
                OBJECT
        );
    }
}
