public class MotifOptionPaneUI extends BasicOptionPaneUI
{
    public static ComponentUI createUI(JComponent x) {
        return new MotifOptionPaneUI();
    }
    protected Container createButtonArea() {
        Container          b = super.createButtonArea();
        if(b != null && b.getLayout() instanceof ButtonAreaLayout) {
            ((ButtonAreaLayout)b.getLayout()).setCentersChildren(false);
        }
        return b;
    }
    public Dimension getMinimumOptionPaneSize() {
        return null;
    }
    protected Container createSeparator() {
        return new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(10, 2);
            }
            public void paint(Graphics g) {
                int width = getWidth();
                g.setColor(Color.darkGray);
                g.drawLine(0, 0, width, 0);
                g.setColor(Color.white);
                g.drawLine(0, 1, width, 1);
            }
        };
    }
    protected void addIcon(Container top) {
        Icon                  sideIcon = getIcon();
        if (sideIcon != null) {
            JLabel            iconLabel = new JLabel(sideIcon);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            top.add(iconLabel, "West");
        }
    }
}
