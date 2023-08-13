public class BasicSplitPaneUI extends SplitPaneUI
{
    protected static final String NON_CONTINUOUS_DIVIDER =
        "nonContinuousDivider";
    protected static int KEYBOARD_DIVIDER_MOVE_OFFSET = 3;
    protected JSplitPane splitPane;
    protected BasicHorizontalLayoutManager layoutManager;
    protected BasicSplitPaneDivider divider;
    protected PropertyChangeListener propertyChangeListener;
    protected FocusListener focusListener;
    private Handler handler;
    private Set<KeyStroke> managingFocusForwardTraversalKeys;
    private Set<KeyStroke> managingFocusBackwardTraversalKeys;
    protected int dividerSize;
    protected Component nonContinuousLayoutDivider;
    protected boolean draggingHW;
    protected int beginDragDividerLocation;
    @Deprecated
    protected KeyStroke upKey;
    @Deprecated
    protected KeyStroke downKey;
    @Deprecated
    protected KeyStroke leftKey;
    @Deprecated
    protected KeyStroke rightKey;
    @Deprecated
    protected KeyStroke homeKey;
    @Deprecated
    protected KeyStroke endKey;
    @Deprecated
    protected KeyStroke dividerResizeToggleKey;
    @Deprecated
    protected ActionListener keyboardUpLeftListener;
    @Deprecated
    protected ActionListener keyboardDownRightListener;
    @Deprecated
    protected ActionListener keyboardHomeListener;
    @Deprecated
    protected ActionListener keyboardEndListener;
    @Deprecated
    protected ActionListener keyboardResizeToggleListener;
    private int         orientation;
    private int         lastDragLocation;
    private boolean     continuousLayout;
    private boolean     dividerKeyboardResize;
    private boolean     dividerLocationIsSet;  
    private Color dividerDraggingColor;
    private boolean rememberPaneSizes;
    private boolean keepHidden = false;
    boolean             painted;
    boolean             ignoreDividerLocationChange;
    public static ComponentUI createUI(JComponent x) {
        return new BasicSplitPaneUI();
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.NEGATIVE_INCREMENT));
        map.put(new Actions(Actions.POSITIVE_INCREMENT));
        map.put(new Actions(Actions.SELECT_MIN));
        map.put(new Actions(Actions.SELECT_MAX));
        map.put(new Actions(Actions.START_RESIZE));
        map.put(new Actions(Actions.TOGGLE_FOCUS));
        map.put(new Actions(Actions.FOCUS_OUT_FORWARD));
        map.put(new Actions(Actions.FOCUS_OUT_BACKWARD));
    }
    public void installUI(JComponent c) {
        splitPane = (JSplitPane) c;
        dividerLocationIsSet = false;
        dividerKeyboardResize = false;
        keepHidden = false;
        installDefaults();
        installListeners();
        installKeyboardActions();
        setLastDragLocation(-1);
    }
    protected void installDefaults(){
        LookAndFeel.installBorder(splitPane, "SplitPane.border");
        LookAndFeel.installColors(splitPane, "SplitPane.background",
                                  "SplitPane.foreground");
        LookAndFeel.installProperty(splitPane, "opaque", Boolean.TRUE);
        if (divider == null) divider = createDefaultDivider();
        divider.setBasicSplitPaneUI(this);
        Border    b = divider.getBorder();
        if (b == null || !(b instanceof UIResource)) {
            divider.setBorder(UIManager.getBorder("SplitPaneDivider.border"));
        }
        dividerDraggingColor = UIManager.getColor("SplitPaneDivider.draggingColor");
        setOrientation(splitPane.getOrientation());
        Integer temp = (Integer)UIManager.get("SplitPane.dividerSize");
        LookAndFeel.installProperty(splitPane, "dividerSize", temp == null? 10: temp);
        divider.setDividerSize(splitPane.getDividerSize());
        dividerSize = divider.getDividerSize();
        splitPane.add(divider, JSplitPane.DIVIDER);
        setContinuousLayout(splitPane.isContinuousLayout());
        resetLayoutManager();
        if(nonContinuousLayoutDivider == null) {
            setNonContinuousLayoutDivider(
                                createDefaultNonContinuousLayoutDivider(),
                                true);
        } else {
            setNonContinuousLayoutDivider(nonContinuousLayoutDivider, true);
        }
        if (managingFocusForwardTraversalKeys==null) {
            managingFocusForwardTraversalKeys = new HashSet<KeyStroke>();
            managingFocusForwardTraversalKeys.add(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        }
        splitPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                        managingFocusForwardTraversalKeys);
        if (managingFocusBackwardTraversalKeys==null) {
            managingFocusBackwardTraversalKeys = new HashSet<KeyStroke>();
            managingFocusBackwardTraversalKeys.add(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        }
        splitPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                        managingFocusBackwardTraversalKeys);
    }
    protected void installListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) !=
            null) {
            splitPane.addPropertyChangeListener(propertyChangeListener);
        }
        if ((focusListener = createFocusListener()) != null) {
            splitPane.addFocusListener(focusListener);
        }
    }
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.
                                  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(splitPane, JComponent.
                                       WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                       km);
        LazyActionMap.installLazyActionMap(splitPane, BasicSplitPaneUI.class,
                                           "SplitPane.actionMap");
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap)DefaultLookup.get(splitPane, this,
                                               "SplitPane.ancestorInputMap");
        }
        return null;
    }
    public void uninstallUI(JComponent c) {
        uninstallKeyboardActions();
        uninstallListeners();
        uninstallDefaults();
        dividerLocationIsSet = false;
        dividerKeyboardResize = false;
        splitPane = null;
    }
    protected void uninstallDefaults() {
        if(splitPane.getLayout() == layoutManager) {
            splitPane.setLayout(null);
        }
        if(nonContinuousLayoutDivider != null) {
            splitPane.remove(nonContinuousLayoutDivider);
        }
        LookAndFeel.uninstallBorder(splitPane);
        Border    b = divider.getBorder();
        if (b instanceof UIResource) {
            divider.setBorder(null);
        }
        splitPane.remove(divider);
        divider.setBasicSplitPaneUI(null);
        layoutManager = null;
        divider = null;
        nonContinuousLayoutDivider = null;
        setNonContinuousLayoutDivider(null);
        splitPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        splitPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    }
    protected void uninstallListeners() {
        if (propertyChangeListener != null) {
            splitPane.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
        if (focusListener != null) {
            splitPane.removeFocusListener(focusListener);
            focusListener = null;
        }
        keyboardUpLeftListener = null;
        keyboardDownRightListener = null;
        keyboardHomeListener = null;
        keyboardEndListener = null;
        keyboardResizeToggleListener = null;
        handler = null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(splitPane, null);
        SwingUtilities.replaceUIInputMap(splitPane, JComponent.
                                      WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                      null);
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected FocusListener createFocusListener() {
        return getHandler();
    }
    @Deprecated
    protected ActionListener createKeyboardUpLeftListener() {
        return new KeyboardUpLeftHandler();
    }
    @Deprecated
    protected ActionListener createKeyboardDownRightListener() {
        return new KeyboardDownRightHandler();
    }
    @Deprecated
    protected ActionListener createKeyboardHomeListener() {
        return new KeyboardHomeHandler();
    }
    @Deprecated
    protected ActionListener createKeyboardEndListener() {
        return new KeyboardEndHandler();
    }
    @Deprecated
    protected ActionListener createKeyboardResizeToggleListener() {
        return new KeyboardResizeToggleHandler();
    }
    public int getOrientation() {
        return orientation;
    }
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    public boolean isContinuousLayout() {
        return continuousLayout;
    }
    public void setContinuousLayout(boolean b) {
        continuousLayout = b;
    }
    public int getLastDragLocation() {
        return lastDragLocation;
    }
    public void setLastDragLocation(int l) {
        lastDragLocation = l;
    }
    int getKeyboardMoveIncrement() {
        return 3;
    }
    public class PropertyHandler implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }
    public class FocusHandler extends FocusAdapter
    {
        public void focusGained(FocusEvent ev) {
            getHandler().focusGained(ev);
        }
        public void focusLost(FocusEvent ev) {
            getHandler().focusLost(ev);
        }
    }
    public class KeyboardUpLeftHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent ev) {
            if (dividerKeyboardResize) {
                splitPane.setDividerLocation(Math.max(0,getDividerLocation
                                  (splitPane) - getKeyboardMoveIncrement()));
            }
        }
    }
    public class KeyboardDownRightHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent ev) {
            if (dividerKeyboardResize) {
                splitPane.setDividerLocation(getDividerLocation(splitPane) +
                                             getKeyboardMoveIncrement());
            }
        }
    }
    public class KeyboardHomeHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent ev) {
            if (dividerKeyboardResize) {
                splitPane.setDividerLocation(0);
            }
        }
    }
    public class KeyboardEndHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent ev) {
            if (dividerKeyboardResize) {
                Insets   insets = splitPane.getInsets();
                int      bottomI = (insets != null) ? insets.bottom : 0;
                int      rightI = (insets != null) ? insets.right : 0;
                if (orientation == JSplitPane.VERTICAL_SPLIT) {
                    splitPane.setDividerLocation(splitPane.getHeight() -
                                       bottomI);
                }
                else {
                    splitPane.setDividerLocation(splitPane.getWidth() -
                                                 rightI);
                }
            }
        }
    }
    public class KeyboardResizeToggleHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent ev) {
            if (!dividerKeyboardResize) {
                splitPane.requestFocus();
            }
        }
    }
    public BasicSplitPaneDivider getDivider() {
        return divider;
    }
    protected Component createDefaultNonContinuousLayoutDivider() {
        return new Canvas() {
            public void paint(Graphics g) {
                if(!isContinuousLayout() && getLastDragLocation() != -1) {
                    Dimension      size = splitPane.getSize();
                    g.setColor(dividerDraggingColor);
                    if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                        g.fillRect(0, 0, dividerSize - 1, size.height - 1);
                    } else {
                        g.fillRect(0, 0, size.width - 1, dividerSize - 1);
                    }
                }
            }
        };
    }
    protected void setNonContinuousLayoutDivider(Component newDivider) {
        setNonContinuousLayoutDivider(newDivider, true);
    }
    protected void setNonContinuousLayoutDivider(Component newDivider,
        boolean rememberSizes) {
        rememberPaneSizes = rememberSizes;
        if(nonContinuousLayoutDivider != null && splitPane != null) {
            splitPane.remove(nonContinuousLayoutDivider);
        }
        nonContinuousLayoutDivider = newDivider;
    }
    private void addHeavyweightDivider() {
        if(nonContinuousLayoutDivider != null && splitPane != null) {
            Component             leftC = splitPane.getLeftComponent();
            Component             rightC = splitPane.getRightComponent();
            int                   lastLocation = splitPane.
                                              getDividerLocation();
            if(leftC != null)
                splitPane.setLeftComponent(null);
            if(rightC != null)
                splitPane.setRightComponent(null);
            splitPane.remove(divider);
            splitPane.add(nonContinuousLayoutDivider, BasicSplitPaneUI.
                          NON_CONTINUOUS_DIVIDER,
                          splitPane.getComponentCount());
            splitPane.setLeftComponent(leftC);
            splitPane.setRightComponent(rightC);
            splitPane.add(divider, JSplitPane.DIVIDER);
            if(rememberPaneSizes) {
                splitPane.setDividerLocation(lastLocation);
            }
        }
    }
    public Component getNonContinuousLayoutDivider() {
        return nonContinuousLayoutDivider;
    }
    public JSplitPane getSplitPane() {
        return splitPane;
    }
    public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this);
    }
    public void resetToPreferredSizes(JSplitPane jc) {
        if(splitPane != null) {
            layoutManager.resetToPreferredSizes();
            splitPane.revalidate();
            splitPane.repaint();
        }
    }
    public void setDividerLocation(JSplitPane jc, int location) {
        if (!ignoreDividerLocationChange) {
            dividerLocationIsSet = true;
            splitPane.revalidate();
            splitPane.repaint();
            if (keepHidden) {
                Insets insets = splitPane.getInsets();
                int orientation = splitPane.getOrientation();
                if ((orientation == JSplitPane.VERTICAL_SPLIT &&
                     location != insets.top &&
                     location != splitPane.getHeight()-divider.getHeight()-insets.top) ||
                    (orientation == JSplitPane.HORIZONTAL_SPLIT &&
                     location != insets.left &&
                     location != splitPane.getWidth()-divider.getWidth()-insets.left)) {
                    setKeepHidden(false);
                }
            }
        }
        else {
            ignoreDividerLocationChange = false;
        }
    }
    public int getDividerLocation(JSplitPane jc) {
        if(orientation == JSplitPane.HORIZONTAL_SPLIT)
            return divider.getLocation().x;
        return divider.getLocation().y;
    }
    public int getMinimumDividerLocation(JSplitPane jc) {
        int       minLoc = 0;
        Component leftC = splitPane.getLeftComponent();
        if ((leftC != null) && (leftC.isVisible())) {
            Insets    insets = splitPane.getInsets();
            Dimension minSize = leftC.getMinimumSize();
            if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                minLoc = minSize.width;
            } else {
                minLoc = minSize.height;
            }
            if(insets != null) {
                if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                    minLoc += insets.left;
                } else {
                    minLoc += insets.top;
                }
            }
        }
        return minLoc;
    }
    public int getMaximumDividerLocation(JSplitPane jc) {
        Dimension splitPaneSize = splitPane.getSize();
        int       maxLoc = 0;
        Component rightC = splitPane.getRightComponent();
        if (rightC != null) {
            Insets    insets = splitPane.getInsets();
            Dimension minSize = new Dimension(0, 0);
            if (rightC.isVisible()) {
                minSize = rightC.getMinimumSize();
            }
            if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                maxLoc = splitPaneSize.width - minSize.width;
            } else {
                maxLoc = splitPaneSize.height - minSize.height;
            }
            maxLoc -= dividerSize;
            if(insets != null) {
                if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                    maxLoc -= insets.right;
                } else {
                    maxLoc -= insets.top;
                }
            }
        }
        return Math.max(getMinimumDividerLocation(splitPane), maxLoc);
    }
    public void finishedPaintingChildren(JSplitPane sp, Graphics g) {
        if(sp == splitPane && getLastDragLocation() != -1 &&
           !isContinuousLayout() && !draggingHW) {
            Dimension      size = splitPane.getSize();
            g.setColor(dividerDraggingColor);
            if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                g.fillRect(getLastDragLocation(), 0, dividerSize - 1,
                           size.height - 1);
            } else {
                g.fillRect(0, lastDragLocation, size.width - 1,
                           dividerSize - 1);
            }
        }
    }
    public void paint(Graphics g, JComponent jc) {
        if (!painted && splitPane.getDividerLocation()<0) {
            ignoreDividerLocationChange = true;
            splitPane.setDividerLocation(getDividerLocation(splitPane));
        }
        painted = true;
    }
    public Dimension getPreferredSize(JComponent jc) {
        if(splitPane != null)
            return layoutManager.preferredLayoutSize(splitPane);
        return new Dimension(0, 0);
    }
    public Dimension getMinimumSize(JComponent jc) {
        if(splitPane != null)
            return layoutManager.minimumLayoutSize(splitPane);
        return new Dimension(0, 0);
    }
    public Dimension getMaximumSize(JComponent jc) {
        if(splitPane != null)
            return layoutManager.maximumLayoutSize(splitPane);
        return new Dimension(0, 0);
    }
    public Insets getInsets(JComponent jc) {
        return null;
    }
    protected void resetLayoutManager() {
        if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
            layoutManager = new BasicHorizontalLayoutManager(0);
        } else {
            layoutManager = new BasicHorizontalLayoutManager(1);
        }
        splitPane.setLayout(layoutManager);
        layoutManager.updateComponents();
        splitPane.revalidate();
        splitPane.repaint();
    }
    void setKeepHidden(boolean keepHidden) {
        this.keepHidden = keepHidden;
    }
    private boolean getKeepHidden() {
        return keepHidden;
    }
    protected void startDragging() {
        Component       leftC = splitPane.getLeftComponent();
        Component       rightC = splitPane.getRightComponent();
        ComponentPeer   cPeer;
        beginDragDividerLocation = getDividerLocation(splitPane);
        draggingHW = false;
        if(leftC != null && (cPeer = leftC.getPeer()) != null &&
           !(cPeer instanceof LightweightPeer)) {
            draggingHW = true;
        } else if(rightC != null && (cPeer = rightC.getPeer()) != null
                  && !(cPeer instanceof LightweightPeer)) {
            draggingHW = true;
        }
        if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
            setLastDragLocation(divider.getBounds().x);
            dividerSize = divider.getSize().width;
            if(!isContinuousLayout() && draggingHW) {
                nonContinuousLayoutDivider.setBounds
                        (getLastDragLocation(), 0, dividerSize,
                         splitPane.getHeight());
                      addHeavyweightDivider();
            }
        } else {
            setLastDragLocation(divider.getBounds().y);
            dividerSize = divider.getSize().height;
            if(!isContinuousLayout() && draggingHW) {
                nonContinuousLayoutDivider.setBounds
                        (0, getLastDragLocation(), splitPane.getWidth(),
                         dividerSize);
                      addHeavyweightDivider();
            }
        }
    }
    protected void dragDividerTo(int location) {
        if(getLastDragLocation() != location) {
            if(isContinuousLayout()) {
                splitPane.setDividerLocation(location);
                setLastDragLocation(location);
            } else {
                int lastLoc = getLastDragLocation();
                setLastDragLocation(location);
                if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                    if(draggingHW) {
                        nonContinuousLayoutDivider.setLocation(
                            getLastDragLocation(), 0);
                    } else {
                        int   splitHeight = splitPane.getHeight();
                        splitPane.repaint(lastLoc, 0, dividerSize,
                                          splitHeight);
                        splitPane.repaint(location, 0, dividerSize,
                                          splitHeight);
                    }
                } else {
                    if(draggingHW) {
                        nonContinuousLayoutDivider.setLocation(0,
                            getLastDragLocation());
                    } else {
                        int    splitWidth = splitPane.getWidth();
                        splitPane.repaint(0, lastLoc, splitWidth,
                                          dividerSize);
                        splitPane.repaint(0, location, splitWidth,
                                          dividerSize);
                    }
                }
            }
        }
    }
    protected void finishDraggingTo(int location) {
        dragDividerTo(location);
        setLastDragLocation(-1);
        if(!isContinuousLayout()) {
            Component   leftC = splitPane.getLeftComponent();
            Rectangle   leftBounds = leftC.getBounds();
            if (draggingHW) {
                if(orientation == JSplitPane.HORIZONTAL_SPLIT) {
                    nonContinuousLayoutDivider.setLocation(-dividerSize, 0);
                }
                else {
                    nonContinuousLayoutDivider.setLocation(0, -dividerSize);
                }
                splitPane.remove(nonContinuousLayoutDivider);
            }
            splitPane.setDividerLocation(location);
        }
    }
    @Deprecated
    protected int getDividerBorderSize() {
        return 1;
    }
    public class BasicHorizontalLayoutManager implements LayoutManager2
    {
        protected int[]         sizes;
        protected Component[]   components;
        private int             lastSplitPaneSize;
        private boolean         doReset;
        private int             axis;
        BasicHorizontalLayoutManager() {
            this(0);
        }
        BasicHorizontalLayoutManager(int axis) {
            this.axis = axis;
            components = new Component[3];
            components[0] = components[1] = components[2] = null;
            sizes = new int[3];
        }
        public void layoutContainer(Container container) {
            Dimension   containerSize = container.getSize();
            if (containerSize.height <= 0 || containerSize.width <= 0 ) {
                lastSplitPaneSize = 0;
                return;
            }
            int         spDividerLocation = splitPane.getDividerLocation();
            Insets      insets = splitPane.getInsets();
            int         availableSize = getAvailableSize(containerSize,
                                                         insets);
            int         newSize = getSizeForPrimaryAxis(containerSize);
            int         beginLocation = getDividerLocation(splitPane);
            int         dOffset = getSizeForPrimaryAxis(insets, true);
            Dimension   dSize = (components[2] == null) ? null :
                                 components[2].getPreferredSize();
            if ((doReset && !dividerLocationIsSet) || spDividerLocation < 0) {
                resetToPreferredSizes(availableSize);
            }
            else if (lastSplitPaneSize <= 0 ||
                     availableSize == lastSplitPaneSize || !painted ||
                     (dSize != null &&
                      getSizeForPrimaryAxis(dSize) != sizes[2])) {
                if (dSize != null) {
                    sizes[2] = getSizeForPrimaryAxis(dSize);
                }
                else {
                    sizes[2] = 0;
                }
                setDividerLocation(spDividerLocation - dOffset, availableSize);
                dividerLocationIsSet = false;
            }
            else if (availableSize != lastSplitPaneSize) {
                distributeSpace(availableSize - lastSplitPaneSize,
                                getKeepHidden());
            }
            doReset = false;
            dividerLocationIsSet = false;
            lastSplitPaneSize = availableSize;
            int nextLocation = getInitialLocation(insets);
            int counter = 0;
            while (counter < 3) {
                if (components[counter] != null &&
                    components[counter].isVisible()) {
                    setComponentToSize(components[counter], sizes[counter],
                                       nextLocation, insets, containerSize);
                    nextLocation += sizes[counter];
                }
                switch (counter) {
                case 0:
                    counter = 2;
                    break;
                case 2:
                    counter = 1;
                    break;
                case 1:
                    counter = 3;
                    break;
                }
            }
            if (painted) {
                int      newLocation = getDividerLocation(splitPane);
                if (newLocation != (spDividerLocation - dOffset)) {
                    int  lastLocation = splitPane.getLastDividerLocation();
                    ignoreDividerLocationChange = true;
                    try {
                        splitPane.setDividerLocation(newLocation);
                        splitPane.setLastDividerLocation(lastLocation);
                    } finally {
                        ignoreDividerLocationChange = false;
                    }
                }
            }
        }
        public void addLayoutComponent(String place, Component component) {
            boolean isValid = true;
            if(place != null) {
                if(place.equals(JSplitPane.DIVIDER)) {
                    components[2] = component;
                    sizes[2] = getSizeForPrimaryAxis(component.
                                                     getPreferredSize());
                } else if(place.equals(JSplitPane.LEFT) ||
                          place.equals(JSplitPane.TOP)) {
                    components[0] = component;
                    sizes[0] = 0;
                } else if(place.equals(JSplitPane.RIGHT) ||
                          place.equals(JSplitPane.BOTTOM)) {
                    components[1] = component;
                    sizes[1] = 0;
                } else if(!place.equals(
                                    BasicSplitPaneUI.NON_CONTINUOUS_DIVIDER))
                    isValid = false;
            } else {
                isValid = false;
            }
            if(!isValid)
                throw new IllegalArgumentException("cannot add to layout: " +
                    "unknown constraint: " +
                    place);
            doReset = true;
        }
        public Dimension minimumLayoutSize(Container container) {
            int         minPrimary = 0;
            int         minSecondary = 0;
            Insets      insets = splitPane.getInsets();
            for (int counter=0; counter<3; counter++) {
                if(components[counter] != null) {
                    Dimension   minSize = components[counter].getMinimumSize();
                    int         secSize = getSizeForSecondaryAxis(minSize);
                    minPrimary += getSizeForPrimaryAxis(minSize);
                    if(secSize > minSecondary)
                        minSecondary = secSize;
                }
            }
            if(insets != null) {
                minPrimary += getSizeForPrimaryAxis(insets, true) +
                              getSizeForPrimaryAxis(insets, false);
                minSecondary += getSizeForSecondaryAxis(insets, true) +
                              getSizeForSecondaryAxis(insets, false);
            }
            if (axis == 0) {
                return new Dimension(minPrimary, minSecondary);
            }
            return new Dimension(minSecondary, minPrimary);
        }
        public Dimension preferredLayoutSize(Container container) {
            int         prePrimary = 0;
            int         preSecondary = 0;
            Insets      insets = splitPane.getInsets();
            for(int counter = 0; counter < 3; counter++) {
                if(components[counter] != null) {
                    Dimension   preSize = components[counter].
                                          getPreferredSize();
                    int         secSize = getSizeForSecondaryAxis(preSize);
                    prePrimary += getSizeForPrimaryAxis(preSize);
                    if(secSize > preSecondary)
                        preSecondary = secSize;
                }
            }
            if(insets != null) {
                prePrimary += getSizeForPrimaryAxis(insets, true) +
                              getSizeForPrimaryAxis(insets, false);
                preSecondary += getSizeForSecondaryAxis(insets, true) +
                              getSizeForSecondaryAxis(insets, false);
            }
            if (axis == 0) {
                return new Dimension(prePrimary, preSecondary);
            }
            return new Dimension(preSecondary, prePrimary);
        }
        public void removeLayoutComponent(Component component) {
            for(int counter = 0; counter < 3; counter++) {
                if(components[counter] == component) {
                    components[counter] = null;
                    sizes[counter] = 0;
                    doReset = true;
                }
            }
        }
        public void addLayoutComponent(Component comp, Object constraints) {
            if ((constraints == null) || (constraints instanceof String)) {
                addLayoutComponent((String)constraints, comp);
            } else {
                throw new IllegalArgumentException("cannot add to layout: " +
                                                   "constraint must be a " +
                                                   "string (or null)");
            }
        }
        public float getLayoutAlignmentX(Container target) {
            return 0.0f;
        }
        public float getLayoutAlignmentY(Container target) {
            return 0.0f;
        }
        public void invalidateLayout(Container c) {
        }
        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        public void resetToPreferredSizes() {
            doReset = true;
        }
        protected void resetSizeAt(int index) {
            sizes[index] = 0;
            doReset = true;
        }
        protected void setSizes(int[] newSizes) {
            System.arraycopy(newSizes, 0, sizes, 0, 3);
        }
        protected int[] getSizes() {
            int[]         retSizes = new int[3];
            System.arraycopy(sizes, 0, retSizes, 0, 3);
            return retSizes;
        }
        protected int getPreferredSizeOfComponent(Component c) {
            return getSizeForPrimaryAxis(c.getPreferredSize());
        }
        int getMinimumSizeOfComponent(Component c) {
            return getSizeForPrimaryAxis(c.getMinimumSize());
        }
        protected int getSizeOfComponent(Component c) {
            return getSizeForPrimaryAxis(c.getSize());
        }
        protected int getAvailableSize(Dimension containerSize,
                                       Insets insets) {
            if(insets == null)
                return getSizeForPrimaryAxis(containerSize);
            return (getSizeForPrimaryAxis(containerSize) -
                    (getSizeForPrimaryAxis(insets, true) +
                     getSizeForPrimaryAxis(insets, false)));
        }
        protected int getInitialLocation(Insets insets) {
            if(insets != null)
                return getSizeForPrimaryAxis(insets, true);
            return 0;
        }
        protected void setComponentToSize(Component c, int size,
                                          int location, Insets insets,
                                          Dimension containerSize) {
            if(insets != null) {
                if (axis == 0) {
                    c.setBounds(location, insets.top, size,
                                containerSize.height -
                                (insets.top + insets.bottom));
                }
                else {
                    c.setBounds(insets.left, location, containerSize.width -
                                (insets.left + insets.right), size);
                }
            }
            else {
                if (axis == 0) {
                    c.setBounds(location, 0, size, containerSize.height);
                }
                else {
                    c.setBounds(0, location, containerSize.width, size);
                }
            }
        }
        int getSizeForPrimaryAxis(Dimension size) {
            if (axis == 0) {
                return size.width;
            }
            return size.height;
        }
        int getSizeForSecondaryAxis(Dimension size) {
            if (axis == 0) {
                return size.height;
            }
            return size.width;
        }
        int getSizeForPrimaryAxis(Insets insets, boolean isTop) {
            if (axis == 0) {
                if (isTop) {
                    return insets.left;
                }
                return insets.right;
            }
            if (isTop) {
                return insets.top;
            }
            return insets.bottom;
        }
        int getSizeForSecondaryAxis(Insets insets, boolean isTop) {
            if (axis == 0) {
                if (isTop) {
                    return insets.top;
                }
                return insets.bottom;
            }
            if (isTop) {
                return insets.left;
            }
            return insets.right;
        }
        protected void updateComponents() {
            Component comp;
            comp = splitPane.getLeftComponent();
            if(components[0] != comp) {
                components[0] = comp;
                if(comp == null) {
                    sizes[0] = 0;
                } else {
                    sizes[0] = -1;
                }
            }
            comp = splitPane.getRightComponent();
            if(components[1] != comp) {
                components[1] = comp;
                if(comp == null) {
                    sizes[1] = 0;
                } else {
                    sizes[1] = -1;
                }
            }
            Component[] children = splitPane.getComponents();
            Component   oldDivider = components[2];
            components[2] = null;
            for(int counter = children.length - 1; counter >= 0; counter--) {
                if(children[counter] != components[0] &&
                   children[counter] != components[1] &&
                   children[counter] != nonContinuousLayoutDivider) {
                    if(oldDivider != children[counter]) {
                        components[2] = children[counter];
                    } else {
                        components[2] = oldDivider;
                    }
                    break;
                }
            }
            if(components[2] == null) {
                sizes[2] = 0;
            }
            else {
                sizes[2] = getSizeForPrimaryAxis(components[2].getPreferredSize());
            }
        }
        void setDividerLocation(int leftSize, int availableSize) {
            boolean          lValid = (components[0] != null &&
                                       components[0].isVisible());
            boolean          rValid = (components[1] != null &&
                                       components[1].isVisible());
            boolean          dValid = (components[2] != null &&
                                       components[2].isVisible());
            int              max = availableSize;
            if (dValid) {
                max -= sizes[2];
            }
            leftSize = Math.max(0, Math.min(leftSize, max));
            if (lValid) {
                if (rValid) {
                    sizes[0] = leftSize;
                    sizes[1] = max - leftSize;
                }
                else {
                    sizes[0] = max;
                    sizes[1] = 0;
                }
            }
            else if (rValid) {
                sizes[1] = max;
                sizes[0] = 0;
            }
        }
        int[] getPreferredSizes() {
            int[]         retValue = new int[3];
            for (int counter = 0; counter < 3; counter++) {
                if (components[counter] != null &&
                    components[counter].isVisible()) {
                    retValue[counter] = getPreferredSizeOfComponent
                                        (components[counter]);
                }
                else {
                    retValue[counter] = -1;
                }
            }
            return retValue;
        }
        int[] getMinimumSizes() {
            int[]         retValue = new int[3];
            for (int counter = 0; counter < 2; counter++) {
                if (components[counter] != null &&
                    components[counter].isVisible()) {
                    retValue[counter] = getMinimumSizeOfComponent
                                        (components[counter]);
                }
                else {
                    retValue[counter] = -1;
                }
            }
            retValue[2] = (components[2] != null) ?
                getMinimumSizeOfComponent(components[2]) : -1;
            return retValue;
        }
        void resetToPreferredSizes(int availableSize) {
            int[]       testSizes = getPreferredSizes();
            int         totalSize = 0;
            for (int counter = 0; counter < 3; counter++) {
                if (testSizes[counter] != -1) {
                    totalSize += testSizes[counter];
                }
            }
            if (totalSize > availableSize) {
                testSizes = getMinimumSizes();
                totalSize = 0;
                for (int counter = 0; counter < 3; counter++) {
                    if (testSizes[counter] != -1) {
                        totalSize += testSizes[counter];
                    }
                }
            }
            setSizes(testSizes);
            distributeSpace(availableSize - totalSize, false);
        }
        void distributeSpace(int space, boolean keepHidden) {
            boolean          lValid = (components[0] != null &&
                                       components[0].isVisible());
            boolean          rValid = (components[1] != null &&
                                       components[1].isVisible());
            if (keepHidden) {
                if (lValid && getSizeForPrimaryAxis(
                                 components[0].getSize()) == 0) {
                    lValid = false;
                    if (rValid && getSizeForPrimaryAxis(
                                     components[1].getSize()) == 0) {
                        lValid = true;
                    }
                }
                else if (rValid && getSizeForPrimaryAxis(
                                   components[1].getSize()) == 0) {
                    rValid = false;
                }
            }
            if (lValid && rValid) {
                double        weight = splitPane.getResizeWeight();
                int           lExtra = (int)(weight * (double)space);
                int           rExtra = (space - lExtra);
                sizes[0] += lExtra;
                sizes[1] += rExtra;
                int           lMin = getMinimumSizeOfComponent(components[0]);
                int           rMin = getMinimumSizeOfComponent(components[1]);
                boolean       lMinValid = (sizes[0] >= lMin);
                boolean       rMinValid = (sizes[1] >= rMin);
                if (!lMinValid && !rMinValid) {
                    if (sizes[0] < 0) {
                        sizes[1] += sizes[0];
                        sizes[0] = 0;
                    }
                    else if (sizes[1] < 0) {
                        sizes[0] += sizes[1];
                        sizes[1] = 0;
                    }
                }
                else if (!lMinValid) {
                    if (sizes[1] - (lMin - sizes[0]) < rMin) {
                        if (sizes[0] < 0) {
                            sizes[1] += sizes[0];
                            sizes[0] = 0;
                        }
                    }
                    else {
                        sizes[1] -= (lMin - sizes[0]);
                        sizes[0] = lMin;
                    }
                }
                else if (!rMinValid) {
                    if (sizes[0] - (rMin - sizes[1]) < lMin) {
                        if (sizes[1] < 0) {
                            sizes[0] += sizes[1];
                            sizes[1] = 0;
                        }
                    }
                    else {
                        sizes[0] -= (rMin - sizes[1]);
                        sizes[1] = rMin;
                    }
                }
                if (sizes[0] < 0) {
                    sizes[0] = 0;
                }
                if (sizes[1] < 0) {
                    sizes[1] = 0;
                }
            }
            else if (lValid) {
                sizes[0] = Math.max(0, sizes[0] + space);
            }
            else if (rValid) {
                sizes[1] = Math.max(0, sizes[1] + space);
            }
        }
    }
    public class BasicVerticalLayoutManager extends
            BasicHorizontalLayoutManager
    {
        public BasicVerticalLayoutManager() {
            super(1);
        }
    }
    private class Handler implements FocusListener, PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            if(e.getSource() == splitPane) {
                String changeName = e.getPropertyName();
                if(changeName == JSplitPane.ORIENTATION_PROPERTY) {
                    orientation = splitPane.getOrientation();
                    resetLayoutManager();
                } else if(changeName == JSplitPane.CONTINUOUS_LAYOUT_PROPERTY){
                    setContinuousLayout(splitPane.isContinuousLayout());
                    if(!isContinuousLayout()) {
                        if(nonContinuousLayoutDivider == null) {
                            setNonContinuousLayoutDivider(
                                createDefaultNonContinuousLayoutDivider(),
                                true);
                        } else if(nonContinuousLayoutDivider.getParent() ==
                                  null) {
                            setNonContinuousLayoutDivider(
                                nonContinuousLayoutDivider,
                                true);
                        }
                    }
                } else if(changeName == JSplitPane.DIVIDER_SIZE_PROPERTY){
                    divider.setDividerSize(splitPane.getDividerSize());
                    dividerSize = divider.getDividerSize();
                    splitPane.revalidate();
                    splitPane.repaint();
                }
            }
        }
        public void focusGained(FocusEvent ev) {
            dividerKeyboardResize = true;
            splitPane.repaint();
        }
        public void focusLost(FocusEvent ev) {
            dividerKeyboardResize = false;
            splitPane.repaint();
        }
    }
    private static class Actions extends UIAction {
        private static final String NEGATIVE_INCREMENT = "negativeIncrement";
        private static final String POSITIVE_INCREMENT = "positiveIncrement";
        private static final String SELECT_MIN = "selectMin";
        private static final String SELECT_MAX = "selectMax";
        private static final String START_RESIZE = "startResize";
        private static final String TOGGLE_FOCUS = "toggleFocus";
        private static final String FOCUS_OUT_FORWARD = "focusOutForward";
        private static final String FOCUS_OUT_BACKWARD = "focusOutBackward";
        Actions(String key) {
            super(key);
        }
        public void actionPerformed(ActionEvent ev) {
            JSplitPane splitPane = (JSplitPane)ev.getSource();
            BasicSplitPaneUI ui = (BasicSplitPaneUI)BasicLookAndFeel.
                      getUIOfType(splitPane.getUI(), BasicSplitPaneUI.class);
            if (ui == null) {
                return;
            }
            String key = getName();
            if (key == NEGATIVE_INCREMENT) {
                if (ui.dividerKeyboardResize) {
                    splitPane.setDividerLocation(Math.max(
                              0, ui.getDividerLocation
                              (splitPane) - ui.getKeyboardMoveIncrement()));
                }
            }
            else if (key == POSITIVE_INCREMENT) {
                if (ui.dividerKeyboardResize) {
                    splitPane.setDividerLocation(
                        ui.getDividerLocation(splitPane) +
                        ui.getKeyboardMoveIncrement());
                }
            }
            else if (key == SELECT_MIN) {
                if (ui.dividerKeyboardResize) {
                    splitPane.setDividerLocation(0);
                }
            }
            else if (key == SELECT_MAX) {
                if (ui.dividerKeyboardResize) {
                    Insets   insets = splitPane.getInsets();
                    int      bottomI = (insets != null) ? insets.bottom : 0;
                    int      rightI = (insets != null) ? insets.right : 0;
                    if (ui.orientation == JSplitPane.VERTICAL_SPLIT) {
                        splitPane.setDividerLocation(splitPane.getHeight() -
                                                     bottomI);
                    }
                    else {
                        splitPane.setDividerLocation(splitPane.getWidth() -
                                                     rightI);
                    }
                }
            }
            else if (key == START_RESIZE) {
                if (!ui.dividerKeyboardResize) {
                    splitPane.requestFocus();
                } else {
                    JSplitPane parentSplitPane =
                        (JSplitPane)SwingUtilities.getAncestorOfClass(
                                         JSplitPane.class, splitPane);
                    if (parentSplitPane!=null) {
                        parentSplitPane.requestFocus();
                    }
                }
            }
            else if (key == TOGGLE_FOCUS) {
                toggleFocus(splitPane);
            }
            else if (key == FOCUS_OUT_FORWARD) {
                moveFocus(splitPane, 1);
            }
            else if (key == FOCUS_OUT_BACKWARD) {
                moveFocus(splitPane, -1);
            }
        }
        private void moveFocus(JSplitPane splitPane, int direction) {
            Container rootAncestor = splitPane.getFocusCycleRootAncestor();
            FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
            Component focusOn = (direction > 0) ?
                policy.getComponentAfter(rootAncestor, splitPane) :
                policy.getComponentBefore(rootAncestor, splitPane);
            HashSet<Component> focusFrom = new HashSet<Component>();
            if (splitPane.isAncestorOf(focusOn)) {
                do {
                    focusFrom.add(focusOn);
                    rootAncestor = focusOn.getFocusCycleRootAncestor();
                    policy = rootAncestor.getFocusTraversalPolicy();
                    focusOn = (direction > 0) ?
                        policy.getComponentAfter(rootAncestor, focusOn) :
                        policy.getComponentBefore(rootAncestor, focusOn);
                } while (splitPane.isAncestorOf(focusOn) &&
                         !focusFrom.contains(focusOn));
            }
            if ( focusOn!=null && !splitPane.isAncestorOf(focusOn) ) {
                focusOn.requestFocus();
            }
        }
        private void toggleFocus(JSplitPane splitPane) {
            Component left = splitPane.getLeftComponent();
            Component right = splitPane.getRightComponent();
            KeyboardFocusManager manager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component focus = manager.getFocusOwner();
            Component focusOn = getNextSide(splitPane, focus);
            if (focusOn != null) {
                if ( focus!=null &&
                     ( (SwingUtilities.isDescendingFrom(focus, left) &&
                        SwingUtilities.isDescendingFrom(focusOn, left)) ||
                       (SwingUtilities.isDescendingFrom(focus, right) &&
                        SwingUtilities.isDescendingFrom(focusOn, right)) ) ) {
                    return;
                }
                SwingUtilities2.compositeRequestFocus(focusOn);
            }
        }
        private Component getNextSide(JSplitPane splitPane, Component focus) {
            Component left = splitPane.getLeftComponent();
            Component right = splitPane.getRightComponent();
            Component next;
            if (focus!=null && SwingUtilities.isDescendingFrom(focus, left) &&
                right!=null) {
                next = getFirstAvailableComponent(right);
                if (next != null) {
                    return next;
                }
            }
            JSplitPane parentSplitPane = (JSplitPane)SwingUtilities.getAncestorOfClass(JSplitPane.class, splitPane);
            if (parentSplitPane!=null) {
                next = getNextSide(parentSplitPane, focus);
            } else {
                next = getFirstAvailableComponent(left);
                if (next == null) {
                    next = getFirstAvailableComponent(right);
                }
            }
            return next;
        }
        private Component getFirstAvailableComponent(Component c) {
            if (c!=null && c instanceof JSplitPane) {
                JSplitPane sp = (JSplitPane)c;
                Component left = getFirstAvailableComponent(sp.getLeftComponent());
                if (left != null) {
                    c = left;
                } else {
                    c = getFirstAvailableComponent(sp.getRightComponent());
                }
            }
            return c;
        }
    }
}
