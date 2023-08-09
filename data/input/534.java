public class Test6910490 extends JApplet implements Icon {
    @Override
    public void init() {
        Insets insets = new Insets(10, 10, 10, 10);
        Dimension size = new Dimension(getWidth() / 2, getHeight());
        JSplitPane pane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                create("Color", size, new MatteBorder(insets, RED)),
                create("Icon", size, new MatteBorder(insets, this)));
        pane.setDividerLocation(size.width - pane.getDividerSize() / 2);
        add(pane);
    }
    private JScrollPane create(String name, Dimension size, MatteBorder border) {
        JButton button = new JButton(name);
        button.setPreferredSize(size);
        button.setBorder(border);
        return new JScrollPane(button);
    }
    public int getIconWidth() {
        return 10;
    }
    public int getIconHeight() {
        return 10;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(RED);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
    }
}
