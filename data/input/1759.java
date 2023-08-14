public class Test6802868 {
    public static void main(String[] args) throws Throwable {
        SwingTest.start(Test6802868.class);
    }
    private final JFrame frame;
    private final JInternalFrame internal;
    private Dimension size;
    private Point location;
    public Test6802868(JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        this.frame = frame;
        this.frame.add(desktop);
        this.internal = new JInternalFrame(getClass().getName(), true, true, true, true);
        this.internal.setVisible(true);
        desktop.add(this.internal);
    }
    public void firstAction() throws PropertyVetoException {
        this.internal.setMaximum(true);
    }
    public void firstTest() {
        this.size = this.internal.getSize();
        resizeFrame();
    }
    public void firstValidation() {
        if (this.internal.getSize().equals(this.size)) {
            throw new Error("InternalFrame hasn't changed its size");
        }
    }
    public void secondAction() throws PropertyVetoException {
        this.internal.setIcon(true);
    }
    public void secondTest() {
        this.location = this.internal.getDesktopIcon().getLocation();
        resizeFrame();
    }
    public void secondValidation() {
        if (this.internal.getDesktopIcon().getLocation().equals(this.location)) {
            throw new Error("JDesktopIcon hasn't moved");
        }
    }
    private void resizeFrame() {
        Dimension size = this.frame.getSize();
        size.width += 10;
        size.height += 10;
        this.frame.setSize(size);
    }
}
