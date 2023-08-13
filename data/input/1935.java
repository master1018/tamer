public class Test4252164 extends JApplet implements MouseWheelListener {
    private int thickness;
    private JLabel rounded;
    private JLabel straight;
    public void mouseWheelMoved(MouseWheelEvent event) {
        update(event.getWheelRotation());
    }
    public void init() {
        add(createUI());
        addMouseWheelListener(this);
    }
    private JPanel createUI() {
        this.rounded = new JLabel("ROUNDED"); 
        this.straight = new JLabel("STRAIGHT"); 
        JPanel panel = new JPanel();
        panel.add(this.rounded);
        panel.add(this.straight);
        update(10);
        return panel;
    }
    private void update(int thickness) {
        this.thickness += thickness;
        this.rounded.setBorder(new LineBorder(Color.RED, this.thickness, true));
        this.straight.setBorder(new LineBorder(Color.RED, this.thickness, false));
    }
}
