public final class javax_swing_JSplitPane extends AbstractTest<JSplitPane> {
    public static void main(String[] args) {
        new javax_swing_JSplitPane().test(true);
    }
    protected JSplitPane getObject() {
        return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    }
    protected JSplitPane getAnotherObject() {
        return new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    }
    protected void validate(JSplitPane before, JSplitPane after) {
        int orientation = after.getOrientation();
        if (orientation != before.getOrientation())
            throw new Error("Invalid orientation: " + orientation);
    }
}
