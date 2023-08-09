public final class javax_swing_Box extends AbstractTest<Box> {
    public static void main(String[] args) {
        new javax_swing_Box().test(true);
    }
    protected Box getObject() {
        return new Box(BoxLayout.LINE_AXIS);
    }
    protected Box getAnotherObject() {
        return Box.createHorizontalBox();
    }
}
