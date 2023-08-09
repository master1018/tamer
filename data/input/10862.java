public class Test6526631 {
    private static final int COLS = 90;
    private static final int ROWS = 50;
    private static final int OFFSET = 10;
    public static void main(String[] args) throws Throwable {
        SwingTest.start(Test6526631.class);
    }
    private final JScrollPane pane;
    private final JFrame frame;
    public Test6526631(JFrame frame) {
        this.pane = new JScrollPane(new JTextArea(ROWS, COLS));
        this.pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        this.frame = frame;
        this.frame.add(this.pane);
    }
    private void update(int offset) {
        Dimension size = this.frame.getSize();
        size.width += offset;
        this.frame.setSize(size);
    }
    public void validateFirst() {
        validateThird();
        update(OFFSET);
    }
    public void validateSecond() {
        validateThird();
        update(-OFFSET);
    }
    public void validateThird() {
        JViewport viewport = this.pane.getViewport();
        JScrollBar scroller = this.pane.getHorizontalScrollBar();
        if (!scroller.getComponentOrientation().equals(ComponentOrientation.RIGHT_TO_LEFT)) {
            throw new Error("unexpected component orientation");
        }
        int value = scroller.getValue();
        if (value != 0) {
            throw new Error("unexpected scroll value");
        }
        int extent = viewport.getExtentSize().width;
        if (extent != scroller.getVisibleAmount()) {
            throw new Error("unexpected visible amount");
        }
        int size = viewport.getViewSize().width;
        if (size != scroller.getMaximum()) {
            throw new Error("unexpected maximum");
        }
        int pos = size - extent - value;
        if (pos != viewport.getViewPosition().x) {
            throw new Error("unexpected position");
        }
    }
}
