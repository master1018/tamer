public class BasicSplitPaneDivider extends Container
    implements PropertyChangeListener
{
    protected static final int ONE_TOUCH_SIZE = 6;
    protected static final int ONE_TOUCH_OFFSET = 2;
    protected DragController dragger;
    protected BasicSplitPaneUI splitPaneUI;
    protected int dividerSize = 0; 
    protected Component hiddenDivider;
    protected JSplitPane splitPane;
    protected MouseHandler mouseHandler;
    protected int orientation;
    protected JButton leftButton;
    protected JButton rightButton;
    private Border border;
    private boolean mouseOver;
    private int oneTouchSize;
    private int oneTouchOffset;
    private boolean centerOneTouchButtons;
    public BasicSplitPaneDivider(BasicSplitPaneUI ui) {
        oneTouchSize = DefaultLookup.getInt(ui.getSplitPane(), ui,
                "SplitPane.oneTouchButtonSize", ONE_TOUCH_SIZE);
        oneTouchOffset = DefaultLookup.getInt(ui.getSplitPane(), ui,
                "SplitPane.oneTouchButtonOffset", ONE_TOUCH_OFFSET);
        centerOneTouchButtons = DefaultLookup.getBoolean(ui.getSplitPane(),
                 ui, "SplitPane.centerOneTouchButtons", true);
        setLayout(new DividerLayout());
        setBasicSplitPaneUI(ui);
        orientation = splitPane.getOrientation();
        setCursor((orientation == JSplitPane.HORIZONTAL_SPLIT) ?
                  Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) :
                  Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        setBackground(UIManager.getColor("SplitPane.background"));
    }
    private void revalidateSplitPane() {
        invalidate();
        if (splitPane != null) {
            splitPane.revalidate();
        }
    }
    public void setBasicSplitPaneUI(BasicSplitPaneUI newUI) {
        if (splitPane != null) {
            splitPane.removePropertyChangeListener(this);
           if (mouseHandler != null) {
               splitPane.removeMouseListener(mouseHandler);
               splitPane.removeMouseMotionListener(mouseHandler);
               removeMouseListener(mouseHandler);
               removeMouseMotionListener(mouseHandler);
               mouseHandler = null;
           }
        }
        splitPaneUI = newUI;
        if (newUI != null) {
            splitPane = newUI.getSplitPane();
            if (splitPane != null) {
                if (mouseHandler == null) mouseHandler = new MouseHandler();
                splitPane.addMouseListener(mouseHandler);
                splitPane.addMouseMotionListener(mouseHandler);
                addMouseListener(mouseHandler);
                addMouseMotionListener(mouseHandler);
                splitPane.addPropertyChangeListener(this);
                if (splitPane.isOneTouchExpandable()) {
                    oneTouchExpandableChanged();
                }
            }
        }
        else {
            splitPane = null;
        }
    }
    public BasicSplitPaneUI getBasicSplitPaneUI() {
        return splitPaneUI;
    }
    public void setDividerSize(int newSize) {
        dividerSize = newSize;
    }
    public int getDividerSize() {
        return dividerSize;
    }
    public void setBorder(Border border) {
        Border         oldBorder = this.border;
        this.border = border;
    }
    public Border getBorder() {
        return border;
    }
    public Insets getInsets() {
        Border    border = getBorder();
        if (border != null) {
            return border.getBorderInsets(this);
        }
        return super.getInsets();
    }
    protected void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }
    public boolean isMouseOver() {
        return mouseOver;
    }
    public Dimension getPreferredSize() {
        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
            return new Dimension(getDividerSize(), 1);
        }
        return new Dimension(1, getDividerSize());
    }
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() == splitPane) {
            if (e.getPropertyName() == JSplitPane.ORIENTATION_PROPERTY) {
                orientation = splitPane.getOrientation();
                setCursor((orientation == JSplitPane.HORIZONTAL_SPLIT) ?
                          Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) :
                          Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                revalidateSplitPane();
            }
            else if (e.getPropertyName() == JSplitPane.
                      ONE_TOUCH_EXPANDABLE_PROPERTY) {
                oneTouchExpandableChanged();
            }
        }
    }
    public void paint(Graphics g) {
      super.paint(g);
      Border   border = getBorder();
      if (border != null) {
          Dimension     size = getSize();
          border.paintBorder(this, g, 0, 0, size.width, size.height);
      }
    }
    protected void oneTouchExpandableChanged() {
        if (!DefaultLookup.getBoolean(splitPane, splitPaneUI,
                           "SplitPane.supportsOneTouchButtons", true)) {
            return;
        }
        if (splitPane.isOneTouchExpandable() &&
            leftButton == null &&
            rightButton == null) {
            leftButton = createLeftOneTouchButton();
            if (leftButton != null)
                leftButton.addActionListener(new OneTouchActionHandler(true));
            rightButton = createRightOneTouchButton();
            if (rightButton != null)
                rightButton.addActionListener(new OneTouchActionHandler
                    (false));
            if (leftButton != null && rightButton != null) {
                add(leftButton);
                add(rightButton);
            }
        }
        revalidateSplitPane();
    }
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
            public void setBorder(Border b) {
            }
            public void paint(Graphics g) {
                if (splitPane != null) {
                    int[]   xs = new int[3];
                    int[]   ys = new int[3];
                    int     blockSize;
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(),
                               this.getHeight());
                    g.setColor(Color.black);
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        blockSize = Math.min(getHeight(), oneTouchSize);
                        xs[0] = blockSize;
                        xs[1] = 0;
                        xs[2] = blockSize << 1;
                        ys[0] = 0;
                        ys[1] = ys[2] = blockSize;
                        g.drawPolygon(xs, ys, 3); 
                    }
                    else {
                        blockSize = Math.min(getWidth(), oneTouchSize);
                        xs[0] = xs[2] = blockSize;
                        xs[1] = 0;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                    }
                    g.fillPolygon(xs, ys, 3);
                }
            }
            public boolean isFocusTraversable() {
                return false;
            }
        };
        b.setMinimumSize(new Dimension(oneTouchSize, oneTouchSize));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        return b;
    }
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
            public void setBorder(Border border) {
            }
            public void paint(Graphics g) {
                if (splitPane != null) {
                    int[]          xs = new int[3];
                    int[]          ys = new int[3];
                    int            blockSize;
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(),
                               this.getHeight());
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        blockSize = Math.min(getHeight(), oneTouchSize);
                        xs[0] = blockSize;
                        xs[1] = blockSize << 1;
                        xs[2] = 0;
                        ys[0] = blockSize;
                        ys[1] = ys[2] = 0;
                    }
                    else {
                        blockSize = Math.min(getWidth(), oneTouchSize);
                        xs[0] = xs[2] = 0;
                        xs[1] = blockSize;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                    }
                    g.setColor(Color.black);
                    g.fillPolygon(xs, ys, 3);
                }
            }
            public boolean isFocusTraversable() {
                return false;
            }
        };
        b.setMinimumSize(new Dimension(oneTouchSize, oneTouchSize));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        return b;
    }
    protected void prepareForDragging() {
        splitPaneUI.startDragging();
    }
    protected void dragDividerTo(int location) {
        splitPaneUI.dragDividerTo(location);
    }
    protected void finishDraggingTo(int location) {
        splitPaneUI.finishDraggingTo(location);
    }
    protected class MouseHandler extends MouseAdapter
            implements MouseMotionListener
    {
        public void mousePressed(MouseEvent e) {
            if ((e.getSource() == BasicSplitPaneDivider.this ||
                 e.getSource() == splitPane) &&
                dragger == null &&splitPane.isEnabled()) {
                Component            newHiddenDivider = splitPaneUI.
                                     getNonContinuousLayoutDivider();
                if (hiddenDivider != newHiddenDivider) {
                    if (hiddenDivider != null) {
                        hiddenDivider.removeMouseListener(this);
                        hiddenDivider.removeMouseMotionListener(this);
                    }
                    hiddenDivider = newHiddenDivider;
                    if (hiddenDivider != null) {
                        hiddenDivider.addMouseMotionListener(this);
                        hiddenDivider.addMouseListener(this);
                    }
                }
                if (splitPane.getLeftComponent() != null &&
                    splitPane.getRightComponent() != null) {
                    if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                        dragger = new DragController(e);
                    }
                    else {
                        dragger = new VerticalDragController(e);
                    }
                    if (!dragger.isValid()) {
                        dragger = null;
                    }
                    else {
                        prepareForDragging();
                        dragger.continueDrag(e);
                    }
                }
                e.consume();
            }
        }
        public void mouseReleased(MouseEvent e) {
            if (dragger != null) {
                if (e.getSource() == splitPane) {
                    dragger.completeDrag(e.getX(), e.getY());
                }
                else if (e.getSource() == BasicSplitPaneDivider.this) {
                    Point   ourLoc = getLocation();
                    dragger.completeDrag(e.getX() + ourLoc.x,
                                         e.getY() + ourLoc.y);
                }
                else if (e.getSource() == hiddenDivider) {
                    Point   hDividerLoc = hiddenDivider.getLocation();
                    int     ourX = e.getX() + hDividerLoc.x;
                    int     ourY = e.getY() + hDividerLoc.y;
                    dragger.completeDrag(ourX, ourY);
                }
                dragger = null;
                e.consume();
            }
        }
        public void mouseDragged(MouseEvent e) {
            if (dragger != null) {
                if (e.getSource() == splitPane) {
                    dragger.continueDrag(e.getX(), e.getY());
                }
                else if (e.getSource() == BasicSplitPaneDivider.this) {
                    Point   ourLoc = getLocation();
                    dragger.continueDrag(e.getX() + ourLoc.x,
                                         e.getY() + ourLoc.y);
                }
                else if (e.getSource() == hiddenDivider) {
                    Point   hDividerLoc = hiddenDivider.getLocation();
                    int     ourX = e.getX() + hDividerLoc.x;
                    int     ourY = e.getY() + hDividerLoc.y;
                    dragger.continueDrag(ourX, ourY);
                }
                e.consume();
            }
        }
        public void mouseMoved(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
            if (e.getSource() == BasicSplitPaneDivider.this) {
                setMouseOver(true);
            }
        }
        public void mouseExited(MouseEvent e) {
            if (e.getSource() == BasicSplitPaneDivider.this) {
                setMouseOver(false);
            }
        }
    }
    protected class DragController
    {
        int initialX;
        int maxX, minX;
        int offset;
        protected DragController(MouseEvent e) {
            JSplitPane  splitPane = splitPaneUI.getSplitPane();
            Component   leftC = splitPane.getLeftComponent();
            Component   rightC = splitPane.getRightComponent();
            initialX = getLocation().x;
            if (e.getSource() == BasicSplitPaneDivider.this) {
                offset = e.getX();
            }
            else { 
                offset = e.getX() - initialX;
            }
            if (leftC == null || rightC == null || offset < -1 ||
                offset >= getSize().width) {
                maxX = -1;
            }
            else {
                Insets      insets = splitPane.getInsets();
                if (leftC.isVisible()) {
                    minX = leftC.getMinimumSize().width;
                    if (insets != null) {
                        minX += insets.left;
                    }
                }
                else {
                    minX = 0;
                }
                if (rightC.isVisible()) {
                    int right = (insets != null) ? insets.right : 0;
                    maxX = Math.max(0, splitPane.getSize().width -
                                    (getSize().width + right) -
                                    rightC.getMinimumSize().width);
                }
                else {
                    int right = (insets != null) ? insets.right : 0;
                    maxX = Math.max(0, splitPane.getSize().width -
                                    (getSize().width + right));
                }
                if (maxX < minX) minX = maxX = 0;
            }
        }
        protected boolean isValid() {
            return (maxX > 0);
        }
        protected int positionForMouseEvent(MouseEvent e) {
            int newX = (e.getSource() == BasicSplitPaneDivider.this) ?
                        (e.getX() + getLocation().x) : e.getX();
            newX = Math.min(maxX, Math.max(minX, newX - offset));
            return newX;
        }
        protected int getNeededLocation(int x, int y) {
            int newX;
            newX = Math.min(maxX, Math.max(minX, x - offset));
            return newX;
        }
        protected void continueDrag(int newX, int newY) {
            dragDividerTo(getNeededLocation(newX, newY));
        }
        protected void continueDrag(MouseEvent e) {
            dragDividerTo(positionForMouseEvent(e));
        }
        protected void completeDrag(int x, int y) {
            finishDraggingTo(getNeededLocation(x, y));
        }
        protected void completeDrag(MouseEvent e) {
            finishDraggingTo(positionForMouseEvent(e));
        }
    } 
    protected class VerticalDragController extends DragController
    {
        protected VerticalDragController(MouseEvent e) {
            super(e);
            JSplitPane splitPane = splitPaneUI.getSplitPane();
            Component  leftC = splitPane.getLeftComponent();
            Component  rightC = splitPane.getRightComponent();
            initialX = getLocation().y;
            if (e.getSource() == BasicSplitPaneDivider.this) {
                offset = e.getY();
            }
            else {
                offset = e.getY() - initialX;
            }
            if (leftC == null || rightC == null || offset < -1 ||
                offset > getSize().height) {
                maxX = -1;
            }
            else {
                Insets     insets = splitPane.getInsets();
                if (leftC.isVisible()) {
                    minX = leftC.getMinimumSize().height;
                    if (insets != null) {
                        minX += insets.top;
                    }
                }
                else {
                    minX = 0;
                }
                if (rightC.isVisible()) {
                    int    bottom = (insets != null) ? insets.bottom : 0;
                    maxX = Math.max(0, splitPane.getSize().height -
                                    (getSize().height + bottom) -
                                    rightC.getMinimumSize().height);
                }
                else {
                    int    bottom = (insets != null) ? insets.bottom : 0;
                    maxX = Math.max(0, splitPane.getSize().height -
                                    (getSize().height + bottom));
                }
                if (maxX < minX) minX = maxX = 0;
            }
        }
        protected int getNeededLocation(int x, int y) {
            int newY;
            newY = Math.min(maxX, Math.max(minX, y - offset));
            return newY;
        }
        protected int positionForMouseEvent(MouseEvent e) {
            int newY = (e.getSource() == BasicSplitPaneDivider.this) ?
                        (e.getY() + getLocation().y) : e.getY();
            newY = Math.min(maxX, Math.max(minX, newY - offset));
            return newY;
        }
    } 
    protected class DividerLayout implements LayoutManager
    {
        public void layoutContainer(Container c) {
            if (leftButton != null && rightButton != null &&
                c == BasicSplitPaneDivider.this) {
                if (splitPane.isOneTouchExpandable()) {
                    Insets insets = getInsets();
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        int extraX = (insets != null) ? insets.left : 0;
                        int blockSize = getHeight();
                        if (insets != null) {
                            blockSize -= (insets.top + insets.bottom);
                            blockSize = Math.max(blockSize, 0);
                        }
                        blockSize = Math.min(blockSize, oneTouchSize);
                        int y = (c.getSize().height - blockSize) / 2;
                        if (!centerOneTouchButtons) {
                            y = (insets != null) ? insets.top : 0;
                            extraX = 0;
                        }
                        leftButton.setBounds(extraX + oneTouchOffset, y,
                                             blockSize * 2, blockSize);
                        rightButton.setBounds(extraX + oneTouchOffset +
                                              oneTouchSize * 2, y,
                                              blockSize * 2, blockSize);
                    }
                    else {
                        int extraY = (insets != null) ? insets.top : 0;
                        int blockSize = getWidth();
                        if (insets != null) {
                            blockSize -= (insets.left + insets.right);
                            blockSize = Math.max(blockSize, 0);
                        }
                        blockSize = Math.min(blockSize, oneTouchSize);
                        int x = (c.getSize().width - blockSize) / 2;
                        if (!centerOneTouchButtons) {
                            x = (insets != null) ? insets.left : 0;
                            extraY = 0;
                        }
                        leftButton.setBounds(x, extraY + oneTouchOffset,
                                             blockSize, blockSize * 2);
                        rightButton.setBounds(x, extraY + oneTouchOffset +
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
            if (c != BasicSplitPaneDivider.this || splitPane == null) {
                return new Dimension(0,0);
            }
            Dimension buttonMinSize = null;
            if (splitPane.isOneTouchExpandable() && leftButton != null) {
                buttonMinSize = leftButton.getMinimumSize();
            }
            Insets insets = getInsets();
            int width = getDividerSize();
            int height = width;
            if (orientation == JSplitPane.VERTICAL_SPLIT) {
                if (buttonMinSize != null) {
                    int size = buttonMinSize.height;
                    if (insets != null) {
                        size += insets.top + insets.bottom;
                    }
                    height = Math.max(height, size);
                }
                width = 1;
            }
            else {
                if (buttonMinSize != null) {
                    int size = buttonMinSize.width;
                    if (insets != null) {
                        size += insets.left + insets.right;
                    }
                    width = Math.max(width, size);
                }
                height = 1;
            }
            return new Dimension(width, height);
        }
        public Dimension preferredLayoutSize(Container c) {
            return minimumLayoutSize(c);
        }
        public void removeLayoutComponent(Component c) {}
        public void addLayoutComponent(String string, Component c) {}
    } 
    private class OneTouchActionHandler implements ActionListener {
        private boolean toMinimum;
        OneTouchActionHandler(boolean toMinimum) {
            this.toMinimum = toMinimum;
        }
        public void actionPerformed(ActionEvent e) {
            Insets  insets = splitPane.getInsets();
            int     lastLoc = splitPane.getLastDividerLocation();
            int     currentLoc = splitPaneUI.getDividerLocation(splitPane);
            int     newLoc;
            if (toMinimum) {
                if (orientation == JSplitPane.VERTICAL_SPLIT) {
                    if (currentLoc >= (splitPane.getHeight() -
                                       insets.bottom - getHeight())) {
                        int maxLoc = splitPane.getMaximumDividerLocation();
                        newLoc = Math.min(lastLoc, maxLoc);
                        splitPaneUI.setKeepHidden(false);
                    }
                    else {
                        newLoc = insets.top;
                        splitPaneUI.setKeepHidden(true);
                    }
                }
                else {
                    if (currentLoc >= (splitPane.getWidth() -
                                       insets.right - getWidth())) {
                        int maxLoc = splitPane.getMaximumDividerLocation();
                        newLoc = Math.min(lastLoc, maxLoc);
                        splitPaneUI.setKeepHidden(false);
                    }
                    else {
                        newLoc = insets.left;
                        splitPaneUI.setKeepHidden(true);
                    }
                }
            }
            else {
                if (orientation == JSplitPane.VERTICAL_SPLIT) {
                    if (currentLoc == insets.top) {
                        int maxLoc = splitPane.getMaximumDividerLocation();
                        newLoc = Math.min(lastLoc, maxLoc);
                        splitPaneUI.setKeepHidden(false);
                    }
                    else {
                        newLoc = splitPane.getHeight() - getHeight() -
                                 insets.top;
                        splitPaneUI.setKeepHidden(true);
                    }
                }
                else {
                    if (currentLoc == insets.left) {
                        int maxLoc = splitPane.getMaximumDividerLocation();
                        newLoc = Math.min(lastLoc, maxLoc);
                        splitPaneUI.setKeepHidden(false);
                    }
                    else {
                        newLoc = splitPane.getWidth() - getWidth() -
                                 insets.left;
                        splitPaneUI.setKeepHidden(true);
                    }
                }
            }
            if (currentLoc != newLoc) {
                splitPane.setDividerLocation(newLoc);
                splitPane.setLastDividerLocation(currentLoc);
            }
        }
    } 
}
