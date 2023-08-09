class SynthSplitPaneDivider extends BasicSplitPaneDivider {
    public SynthSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
    }
    protected void setMouseOver(boolean mouseOver) {
        if (isMouseOver() != mouseOver) {
            repaint();
        }
        super.setMouseOver(mouseOver);
    }
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getSource() == splitPane) {
            if (e.getPropertyName() == JSplitPane.ORIENTATION_PROPERTY) {
                if (leftButton instanceof SynthArrowButton) {
                    ((SynthArrowButton)leftButton).setDirection(
                                       mapDirection(true));
                }
                if (rightButton instanceof SynthArrowButton) {
                    ((SynthArrowButton)rightButton).setDirection(
                                       mapDirection(false));
                }
            }
        }
    }
    public void paint(Graphics g) {
        Graphics g2 = g.create();
        SynthContext context = ((SynthSplitPaneUI)splitPaneUI).getContext(
                               splitPane, Region.SPLIT_PANE_DIVIDER);
        Rectangle bounds = getBounds();
        bounds.x = bounds.y = 0;
        SynthLookAndFeel.updateSubregion(context, g, bounds);
        context.getPainter().paintSplitPaneDividerBackground(context,
                          g, 0, 0, bounds.width, bounds.height,
                          splitPane.getOrientation());
        SynthPainter foreground = null;
        context.getPainter().paintSplitPaneDividerForeground(context, g, 0, 0,
                getWidth(), getHeight(), splitPane.getOrientation());
        context.dispose();
        for (int counter = 0; counter < getComponentCount(); counter++) {
            Component child = getComponent(counter);
            Rectangle childBounds = child.getBounds();
            Graphics childG = g.create(childBounds.x, childBounds.y,
                                       childBounds.width, childBounds.height);
            child.paint(childG);
            childG.dispose();
        }
        g2.dispose();
    }
    private int mapDirection(boolean isLeft) {
        if (isLeft) {
            if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT){
                return SwingConstants.WEST;
            }
            return SwingConstants.NORTH;
        }
        if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT){
            return SwingConstants.EAST;
        }
        return SwingConstants.SOUTH;
    }
    protected JButton createLeftOneTouchButton() {
        SynthArrowButton b = new SynthArrowButton(SwingConstants.NORTH);
        int oneTouchSize = lookupOneTouchSize();
        b.setName("SplitPaneDivider.leftOneTouchButton");
        b.setMinimumSize(new Dimension(oneTouchSize, oneTouchSize));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        b.setDirection(mapDirection(true));
        return b;
    }
    private int lookupOneTouchSize() {
        return DefaultLookup.getInt(splitPaneUI.getSplitPane(), splitPaneUI,
              "SplitPaneDivider.oneTouchButtonSize", ONE_TOUCH_SIZE);
    }
    protected JButton createRightOneTouchButton() {
        SynthArrowButton b = new SynthArrowButton(SwingConstants.NORTH);
        int oneTouchSize = lookupOneTouchSize();
        b.setName("SplitPaneDivider.rightOneTouchButton");
        b.setMinimumSize(new Dimension(oneTouchSize, oneTouchSize));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        b.setDirection(mapDirection(false));
        return b;
    }
}
