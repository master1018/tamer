public final class javax_swing_JLayeredPane extends AbstractTest<JLayeredPane> {
    public static void main(String[] args) {
        new javax_swing_JLayeredPane().test(true);
    }
    private static void init(JLayeredPane pane, int layer, int x, int y, int w, int h, Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setLocation(x, y);
        panel.setSize(w, h);
        pane.add(panel, new Integer(layer));
    }
    protected JLayeredPane getObject() {
        JLayeredPane pane = new JLayeredPane();
        init(pane, 0, 25, 25, 50, 50, Color.RED);
        init(pane, 1, 10, 10, 50, 50, Color.BLUE);
        init(pane, 2, 40, 40, 50, 50, Color.YELLOW);
        pane.setSize(200, 200);
        return pane;
    }
    protected JLayeredPane getAnotherObject() {
        return null; 
    }
}
