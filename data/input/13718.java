class MetalSplitPaneDivider extends BasicSplitPaneDivider
{
    private MetalBumps bumps = new MetalBumps(10, 10,
                 MetalLookAndFeel.getControlHighlight(),
                 MetalLookAndFeel.getControlDarkShadow(),
                 MetalLookAndFeel.getControl() );
    private MetalBumps focusBumps = new MetalBumps(10, 10,
                 MetalLookAndFeel.getPrimaryControlHighlight(),
                 MetalLookAndFeel.getPrimaryControlDarkShadow(),
                 UIManager.getColor("SplitPane.dividerFocusColor"));
    private int inset = 2;
    private Color controlColor = MetalLookAndFeel.getControl();
    private Color primaryControlColor = UIManager.getColor(
                                "SplitPane.dividerFocusColor");
    public MetalSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
    }
    public void paint(Graphics g) {
        MetalBumps usedBumps;
        if (splitPane.hasFocus()) {
            usedBumps = focusBumps;
            g.setColor(primaryControlColor);
        }
        else {
            usedBumps = bumps;
            g.setColor(controlColor);
        }
        Rectangle clip = g.getClipBounds();
        Insets insets = getInsets();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        Dimension  size = getSize();
        size.width -= inset * 2;
        size.height -= inset * 2;
        int drawX = inset;
        int drawY = inset;
        if (insets != null) {
            size.width -= (insets.left + insets.right);
            size.height -= (insets.top + insets.bottom);
            drawX += insets.left;
            drawY += insets.top;
        }
        usedBumps.setBumpArea(size);
        usedBumps.paintIcon(this, g, drawX, drawY);
        super.paint(g);
    }
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
            int[][]     buffer = {{0, 0, 0, 2, 2, 0, 0, 0, 0},
                                  {0, 0, 2, 1, 1, 1, 0, 0, 0},
                                  {0, 2, 1, 1, 1, 1, 1, 0, 0},
                                  {2, 1, 1, 1, 1, 1, 1, 1, 0},
                                  {0, 3, 3, 3, 3, 3, 3, 3, 3}};
            public void setBorder(Border b) {
            }
            public void paint(Graphics g) {
                JSplitPane splitPane = getSplitPaneFromSuper();
                if(splitPane != null) {
                    int         oneTouchSize = getOneTouchSizeFromSuper();
                    int         orientation = getOrientationFromSuper();
                    int         blockSize = Math.min(getDividerSize(),
                                                     oneTouchSize);
                    Color[]     colors = {
                            this.getBackground(),
                            MetalLookAndFeel.getPrimaryControlDarkShadow(),
                            MetalLookAndFeel.getPrimaryControlInfo(),
                            MetalLookAndFeel.getPrimaryControlHighlight()};
                    g.setColor(this.getBackground());
                    if (isOpaque()) {
                        g.fillRect(0, 0, this.getWidth(),
                                   this.getHeight());
                    }
                    if (getModel().isPressed()) {
                            colors[1] = colors[2];
                    }
                    if(orientation == JSplitPane.VERTICAL_SPLIT) {
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    continue;
                                            }
                                            else {
                                                g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            g.drawLine(i, j, i, j);
                                    }
                            }
                    }
                    else {
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    continue;
                                            }
                                            else {
                                                    g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            g.drawLine(j, i, j, i);
                                    }
                            }
                    }
                }
            }
            public boolean isFocusTraversable() {
                return false;
            }
        };
        b.setRequestFocusEnabled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        maybeMakeButtonOpaque(b);
        return b;
    }
    private void maybeMakeButtonOpaque(JComponent c) {
        Object opaque = UIManager.get("SplitPane.oneTouchButtonsOpaque");
        if (opaque != null) {
            c.setOpaque(((Boolean)opaque).booleanValue());
        }
    }
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
            int[][]     buffer = {{2, 2, 2, 2, 2, 2, 2, 2},
                                  {0, 1, 1, 1, 1, 1, 1, 3},
                                  {0, 0, 1, 1, 1, 1, 3, 0},
                                  {0, 0, 0, 1, 1, 3, 0, 0},
                                  {0, 0, 0, 0, 3, 0, 0, 0}};
            public void setBorder(Border border) {
            }
            public void paint(Graphics g) {
                JSplitPane splitPane = getSplitPaneFromSuper();
                if(splitPane != null) {
                    int         oneTouchSize = getOneTouchSizeFromSuper();
                    int         orientation = getOrientationFromSuper();
                    int         blockSize = Math.min(getDividerSize(),
                                                     oneTouchSize);
                    Color[]     colors = {
                            this.getBackground(),
                            MetalLookAndFeel.getPrimaryControlDarkShadow(),
                            MetalLookAndFeel.getPrimaryControlInfo(),
                            MetalLookAndFeel.getPrimaryControlHighlight()};
                    g.setColor(this.getBackground());
                    if (isOpaque()) {
                        g.fillRect(0, 0, this.getWidth(),
                                   this.getHeight());
                    }
                    if (getModel().isPressed()) {
                            colors[1] = colors[2];
                    }
                    if(orientation == JSplitPane.VERTICAL_SPLIT) {
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    continue;
                                            }
                                            else {
                                                g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            g.drawLine(i, j, i, j);
                                    }
                            }
                    }
                    else {
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    continue;
                                            }
                                            else {
                                                    g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            g.drawLine(j, i, j, i);
                                    }
                            }
                    }
                }
            }
            public boolean isFocusTraversable() {
                return false;
            }
        };
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        maybeMakeButtonOpaque(b);
        return b;
    }
    public class MetalDividerLayout implements LayoutManager {
        public void layoutContainer(Container c) {
            JButton     leftButton = getLeftButtonFromSuper();
            JButton     rightButton = getRightButtonFromSuper();
            JSplitPane  splitPane = getSplitPaneFromSuper();
            int         orientation = getOrientationFromSuper();
            int         oneTouchSize = getOneTouchSizeFromSuper();
            int         oneTouchOffset = getOneTouchOffsetFromSuper();
            Insets      insets = getInsets();
            if (leftButton != null && rightButton != null &&
                c == MetalSplitPaneDivider.this) {
                if (splitPane.isOneTouchExpandable()) {
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        int extraY = (insets != null) ? insets.top : 0;
                        int blockSize = getDividerSize();
                        if (insets != null) {
                            blockSize -= (insets.top + insets.bottom);
                        }
                        blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(oneTouchOffset, extraY,
                                             blockSize * 2, blockSize);
                        rightButton.setBounds(oneTouchOffset +
                                              oneTouchSize * 2, extraY,
                                              blockSize * 2, blockSize);
                    }
                    else {
                        int blockSize = getDividerSize();
                        int extraX = (insets != null) ? insets.left : 0;
                        if (insets != null) {
                            blockSize -= (insets.left + insets.right);
                        }
                        blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(extraX, oneTouchOffset,
                                             blockSize, blockSize * 2);
                        rightButton.setBounds(extraX, oneTouchOffset +
                                              oneTouchSize * 2, blockSize,
                                              blockSize * 2);
                    }
                }
                else {
                    leftButton.setBounds(-5, -5, 1, 1);
                    rightButton.setBounds(-5, -5, 1, 1);
                }
            }
        }
        public Dimension minimumLayoutSize(Container c) {
            return new Dimension(0,0);
        }
        public Dimension preferredLayoutSize(Container c) {
            return new Dimension(0, 0);
        }
        public void removeLayoutComponent(Component c) {}
        public void addLayoutComponent(String string, Component c) {}
    }
    int getOneTouchSizeFromSuper() {
        return super.ONE_TOUCH_SIZE;
    }
    int getOneTouchOffsetFromSuper() {
        return super.ONE_TOUCH_OFFSET;
    }
    int getOrientationFromSuper() {
        return super.orientation;
    }
    JSplitPane getSplitPaneFromSuper() {
        return super.splitPane;
    }
    JButton getLeftButtonFromSuper() {
        return super.leftButton;
    }
    JButton getRightButtonFromSuper() {
        return super.rightButton;
    }
}
