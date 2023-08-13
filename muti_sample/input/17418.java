public abstract class JComponent extends Container implements Serializable,
                                              TransferHandler.HasGetTransferHandler
{
    private static final String uiClassID = "ComponentUI";
    private static final Hashtable<ObjectInputStream, ReadObjectCallback> readObjectCallbacks =
            new Hashtable<ObjectInputStream, ReadObjectCallback>(1);
    private static Set<KeyStroke> managingFocusForwardTraversalKeys;
    private static Set<KeyStroke> managingFocusBackwardTraversalKeys;
    private static final int NOT_OBSCURED = 0;
    private static final int PARTIALLY_OBSCURED = 1;
    private static final int COMPLETELY_OBSCURED = 2;
    static boolean DEBUG_GRAPHICS_LOADED;
    private static final Object INPUT_VERIFIER_SOURCE_KEY =
            new StringBuilder("InputVerifierSourceKey");
    private boolean isAlignmentXSet;
    private float alignmentX;
    private boolean isAlignmentYSet;
    private float alignmentY;
    protected transient ComponentUI ui;
    protected EventListenerList listenerList = new EventListenerList();
    private transient ArrayTable clientProperties;
    private VetoableChangeSupport vetoableChangeSupport;
    private boolean autoscrolls;
    private Border border;
    private int flags;
    private InputVerifier inputVerifier = null;
    private boolean verifyInputWhenFocusTarget = true;
    transient Component         paintingChild;
    public static final int WHEN_FOCUSED = 0;
    public static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 1;
    public static final int WHEN_IN_FOCUSED_WINDOW = 2;
    public static final int UNDEFINED_CONDITION = -1;
    private static final String KEYBOARD_BINDINGS_KEY = "_KeyboardBindings";
    private static final String WHEN_IN_FOCUSED_WINDOW_BINDINGS = "_WhenInFocusedWindow";
    public static final String TOOL_TIP_TEXT_KEY = "ToolTipText";
    private static final String NEXT_FOCUS = "nextFocus";
    private JPopupMenu popupMenu;
    private static final int IS_DOUBLE_BUFFERED                       =  0;
    private static final int ANCESTOR_USING_BUFFER                    =  1;
    private static final int IS_PAINTING_TILE                         =  2;
    private static final int IS_OPAQUE                                =  3;
    private static final int KEY_EVENTS_ENABLED                       =  4;
    private static final int FOCUS_INPUTMAP_CREATED                   =  5;
    private static final int ANCESTOR_INPUTMAP_CREATED                =  6;
    private static final int WIF_INPUTMAP_CREATED                     =  7;
    private static final int ACTIONMAP_CREATED                        =  8;
    private static final int CREATED_DOUBLE_BUFFER                    =  9;
    private static final int IS_PRINTING                              = 11;
    private static final int IS_PRINTING_ALL                          = 12;
    private static final int IS_REPAINTING                            = 13;
    private static final int WRITE_OBJ_COUNTER_FIRST                  = 14;
    private static final int RESERVED_1                               = 15;
    private static final int RESERVED_2                               = 16;
    private static final int RESERVED_3                               = 17;
    private static final int RESERVED_4                               = 18;
    private static final int RESERVED_5                               = 19;
    private static final int RESERVED_6                               = 20;
    private static final int WRITE_OBJ_COUNTER_LAST                   = 21;
    private static final int REQUEST_FOCUS_DISABLED                   = 22;
    private static final int INHERITS_POPUP_MENU                      = 23;
    private static final int OPAQUE_SET                               = 24;
    private static final int AUTOSCROLLS_SET                          = 25;
    private static final int FOCUS_TRAVERSAL_KEYS_FORWARD_SET         = 26;
    private static final int FOCUS_TRAVERSAL_KEYS_BACKWARD_SET        = 27;
    private static final int REVALIDATE_RUNNABLE_SCHEDULED            = 28;
    private static java.util.List<Rectangle> tempRectangles = new java.util.ArrayList<Rectangle>(11);
    private InputMap focusInputMap;
    private InputMap ancestorInputMap;
    private ComponentInputMap windowInputMap;
    private ActionMap actionMap;
    private static final String defaultLocale = "JComponent.defaultLocale";
    private static Component componentObtainingGraphicsFrom;
    private static Object componentObtainingGraphicsFromLock = new
            StringBuilder("componentObtainingGraphicsFrom");
    transient private Object aaTextInfo;
    static Graphics safelyGetGraphics(Component c) {
        return safelyGetGraphics(c, SwingUtilities.getRoot(c));
    }
    static Graphics safelyGetGraphics(Component c, Component root) {
        synchronized(componentObtainingGraphicsFromLock) {
            componentObtainingGraphicsFrom = root;
            Graphics g = c.getGraphics();
            componentObtainingGraphicsFrom = null;
            return g;
        }
    }
    static void getGraphicsInvoked(Component root) {
        if (!JComponent.isComponentObtainingGraphicsFrom(root)) {
            JRootPane rootPane = ((RootPaneContainer)root).getRootPane();
            if (rootPane != null) {
                rootPane.disableTrueDoubleBuffering();
            }
        }
    }
    private static boolean isComponentObtainingGraphicsFrom(Component c) {
        synchronized(componentObtainingGraphicsFromLock) {
            return (componentObtainingGraphicsFrom == c);
        }
    }
    static Set<KeyStroke> getManagingFocusForwardTraversalKeys() {
        synchronized(JComponent.class) {
            if (managingFocusForwardTraversalKeys == null) {
                managingFocusForwardTraversalKeys = new HashSet<KeyStroke>(1);
                managingFocusForwardTraversalKeys.add(
                    KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                           InputEvent.CTRL_MASK));
            }
        }
        return managingFocusForwardTraversalKeys;
    }
    static Set<KeyStroke> getManagingFocusBackwardTraversalKeys() {
        synchronized(JComponent.class) {
            if (managingFocusBackwardTraversalKeys == null) {
                managingFocusBackwardTraversalKeys = new HashSet<KeyStroke>(1);
                managingFocusBackwardTraversalKeys.add(
                    KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                           InputEvent.SHIFT_MASK |
                                           InputEvent.CTRL_MASK));
            }
        }
        return managingFocusBackwardTraversalKeys;
    }
    private static Rectangle fetchRectangle() {
        synchronized(tempRectangles) {
            Rectangle rect;
            int size = tempRectangles.size();
            if (size > 0) {
                rect = tempRectangles.remove(size - 1);
            }
            else {
                rect = new Rectangle(0, 0, 0, 0);
            }
            return rect;
        }
    }
    private static void recycleRectangle(Rectangle rect) {
        synchronized(tempRectangles) {
            tempRectangles.add(rect);
        }
    }
    public void setInheritsPopupMenu(boolean value) {
        boolean oldValue = getFlag(INHERITS_POPUP_MENU);
        setFlag(INHERITS_POPUP_MENU, value);
        firePropertyChange("inheritsPopupMenu", oldValue, value);
    }
    public boolean getInheritsPopupMenu() {
        return getFlag(INHERITS_POPUP_MENU);
    }
    public void setComponentPopupMenu(JPopupMenu popup) {
        if(popup != null) {
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }
        JPopupMenu oldPopup = this.popupMenu;
        this.popupMenu = popup;
        firePropertyChange("componentPopupMenu", oldPopup, popup);
    }
    public JPopupMenu getComponentPopupMenu() {
        if(!getInheritsPopupMenu()) {
            return popupMenu;
        }
        if(popupMenu == null) {
            Container parent = getParent();
            while (parent != null) {
                if(parent instanceof JComponent) {
                    return ((JComponent)parent).getComponentPopupMenu();
                }
                if(parent instanceof Window ||
                   parent instanceof Applet) {
                    break;
                }
                parent = parent.getParent();
            }
            return null;
        }
        return popupMenu;
    }
    public JComponent() {
        super();
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        if (isManagingFocus()) {
            LookAndFeel.installProperty(this,
                                        "focusTraversalKeysForward",
                                  getManagingFocusForwardTraversalKeys());
            LookAndFeel.installProperty(this,
                                        "focusTraversalKeysBackward",
                                  getManagingFocusBackwardTraversalKeys());
        }
        super.setLocale( JComponent.getDefaultLocale() );
    }
    public void updateUI() {}
    protected void setUI(ComponentUI newUI) {
        uninstallUIAndProperties();
        aaTextInfo =
            UIManager.getDefaults().get(SwingUtilities2.AA_TEXT_PROPERTY_KEY);
        ComponentUI oldUI = ui;
        ui = newUI;
        if (ui != null) {
            ui.installUI(this);
        }
        firePropertyChange("UI", oldUI, newUI);
        revalidate();
        repaint();
    }
    private void uninstallUIAndProperties() {
        if (ui != null) {
            ui.uninstallUI(this);
            if (clientProperties != null) {
                synchronized(clientProperties) {
                    Object[] clientPropertyKeys =
                        clientProperties.getKeys(null);
                    if (clientPropertyKeys != null) {
                        for (Object key : clientPropertyKeys) {
                            if (key instanceof UIClientPropertyKey) {
                                putClientProperty(key, null);
                            }
                        }
                    }
                }
            }
        }
    }
    public String getUIClassID() {
        return uiClassID;
    }
    protected Graphics getComponentGraphics(Graphics g) {
        Graphics componentGraphics = g;
        if (ui != null && DEBUG_GRAPHICS_LOADED) {
            if ((DebugGraphics.debugComponentCount() != 0) &&
                    (shouldDebugGraphics() != 0) &&
                    !(g instanceof DebugGraphics)) {
                componentGraphics = new DebugGraphics(g,this);
            }
        }
        componentGraphics.setColor(getForeground());
        componentGraphics.setFont(getFont());
        return componentGraphics;
    }
    protected void paintComponent(Graphics g) {
        if (ui != null) {
            Graphics scratchGraphics = (g == null) ? null : g.create();
            try {
                ui.update(scratchGraphics, this);
            }
            finally {
                scratchGraphics.dispose();
            }
        }
    }
    protected void paintChildren(Graphics g) {
        Graphics sg = g;
        synchronized(getTreeLock()) {
            int i = getComponentCount() - 1;
            if (i < 0) {
                return;
            }
            if (paintingChild != null &&
                (paintingChild instanceof JComponent) &&
                paintingChild.isOpaque()) {
                for (; i >= 0; i--) {
                    if (getComponent(i) == paintingChild){
                        break;
                    }
                }
            }
            Rectangle tmpRect = fetchRectangle();
            boolean checkSiblings = (!isOptimizedDrawingEnabled() &&
                                     checkIfChildObscuredBySibling());
            Rectangle clipBounds = null;
            if (checkSiblings) {
                clipBounds = sg.getClipBounds();
                if (clipBounds == null) {
                    clipBounds = new Rectangle(0, 0, getWidth(),
                                               getHeight());
                }
            }
            boolean printing = getFlag(IS_PRINTING);
            final Window window = SwingUtilities.getWindowAncestor(this);
            final boolean isWindowOpaque = window == null || window.isOpaque();
            for (; i >= 0 ; i--) {
                Component comp = getComponent(i);
                if (comp == null) {
                    continue;
                }
                final boolean isJComponent = comp instanceof JComponent;
                if ((!isWindowOpaque || isJComponent ||
                            isLightweightComponent(comp)) && comp.isVisible())
                {
                    Rectangle cr;
                    cr = comp.getBounds(tmpRect);
                    boolean hitClip = g.hitClip(cr.x, cr.y, cr.width,
                                                cr.height);
                    if (hitClip) {
                        if (checkSiblings && i > 0) {
                            int x = cr.x;
                            int y = cr.y;
                            int width = cr.width;
                            int height = cr.height;
                            SwingUtilities.computeIntersection
                                (clipBounds.x, clipBounds.y,
                                 clipBounds.width, clipBounds.height, cr);
                            if(getObscuredState(i, cr.x, cr.y, cr.width,
                                          cr.height) == COMPLETELY_OBSCURED) {
                                continue;
                            }
                            cr.x = x;
                            cr.y = y;
                            cr.width = width;
                            cr.height = height;
                        }
                        Graphics cg = sg.create(cr.x, cr.y, cr.width,
                                                cr.height);
                        cg.setColor(comp.getForeground());
                        cg.setFont(comp.getFont());
                        boolean shouldSetFlagBack = false;
                        try {
                            if(isJComponent) {
                                if(getFlag(ANCESTOR_USING_BUFFER)) {
                                    ((JComponent)comp).setFlag(
                                                 ANCESTOR_USING_BUFFER,true);
                                    shouldSetFlagBack = true;
                                }
                                if(getFlag(IS_PAINTING_TILE)) {
                                    ((JComponent)comp).setFlag(
                                                 IS_PAINTING_TILE,true);
                                    shouldSetFlagBack = true;
                                }
                                if(!printing) {
                                    comp.paint(cg);
                                }
                                else {
                                    if (!getFlag(IS_PRINTING_ALL)) {
                                        comp.print(cg);
                                    }
                                    else {
                                        comp.printAll(cg);
                                    }
                                }
                            } else {
                                if (!printing) {
                                    comp.paint(cg);
                                }
                                else {
                                    if (!getFlag(IS_PRINTING_ALL)) {
                                        comp.print(cg);
                                    }
                                    else {
                                        comp.printAll(cg);
                                    }
                                }
                            }
                        } finally {
                            cg.dispose();
                            if(shouldSetFlagBack) {
                                ((JComponent)comp).setFlag(
                                             ANCESTOR_USING_BUFFER,false);
                                ((JComponent)comp).setFlag(
                                             IS_PAINTING_TILE,false);
                            }
                        }
                    }
                }
            }
            recycleRectangle(tmpRect);
        }
    }
    protected void paintBorder(Graphics g) {
        Border border = getBorder();
        if (border != null) {
            border.paintBorder(this, g, 0, 0, getWidth(), getHeight());
        }
    }
    public void update(Graphics g) {
        paint(g);
    }
    public void paint(Graphics g) {
        boolean shouldClearPaintFlags = false;
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }
        Graphics componentGraphics = getComponentGraphics(g);
        Graphics co = componentGraphics.create();
        try {
            RepaintManager repaintManager = RepaintManager.currentManager(this);
            Rectangle clipRect = co.getClipBounds();
            int clipX;
            int clipY;
            int clipW;
            int clipH;
            if (clipRect == null) {
                clipX = clipY = 0;
                clipW = getWidth();
                clipH = getHeight();
            }
            else {
                clipX = clipRect.x;
                clipY = clipRect.y;
                clipW = clipRect.width;
                clipH = clipRect.height;
            }
            if(clipW > getWidth()) {
                clipW = getWidth();
            }
            if(clipH > getHeight()) {
                clipH = getHeight();
            }
            if(getParent() != null && !(getParent() instanceof JComponent)) {
                adjustPaintFlags();
                shouldClearPaintFlags = true;
            }
            int bw,bh;
            boolean printing = getFlag(IS_PRINTING);
            if (!printing && repaintManager.isDoubleBufferingEnabled() &&
                !getFlag(ANCESTOR_USING_BUFFER) && isDoubleBuffered() &&
                (getFlag(IS_REPAINTING) || repaintManager.isPainting()))
            {
                repaintManager.beginPaint();
                try {
                    repaintManager.paint(this, this, co, clipX, clipY, clipW,
                                         clipH);
                } finally {
                    repaintManager.endPaint();
                }
            }
            else {
                if (clipRect == null) {
                    co.setClip(clipX, clipY, clipW, clipH);
                }
                if (!rectangleIsObscured(clipX,clipY,clipW,clipH)) {
                    if (!printing) {
                        paintComponent(co);
                        paintBorder(co);
                    }
                    else {
                        printComponent(co);
                        printBorder(co);
                    }
                }
                if (!printing) {
                    paintChildren(co);
                }
                else {
                    printChildren(co);
                }
            }
        } finally {
            co.dispose();
            if(shouldClearPaintFlags) {
                setFlag(ANCESTOR_USING_BUFFER,false);
                setFlag(IS_PAINTING_TILE,false);
                setFlag(IS_PRINTING,false);
                setFlag(IS_PRINTING_ALL,false);
            }
        }
    }
    void paintForceDoubleBuffered(Graphics g) {
        RepaintManager rm = RepaintManager.currentManager(this);
        Rectangle clip = g.getClipBounds();
        rm.beginPaint();
        setFlag(IS_REPAINTING, true);
        try {
            rm.paint(this, this, g, clip.x, clip.y, clip.width, clip.height);
        } finally {
            rm.endPaint();
            setFlag(IS_REPAINTING, false);
        }
    }
    boolean isPainting() {
        Container component = this;
        while (component != null) {
            if (component instanceof JComponent &&
                   ((JComponent)component).getFlag(ANCESTOR_USING_BUFFER)) {
                return true;
            }
            component = component.getParent();
        }
        return false;
    }
    private void adjustPaintFlags() {
        JComponent jparent;
        Container parent;
        for(parent = getParent() ; parent != null ; parent =
            parent.getParent()) {
            if(parent instanceof JComponent) {
                jparent = (JComponent) parent;
                if(jparent.getFlag(ANCESTOR_USING_BUFFER))
                  setFlag(ANCESTOR_USING_BUFFER, true);
                if(jparent.getFlag(IS_PAINTING_TILE))
                  setFlag(IS_PAINTING_TILE, true);
                if(jparent.getFlag(IS_PRINTING))
                  setFlag(IS_PRINTING, true);
                if(jparent.getFlag(IS_PRINTING_ALL))
                  setFlag(IS_PRINTING_ALL, true);
                break;
            }
        }
    }
    public void printAll(Graphics g) {
        setFlag(IS_PRINTING_ALL, true);
        try {
            print(g);
        }
        finally {
            setFlag(IS_PRINTING_ALL, false);
        }
    }
    public void print(Graphics g) {
        setFlag(IS_PRINTING, true);
        firePropertyChange("paintingForPrint", false, true);
        try {
            paint(g);
        }
        finally {
            setFlag(IS_PRINTING, false);
            firePropertyChange("paintingForPrint", true, false);
        }
    }
    protected void printComponent(Graphics g) {
        paintComponent(g);
    }
    protected void printChildren(Graphics g) {
        paintChildren(g);
    }
    protected void printBorder(Graphics g) {
        paintBorder(g);
    }
    public boolean isPaintingTile() {
        return getFlag(IS_PAINTING_TILE);
    }
    public final boolean isPaintingForPrint() {
        return getFlag(IS_PRINTING);
    }
    @Deprecated
    public boolean isManagingFocus() {
        return false;
    }
    private void registerNextFocusableComponent() {
        registerNextFocusableComponent(getNextFocusableComponent());
    }
    private void registerNextFocusableComponent(Component
                                                nextFocusableComponent) {
        if (nextFocusableComponent == null) {
            return;
        }
        Container nearestRoot =
            (isFocusCycleRoot()) ? this : getFocusCycleRootAncestor();
        FocusTraversalPolicy policy = nearestRoot.getFocusTraversalPolicy();
        if (!(policy instanceof LegacyGlueFocusTraversalPolicy)) {
            policy = new LegacyGlueFocusTraversalPolicy(policy);
            nearestRoot.setFocusTraversalPolicy(policy);
        }
        ((LegacyGlueFocusTraversalPolicy)policy).
            setNextFocusableComponent(this, nextFocusableComponent);
    }
    private void deregisterNextFocusableComponent() {
        Component nextFocusableComponent = getNextFocusableComponent();
        if (nextFocusableComponent == null) {
            return;
        }
        Container nearestRoot =
            (isFocusCycleRoot()) ? this : getFocusCycleRootAncestor();
        if (nearestRoot == null) {
            return;
        }
        FocusTraversalPolicy policy = nearestRoot.getFocusTraversalPolicy();
        if (policy instanceof LegacyGlueFocusTraversalPolicy) {
            ((LegacyGlueFocusTraversalPolicy)policy).
                unsetNextFocusableComponent(this, nextFocusableComponent);
        }
    }
    @Deprecated
    public void setNextFocusableComponent(Component aComponent) {
        boolean displayable = isDisplayable();
        if (displayable) {
            deregisterNextFocusableComponent();
        }
        putClientProperty(NEXT_FOCUS, aComponent);
        if (displayable) {
            registerNextFocusableComponent(aComponent);
        }
    }
    @Deprecated
    public Component getNextFocusableComponent() {
        return (Component)getClientProperty(NEXT_FOCUS);
    }
    public void setRequestFocusEnabled(boolean requestFocusEnabled) {
        setFlag(REQUEST_FOCUS_DISABLED, !requestFocusEnabled);
    }
    public boolean isRequestFocusEnabled() {
        return !getFlag(REQUEST_FOCUS_DISABLED);
    }
    public void requestFocus() {
        super.requestFocus();
    }
    public boolean requestFocus(boolean temporary) {
        return super.requestFocus(temporary);
    }
    public boolean requestFocusInWindow() {
        return super.requestFocusInWindow();
    }
    protected boolean requestFocusInWindow(boolean temporary) {
        return super.requestFocusInWindow(temporary);
    }
    public void grabFocus() {
        requestFocus();
    }
    public void setVerifyInputWhenFocusTarget(boolean
                                              verifyInputWhenFocusTarget) {
        boolean oldVerifyInputWhenFocusTarget =
            this.verifyInputWhenFocusTarget;
        this.verifyInputWhenFocusTarget = verifyInputWhenFocusTarget;
        firePropertyChange("verifyInputWhenFocusTarget",
                           oldVerifyInputWhenFocusTarget,
                           verifyInputWhenFocusTarget);
    }
    public boolean getVerifyInputWhenFocusTarget() {
        return verifyInputWhenFocusTarget;
    }
    public FontMetrics getFontMetrics(Font font) {
        return SwingUtilities2.getFontMetrics(this, font);
    }
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
    }
    @Transient
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension size = null;
        if (ui != null) {
            size = ui.getPreferredSize(this);
        }
        return (size != null) ? size : super.getPreferredSize();
    }
    public void setMaximumSize(Dimension maximumSize) {
        super.setMaximumSize(maximumSize);
    }
    @Transient
    public Dimension getMaximumSize() {
        if (isMaximumSizeSet()) {
            return super.getMaximumSize();
        }
        Dimension size = null;
        if (ui != null) {
            size = ui.getMaximumSize(this);
        }
        return (size != null) ? size : super.getMaximumSize();
    }
    public void setMinimumSize(Dimension minimumSize) {
        super.setMinimumSize(minimumSize);
    }
    @Transient
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) {
            return super.getMinimumSize();
        }
        Dimension size = null;
        if (ui != null) {
            size = ui.getMinimumSize(this);
        }
        return (size != null) ? size : super.getMinimumSize();
    }
    public boolean contains(int x, int y) {
        return (ui != null) ? ui.contains(this, x, y) : super.contains(x, y);
    }
    public void setBorder(Border border) {
        Border         oldBorder = this.border;
        this.border = border;
        firePropertyChange("border", oldBorder, border);
        if (border != oldBorder) {
            if (border == null || oldBorder == null ||
                !(border.getBorderInsets(this).equals(oldBorder.getBorderInsets(this)))) {
                revalidate();
            }
            repaint();
        }
    }
    public Border getBorder() {
        return border;
    }
    public Insets getInsets() {
        if (border != null) {
            return border.getBorderInsets(this);
        }
        return super.getInsets();
    }
    public Insets getInsets(Insets insets) {
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }
        if (border != null) {
            if (border instanceof AbstractBorder) {
                return ((AbstractBorder)border).getBorderInsets(this, insets);
            } else {
                return border.getBorderInsets(this);
            }
        } else {
            insets.left = insets.top = insets.right = insets.bottom = 0;
            return insets;
        }
    }
    public float getAlignmentY() {
        if (isAlignmentYSet) {
            return alignmentY;
        }
        return super.getAlignmentY();
    }
    public void setAlignmentY(float alignmentY) {
        this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;
        isAlignmentYSet = true;
    }
    public float getAlignmentX() {
        if (isAlignmentXSet) {
            return alignmentX;
        }
        return super.getAlignmentX();
    }
    public void setAlignmentX(float alignmentX) {
        this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
        isAlignmentXSet = true;
    }
    public void setInputVerifier(InputVerifier inputVerifier) {
        InputVerifier oldInputVerifier = (InputVerifier)getClientProperty(
                                         JComponent_INPUT_VERIFIER);
        putClientProperty(JComponent_INPUT_VERIFIER, inputVerifier);
        firePropertyChange("inputVerifier", oldInputVerifier, inputVerifier);
    }
    public InputVerifier getInputVerifier() {
        return (InputVerifier)getClientProperty(JComponent_INPUT_VERIFIER);
    }
    public Graphics getGraphics() {
        if (DEBUG_GRAPHICS_LOADED && shouldDebugGraphics() != 0) {
            DebugGraphics graphics = new DebugGraphics(super.getGraphics(),
                                                       this);
            return graphics;
        }
        return super.getGraphics();
    }
    public void setDebugGraphicsOptions(int debugOptions) {
        DebugGraphics.setDebugOptions(this, debugOptions);
    }
    public int getDebugGraphicsOptions() {
        return DebugGraphics.getDebugOptions(this);
    }
    int shouldDebugGraphics() {
        return DebugGraphics.shouldComponentDebug(this);
    }
    public void registerKeyboardAction(ActionListener anAction,String aCommand,KeyStroke aKeyStroke,int aCondition) {
        InputMap inputMap = getInputMap(aCondition, true);
        if (inputMap != null) {
            ActionMap actionMap = getActionMap(true);
            ActionStandin action = new ActionStandin(anAction, aCommand);
            inputMap.put(aKeyStroke, action);
            if (actionMap != null) {
                actionMap.put(action, action);
            }
        }
    }
    private void registerWithKeyboardManager(boolean onlyIfNew) {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW, false);
        KeyStroke[] strokes;
        Hashtable<KeyStroke, KeyStroke> registered = (Hashtable)getClientProperty
                                (WHEN_IN_FOCUSED_WINDOW_BINDINGS);
        if (inputMap != null) {
            strokes = inputMap.allKeys();
            if (strokes != null) {
                for (int counter = strokes.length - 1; counter >= 0;
                     counter--) {
                    if (!onlyIfNew || registered == null ||
                        registered.get(strokes[counter]) == null) {
                        registerWithKeyboardManager(strokes[counter]);
                    }
                    if (registered != null) {
                        registered.remove(strokes[counter]);
                    }
                }
            }
        }
        else {
            strokes = null;
        }
        if (registered != null && registered.size() > 0) {
            Enumeration<KeyStroke> keys = registered.keys();
            while (keys.hasMoreElements()) {
                KeyStroke ks = keys.nextElement();
                unregisterWithKeyboardManager(ks);
            }
            registered.clear();
        }
        if (strokes != null && strokes.length > 0) {
            if (registered == null) {
                registered = new Hashtable<KeyStroke, KeyStroke>(strokes.length);
                putClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS, registered);
            }
            for (int counter = strokes.length - 1; counter >= 0; counter--) {
                registered.put(strokes[counter], strokes[counter]);
            }
        }
        else {
            putClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS, null);
        }
    }
    private void unregisterWithKeyboardManager() {
        Hashtable registered = (Hashtable)getClientProperty
                                (WHEN_IN_FOCUSED_WINDOW_BINDINGS);
        if (registered != null && registered.size() > 0) {
            Enumeration keys = registered.keys();
            while (keys.hasMoreElements()) {
                KeyStroke ks = (KeyStroke)keys.nextElement();
                unregisterWithKeyboardManager(ks);
            }
        }
        putClientProperty(WHEN_IN_FOCUSED_WINDOW_BINDINGS, null);
    }
    void componentInputMapChanged(ComponentInputMap inputMap) {
        InputMap km = getInputMap(WHEN_IN_FOCUSED_WINDOW, false);
        while (km != inputMap && km != null) {
            km = km.getParent();
        }
        if (km != null) {
            registerWithKeyboardManager(false);
        }
    }
    private void registerWithKeyboardManager(KeyStroke aKeyStroke) {
        KeyboardManager.getCurrentManager().registerKeyStroke(aKeyStroke,this);
    }
    private void unregisterWithKeyboardManager(KeyStroke aKeyStroke) {
        KeyboardManager.getCurrentManager().unregisterKeyStroke(aKeyStroke,
                                                                this);
    }
    public void registerKeyboardAction(ActionListener anAction,KeyStroke aKeyStroke,int aCondition) {
        registerKeyboardAction(anAction,null,aKeyStroke,aCondition);
    }
    public void unregisterKeyboardAction(KeyStroke aKeyStroke) {
        ActionMap am = getActionMap(false);
        for (int counter = 0; counter < 3; counter++) {
            InputMap km = getInputMap(counter, false);
            if (km != null) {
                Object actionID = km.get(aKeyStroke);
                if (am != null && actionID != null) {
                    am.remove(actionID);
                }
                km.remove(aKeyStroke);
            }
        }
    }
    public KeyStroke[] getRegisteredKeyStrokes() {
        int[] counts = new int[3];
        KeyStroke[][] strokes = new KeyStroke[3][];
        for (int counter = 0; counter < 3; counter++) {
            InputMap km = getInputMap(counter, false);
            strokes[counter] = (km != null) ? km.allKeys() : null;
            counts[counter] = (strokes[counter] != null) ?
                               strokes[counter].length : 0;
        }
        KeyStroke[] retValue = new KeyStroke[counts[0] + counts[1] +
                                            counts[2]];
        for (int counter = 0, last = 0; counter < 3; counter++) {
            if (counts[counter] > 0) {
                System.arraycopy(strokes[counter], 0, retValue, last,
                                 counts[counter]);
                last += counts[counter];
            }
        }
        return retValue;
    }
    public int getConditionForKeyStroke(KeyStroke aKeyStroke) {
        for (int counter = 0; counter < 3; counter++) {
            InputMap inputMap = getInputMap(counter, false);
            if (inputMap != null && inputMap.get(aKeyStroke) != null) {
                return counter;
            }
        }
        return UNDEFINED_CONDITION;
    }
    public ActionListener getActionForKeyStroke(KeyStroke aKeyStroke) {
        ActionMap am = getActionMap(false);
        if (am == null) {
            return null;
        }
        for (int counter = 0; counter < 3; counter++) {
            InputMap inputMap = getInputMap(counter, false);
            if (inputMap != null) {
                Object actionBinding = inputMap.get(aKeyStroke);
                if (actionBinding != null) {
                    Action action = am.get(actionBinding);
                    if (action instanceof ActionStandin) {
                        return ((ActionStandin)action).actionListener;
                    }
                    return action;
                }
            }
        }
        return null;
    }
    public void resetKeyboardActions() {
        for (int counter = 0; counter < 3; counter++) {
            InputMap inputMap = getInputMap(counter, false);
            if (inputMap != null) {
                inputMap.clear();
            }
        }
        ActionMap am = getActionMap(false);
        if (am != null) {
            am.clear();
        }
    }
    public final void setInputMap(int condition, InputMap map) {
        switch (condition) {
        case WHEN_IN_FOCUSED_WINDOW:
            if (map != null && !(map instanceof ComponentInputMap)) {
                throw new IllegalArgumentException("WHEN_IN_FOCUSED_WINDOW InputMaps must be of type ComponentInputMap");
            }
            windowInputMap = (ComponentInputMap)map;
            setFlag(WIF_INPUTMAP_CREATED, true);
            registerWithKeyboardManager(false);
            break;
        case WHEN_ANCESTOR_OF_FOCUSED_COMPONENT:
            ancestorInputMap = map;
            setFlag(ANCESTOR_INPUTMAP_CREATED, true);
            break;
        case WHEN_FOCUSED:
            focusInputMap = map;
            setFlag(FOCUS_INPUTMAP_CREATED, true);
            break;
        default:
            throw new IllegalArgumentException("condition must be one of JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED or JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT");
        }
    }
    public final InputMap getInputMap(int condition) {
        return getInputMap(condition, true);
    }
    public final InputMap getInputMap() {
        return getInputMap(WHEN_FOCUSED, true);
    }
    public final void setActionMap(ActionMap am) {
        actionMap = am;
        setFlag(ACTIONMAP_CREATED, true);
    }
    public final ActionMap getActionMap() {
        return getActionMap(true);
    }
    final InputMap getInputMap(int condition, boolean create) {
        switch (condition) {
        case WHEN_FOCUSED:
            if (getFlag(FOCUS_INPUTMAP_CREATED)) {
                return focusInputMap;
            }
            if (create) {
                InputMap km = new InputMap();
                setInputMap(condition, km);
                return km;
            }
            break;
        case WHEN_ANCESTOR_OF_FOCUSED_COMPONENT:
            if (getFlag(ANCESTOR_INPUTMAP_CREATED)) {
                return ancestorInputMap;
            }
            if (create) {
                InputMap km = new InputMap();
                setInputMap(condition, km);
                return km;
            }
            break;
        case WHEN_IN_FOCUSED_WINDOW:
            if (getFlag(WIF_INPUTMAP_CREATED)) {
                return windowInputMap;
            }
            if (create) {
                ComponentInputMap km = new ComponentInputMap(this);
                setInputMap(condition, km);
                return km;
            }
            break;
        default:
            throw new IllegalArgumentException("condition must be one of JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED or JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT");
        }
        return null;
    }
    final ActionMap getActionMap(boolean create) {
        if (getFlag(ACTIONMAP_CREATED)) {
            return actionMap;
        }
        if (create) {
            ActionMap am = new ActionMap();
            setActionMap(am);
            return am;
        }
        return null;
    }
    public int getBaseline(int width, int height) {
        super.getBaseline(width, height);
        if (ui != null) {
            return ui.getBaseline(this, width, height);
        }
        return -1;
    }
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        if (ui != null) {
            return ui.getBaselineResizeBehavior(this);
        }
        return BaselineResizeBehavior.OTHER;
    }
    @Deprecated
    public boolean requestDefaultFocus() {
        Container nearestRoot =
            (isFocusCycleRoot()) ? this : getFocusCycleRootAncestor();
        if (nearestRoot == null) {
            return false;
        }
        Component comp = nearestRoot.getFocusTraversalPolicy().
            getDefaultComponent(nearestRoot);
        if (comp != null) {
            comp.requestFocus();
            return true;
        } else {
            return false;
        }
    }
    public void setVisible(boolean aFlag) {
        if(aFlag != isVisible()) {
            super.setVisible(aFlag);
            Container parent = getParent();
            if(parent != null) {
                Rectangle r = getBounds();
                parent.repaint(r.x,r.y,r.width,r.height);
            }
            revalidate();
        }
    }
    public void setEnabled(boolean enabled) {
        boolean oldEnabled = isEnabled();
        super.setEnabled(enabled);
        firePropertyChange("enabled", oldEnabled, enabled);
        if (enabled != oldEnabled) {
            repaint();
        }
    }
    public void setForeground(Color fg) {
        Color oldFg = getForeground();
        super.setForeground(fg);
        if ((oldFg != null) ? !oldFg.equals(fg) : ((fg != null) && !fg.equals(oldFg))) {
            repaint();
        }
    }
    public void setBackground(Color bg) {
        Color oldBg = getBackground();
        super.setBackground(bg);
        if ((oldBg != null) ? !oldBg.equals(bg) : ((bg != null) && !bg.equals(oldBg))) {
            repaint();
        }
    }
    public void setFont(Font font) {
        Font oldFont = getFont();
        super.setFont(font);
        if (font != oldFont) {
            revalidate();
            repaint();
        }
    }
    static public Locale getDefaultLocale() {
        Locale l = (Locale) SwingUtilities.appContextGet(defaultLocale);
        if( l == null ) {
            l = Locale.getDefault();
            JComponent.setDefaultLocale( l );
        }
        return l;
    }
    static public void setDefaultLocale( Locale l ) {
        SwingUtilities.appContextPut(defaultLocale, l);
    }
    protected void processComponentKeyEvent(KeyEvent e) {
    }
    protected void processKeyEvent(KeyEvent e) {
      boolean result;
      boolean shouldProcessKey;
      super.processKeyEvent(e);
      if (! e.isConsumed()) {
          processComponentKeyEvent(e);
      }
      shouldProcessKey = KeyboardState.shouldProcess(e);
      if(e.isConsumed()) {
        return;
      }
      if (shouldProcessKey && processKeyBindings(e, e.getID() ==
                                                 KeyEvent.KEY_PRESSED)) {
          e.consume();
      }
    }
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                        int condition, boolean pressed) {
        InputMap map = getInputMap(condition, false);
        ActionMap am = getActionMap(false);
        if(map != null && am != null && isEnabled()) {
            Object binding = map.get(ks);
            Action action = (binding == null) ? null : am.get(binding);
            if (action != null) {
                return SwingUtilities.notifyAction(action, ks, e, this,
                                                   e.getModifiers());
            }
        }
        return false;
    }
    boolean processKeyBindings(KeyEvent e, boolean pressed) {
      if (!SwingUtilities.isValidKeyEventForKeyBindings(e)) {
          return false;
      }
      KeyStroke ks;
      KeyStroke ksE = null;
      if (e.getID() == KeyEvent.KEY_TYPED) {
          ks = KeyStroke.getKeyStroke(e.getKeyChar());
      }
      else {
          ks = KeyStroke.getKeyStroke(e.getKeyCode(),e.getModifiers(),
                                    (pressed ? false:true));
          if (e.getKeyCode() != e.getExtendedKeyCode()) {
              ksE = KeyStroke.getKeyStroke(e.getExtendedKeyCode(),e.getModifiers(),
                                    (pressed ? false:true));
          }
      }
      if(ksE != null && processKeyBinding(ksE, e, WHEN_FOCUSED, pressed)) {
          return true;
      }
      if(processKeyBinding(ks, e, WHEN_FOCUSED, pressed))
          return true;
      Container parent = this;
      while (parent != null && !(parent instanceof Window) &&
             !(parent instanceof Applet)) {
          if(parent instanceof JComponent) {
              if(ksE != null && ((JComponent)parent).processKeyBinding(ksE, e,
                               WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, pressed))
                  return true;
              if(((JComponent)parent).processKeyBinding(ks, e,
                               WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, pressed))
                  return true;
          }
          if ((parent instanceof JInternalFrame) &&
              JComponent.processKeyBindingsForAllComponents(e,parent,pressed)){
              return true;
          }
          parent = parent.getParent();
      }
      if(parent != null) {
        return JComponent.processKeyBindingsForAllComponents(e,parent,pressed);
      }
      return false;
    }
    static boolean processKeyBindingsForAllComponents(KeyEvent e,
                                      Container container, boolean pressed) {
        while (true) {
            if (KeyboardManager.getCurrentManager().fireKeyboardAction(
                                e, pressed, container)) {
                return true;
            }
            if (container instanceof Popup.HeavyWeightWindow) {
                container = ((Window)container).getOwner();
            }
            else {
                return false;
            }
        }
    }
    public void setToolTipText(String text) {
        String oldText = getToolTipText();
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        if (text != null) {
            if (oldText == null) {
                toolTipManager.registerComponent(this);
            }
        } else {
            toolTipManager.unregisterComponent(this);
        }
    }
    public String getToolTipText() {
        return (String)getClientProperty(TOOL_TIP_TEXT_KEY);
    }
    public String getToolTipText(MouseEvent event) {
        return getToolTipText();
    }
    public Point getToolTipLocation(MouseEvent event) {
        return null;
    }
    public Point getPopupLocation(MouseEvent event) {
        return null;
    }
    public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        tip.setComponent(this);
        return tip;
    }
    public void scrollRectToVisible(Rectangle aRect) {
        Container parent;
        int dx = getX(), dy = getY();
        for (parent = getParent();
                 !(parent == null) &&
                 !(parent instanceof JComponent) &&
                 !(parent instanceof CellRendererPane);
             parent = parent.getParent()) {
             Rectangle bounds = parent.getBounds();
             dx += bounds.x;
             dy += bounds.y;
        }
        if (!(parent == null) && !(parent instanceof CellRendererPane)) {
            aRect.x += dx;
            aRect.y += dy;
            ((JComponent)parent).scrollRectToVisible(aRect);
            aRect.x -= dx;
            aRect.y -= dy;
        }
    }
    public void setAutoscrolls(boolean autoscrolls) {
        setFlag(AUTOSCROLLS_SET, true);
        if (this.autoscrolls != autoscrolls) {
            this.autoscrolls = autoscrolls;
            if (autoscrolls) {
                enableEvents(AWTEvent.MOUSE_EVENT_MASK);
                enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            }
            else {
                Autoscroller.stop(this);
            }
        }
    }
    public boolean getAutoscrolls() {
        return autoscrolls;
    }
    public void setTransferHandler(TransferHandler newHandler) {
        TransferHandler oldHandler = (TransferHandler)getClientProperty(
                                      JComponent_TRANSFER_HANDLER);
        putClientProperty(JComponent_TRANSFER_HANDLER, newHandler);
        SwingUtilities.installSwingDropTargetAsNecessary(this, newHandler);
        firePropertyChange("transferHandler", oldHandler, newHandler);
    }
    public TransferHandler getTransferHandler() {
        return (TransferHandler)getClientProperty(JComponent_TRANSFER_HANDLER);
    }
    TransferHandler.DropLocation dropLocationForPoint(Point p) {
        return null;
    }
    Object setDropLocation(TransferHandler.DropLocation location,
                           Object state,
                           boolean forDrop) {
        return null;
    }
    void dndDone() {
    }
    protected void processMouseEvent(MouseEvent e) {
        if (autoscrolls && e.getID() == MouseEvent.MOUSE_RELEASED) {
            Autoscroller.stop(this);
        }
        super.processMouseEvent(e);
    }
    protected void processMouseMotionEvent(MouseEvent e) {
        boolean dispatch = true;
        if (autoscrolls && e.getID() == MouseEvent.MOUSE_DRAGGED) {
            dispatch = !Autoscroller.isRunning(this);
            Autoscroller.processMouseDragged(e);
        }
        if (dispatch) {
            super.processMouseMotionEvent(e);
        }
    }
    void superProcessMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
    }
    void setCreatedDoubleBuffer(boolean newValue) {
        setFlag(CREATED_DOUBLE_BUFFER, newValue);
    }
    boolean getCreatedDoubleBuffer() {
        return getFlag(CREATED_DOUBLE_BUFFER);
    }
    final class ActionStandin implements Action {
        private final ActionListener actionListener;
        private final String command;
        private final Action action;
        ActionStandin(ActionListener actionListener, String command) {
            this.actionListener = actionListener;
            if (actionListener instanceof Action) {
                this.action = (Action)actionListener;
            }
            else {
                this.action = null;
            }
            this.command = command;
        }
        public Object getValue(String key) {
            if (key != null) {
                if (key.equals(Action.ACTION_COMMAND_KEY)) {
                    return command;
                }
                if (action != null) {
                    return action.getValue(key);
                }
                if (key.equals(NAME)) {
                    return "ActionStandin";
                }
            }
            return null;
        }
        public boolean isEnabled() {
            if (actionListener == null) {
                return false;
            }
            if (action == null) {
                return true;
            }
            return action.isEnabled();
        }
        public void actionPerformed(ActionEvent ae) {
            if (actionListener != null) {
                actionListener.actionPerformed(ae);
            }
        }
        public void putValue(String key, Object value) {}
        public void setEnabled(boolean b) { }
        public void addPropertyChangeListener
                    (PropertyChangeListener listener) {}
        public void removePropertyChangeListener
                          (PropertyChangeListener listener) {}
    }
    static final class IntVector {
        int array[] = null;
        int count = 0;
        int capacity = 0;
        int size() {
            return count;
        }
        int elementAt(int index) {
            return array[index];
        }
        void addElement(int value) {
            if (count == capacity) {
                capacity = (capacity + 2) * 2;
                int[] newarray = new int[capacity];
                if (count > 0) {
                    System.arraycopy(array, 0, newarray, 0, count);
                }
                array = newarray;
            }
            array[count++] = value;
        }
        void setElementAt(int value, int index) {
            array[index] = value;
        }
    }
    static class KeyboardState implements Serializable {
        private static final Object keyCodesKey =
            JComponent.KeyboardState.class;
        static IntVector getKeyCodeArray() {
            IntVector iv =
                (IntVector)SwingUtilities.appContextGet(keyCodesKey);
            if (iv == null) {
                iv = new IntVector();
                SwingUtilities.appContextPut(keyCodesKey, iv);
            }
            return iv;
        }
        static void registerKeyPressed(int keyCode) {
            IntVector kca = getKeyCodeArray();
            int count = kca.size();
            int i;
            for(i=0;i<count;i++) {
                if(kca.elementAt(i) == -1){
                    kca.setElementAt(keyCode, i);
                    return;
                }
            }
            kca.addElement(keyCode);
        }
        static void registerKeyReleased(int keyCode) {
            IntVector kca = getKeyCodeArray();
            int count = kca.size();
            int i;
            for(i=0;i<count;i++) {
                if(kca.elementAt(i) == keyCode) {
                    kca.setElementAt(-1, i);
                    return;
                }
            }
        }
        static boolean keyIsPressed(int keyCode) {
            IntVector kca = getKeyCodeArray();
            int count = kca.size();
            int i;
            for(i=0;i<count;i++) {
                if(kca.elementAt(i) == keyCode) {
                    return true;
                }
            }
            return false;
        }
        static boolean shouldProcess(KeyEvent e) {
            switch (e.getID()) {
            case KeyEvent.KEY_PRESSED:
                if (!keyIsPressed(e.getKeyCode())) {
                    registerKeyPressed(e.getKeyCode());
                }
                return true;
            case KeyEvent.KEY_RELEASED:
                if (keyIsPressed(e.getKeyCode()) || e.getKeyCode()==KeyEvent.VK_PRINTSCREEN) {
                    registerKeyReleased(e.getKeyCode());
                    return true;
                }
                return false;
            case KeyEvent.KEY_TYPED:
                return true;
            default:
                return false;
            }
      }
    }
    static final sun.awt.RequestFocusController focusController =
        new sun.awt.RequestFocusController() {
            public boolean acceptRequestFocus(Component from, Component to,
                                              boolean temporary, boolean focusedWindowChangeAllowed,
                                              sun.awt.CausedFocusEvent.Cause cause)
            {
                if ((to == null) || !(to instanceof JComponent)) {
                    return true;
                }
                if ((from == null) || !(from instanceof JComponent)) {
                    return true;
                }
                JComponent target = (JComponent) to;
                if (!target.getVerifyInputWhenFocusTarget()) {
                    return true;
                }
                JComponent jFocusOwner = (JComponent)from;
                InputVerifier iv = jFocusOwner.getInputVerifier();
                if (iv == null) {
                    return true;
                } else {
                    Object currentSource = SwingUtilities.appContextGet(
                            INPUT_VERIFIER_SOURCE_KEY);
                    if (currentSource == jFocusOwner) {
                        return true;
                    }
                    SwingUtilities.appContextPut(INPUT_VERIFIER_SOURCE_KEY,
                                                 jFocusOwner);
                    try {
                        return iv.shouldYieldFocus(jFocusOwner);
                    } finally {
                        if (currentSource != null) {
                            SwingUtilities.appContextPut(
                                INPUT_VERIFIER_SOURCE_KEY, currentSource);
                        } else {
                            SwingUtilities.appContextRemove(
                                INPUT_VERIFIER_SOURCE_KEY);
                        }
                    }
                }
            }
        };
    @Deprecated
    public void enable() {
        if (isEnabled() != true) {
            super.enable();
            if (accessibleContext != null) {
                accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    null, AccessibleState.ENABLED);
            }
        }
    }
    @Deprecated
    public void disable() {
        if (isEnabled() != false) {
            super.disable();
            if (accessibleContext != null) {
                accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    AccessibleState.ENABLED, null);
            }
        }
    }
    protected AccessibleContext accessibleContext = null;
    public AccessibleContext getAccessibleContext() {
        return accessibleContext;
    }
    public abstract class AccessibleJComponent extends AccessibleAWTContainer
       implements AccessibleExtendedComponent
    {
        protected AccessibleJComponent() {
            super();
        }
        protected ContainerListener accessibleContainerHandler = null;
        protected FocusListener accessibleFocusHandler = null;
        protected class AccessibleContainerHandler
            implements ContainerListener {
            public void componentAdded(ContainerEvent e) {
                Component c = e.getChild();
                if (c != null && c instanceof Accessible) {
                    AccessibleJComponent.this.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_CHILD_PROPERTY,
                        null, c.getAccessibleContext());
                }
            }
            public void componentRemoved(ContainerEvent e) {
                Component c = e.getChild();
                if (c != null && c instanceof Accessible) {
                    AccessibleJComponent.this.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_CHILD_PROPERTY,
                        c.getAccessibleContext(), null);
                }
            }
        }
        protected class AccessibleFocusHandler implements FocusListener {
           public void focusGained(FocusEvent event) {
               if (accessibleContext != null) {
                    accessibleContext.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null, AccessibleState.FOCUSED);
                }
            }
            public void focusLost(FocusEvent event) {
                if (accessibleContext != null) {
                    accessibleContext.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.FOCUSED, null);
                }
            }
        } 
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (accessibleFocusHandler == null) {
                accessibleFocusHandler = new AccessibleFocusHandler();
                JComponent.this.addFocusListener(accessibleFocusHandler);
            }
            if (accessibleContainerHandler == null) {
                accessibleContainerHandler = new AccessibleContainerHandler();
                JComponent.this.addContainerListener(accessibleContainerHandler);
            }
            super.addPropertyChangeListener(listener);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (accessibleFocusHandler != null) {
                JComponent.this.removeFocusListener(accessibleFocusHandler);
                accessibleFocusHandler = null;
            }
            super.removePropertyChangeListener(listener);
        }
        protected String getBorderTitle(Border b) {
            String s;
            if (b instanceof TitledBorder) {
                return ((TitledBorder) b).getTitle();
            } else if (b instanceof CompoundBorder) {
                s = getBorderTitle(((CompoundBorder) b).getInsideBorder());
                if (s == null) {
                    s = getBorderTitle(((CompoundBorder) b).getOutsideBorder());
                }
                return s;
            } else {
                return null;
            }
        }
        public String getAccessibleName() {
            String name = accessibleName;
            if (name == null) {
                name = (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
            }
            if (name == null) {
                name = getBorderTitle(getBorder());
            }
            if (name == null) {
                Object o = getClientProperty(JLabel.LABELED_BY_PROPERTY);
                if (o instanceof Accessible) {
                    AccessibleContext ac = ((Accessible) o).getAccessibleContext();
                    if (ac != null) {
                        name = ac.getAccessibleName();
                    }
                }
            }
            return name;
        }
        public String getAccessibleDescription() {
            String description = accessibleDescription;
            if (description == null) {
                description = (String)getClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY);
            }
            if (description == null) {
                try {
                    description = getToolTipText();
                } catch (Exception e) {
                }
            }
            if (description == null) {
                Object o = getClientProperty(JLabel.LABELED_BY_PROPERTY);
                if (o instanceof Accessible) {
                    AccessibleContext ac = ((Accessible) o).getAccessibleContext();
                    if (ac != null) {
                        description = ac.getAccessibleDescription();
                    }
                }
            }
            return description;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SWING_COMPONENT;
        }
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JComponent.this.isOpaque()) {
                states.add(AccessibleState.OPAQUE);
            }
            return states;
        }
        public int getAccessibleChildrenCount() {
            return super.getAccessibleChildrenCount();
        }
        public Accessible getAccessibleChild(int i) {
            return super.getAccessibleChild(i);
        }
        AccessibleExtendedComponent getAccessibleExtendedComponent() {
            return this;
        }
        public String getToolTipText() {
            return JComponent.this.getToolTipText();
        }
        public String getTitledBorderText() {
            Border border = JComponent.this.getBorder();
            if (border instanceof TitledBorder) {
                return ((TitledBorder)border).getTitle();
            } else {
                return null;
            }
        }
        public AccessibleKeyBinding getAccessibleKeyBinding() {
            return null;
        }
    } 
    private ArrayTable getClientProperties() {
        if (clientProperties == null) {
            clientProperties = new ArrayTable();
        }
        return clientProperties;
    }
    public final Object getClientProperty(Object key) {
        if (key == SwingUtilities2.AA_TEXT_PROPERTY_KEY) {
            return aaTextInfo;
        } else if (key == SwingUtilities2.COMPONENT_UI_PROPERTY_KEY) {
            return ui;
        }
         if(clientProperties == null) {
            return null;
        } else {
            synchronized(clientProperties) {
                return clientProperties.get(key);
            }
        }
    }
    public final void putClientProperty(Object key, Object value) {
        if (key == SwingUtilities2.AA_TEXT_PROPERTY_KEY) {
            aaTextInfo = value;
            return;
        }
        if (value == null && clientProperties == null) {
            return;
        }
        ArrayTable clientProperties = getClientProperties();
        Object oldValue;
        synchronized(clientProperties) {
            oldValue = clientProperties.get(key);
            if (value != null) {
                clientProperties.put(key, value);
            } else if (oldValue != null) {
                clientProperties.remove(key);
            } else {
                return;
            }
        }
        clientPropertyChanged(key, oldValue, value);
        firePropertyChange(key.toString(), oldValue, value);
    }
    void clientPropertyChanged(Object key, Object oldValue,
                               Object newValue) {
    }
    void setUIProperty(String propertyName, Object value) {
        if (propertyName == "opaque") {
            if (!getFlag(OPAQUE_SET)) {
                setOpaque(((Boolean)value).booleanValue());
                setFlag(OPAQUE_SET, false);
            }
        } else if (propertyName == "autoscrolls") {
            if (!getFlag(AUTOSCROLLS_SET)) {
                setAutoscrolls(((Boolean)value).booleanValue());
                setFlag(AUTOSCROLLS_SET, false);
            }
        } else if (propertyName == "focusTraversalKeysForward") {
            if (!getFlag(FOCUS_TRAVERSAL_KEYS_FORWARD_SET)) {
                super.setFocusTraversalKeys(KeyboardFocusManager.
                                            FORWARD_TRAVERSAL_KEYS,
                                            (Set)value);
            }
        } else if (propertyName == "focusTraversalKeysBackward") {
            if (!getFlag(FOCUS_TRAVERSAL_KEYS_BACKWARD_SET)) {
                super.setFocusTraversalKeys(KeyboardFocusManager.
                                            BACKWARD_TRAVERSAL_KEYS,
                                            (Set)value);
            }
        } else {
            throw new IllegalArgumentException("property \""+
                                               propertyName+ "\" cannot be set using this method");
        }
    }
    public void
        setFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes)
    {
        if (id == KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS) {
            setFlag(FOCUS_TRAVERSAL_KEYS_FORWARD_SET,true);
        } else if (id == KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS) {
            setFlag(FOCUS_TRAVERSAL_KEYS_BACKWARD_SET,true);
        }
        super.setFocusTraversalKeys(id,keystrokes);
    }
    public static boolean isLightweightComponent(Component c) {
        return c.getPeer() instanceof LightweightPeer;
    }
    @Deprecated
    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
    }
    public Rectangle getBounds(Rectangle rv) {
        if (rv == null) {
            return new Rectangle(getX(), getY(), getWidth(), getHeight());
        }
        else {
            rv.setBounds(getX(), getY(), getWidth(), getHeight());
            return rv;
        }
    }
    public Dimension getSize(Dimension rv) {
        if (rv == null) {
            return new Dimension(getWidth(), getHeight());
        }
        else {
            rv.setSize(getWidth(), getHeight());
            return rv;
        }
    }
    public Point getLocation(Point rv) {
        if (rv == null) {
            return new Point(getX(), getY());
        }
        else {
            rv.setLocation(getX(), getY());
            return rv;
        }
    }
    public int getX() { return super.getX(); }
    public int getY() { return super.getY(); }
    public int getWidth() { return super.getWidth(); }
    public int getHeight() { return super.getHeight(); }
    public boolean isOpaque() {
        return getFlag(IS_OPAQUE);
    }
    public void setOpaque(boolean isOpaque) {
        boolean oldValue = getFlag(IS_OPAQUE);
        setFlag(IS_OPAQUE, isOpaque);
        setFlag(OPAQUE_SET, true);
        firePropertyChange("opaque", oldValue, isOpaque);
    }
    boolean rectangleIsObscured(int x,int y,int width,int height)
    {
        int numChildren = getComponentCount();
        for(int i = 0; i < numChildren; i++) {
            Component child = getComponent(i);
            int cx, cy, cw, ch;
            cx = child.getX();
            cy = child.getY();
            cw = child.getWidth();
            ch = child.getHeight();
            if (x >= cx && (x + width) <= (cx + cw) &&
                y >= cy && (y + height) <= (cy + ch) && child.isVisible()) {
                if(child instanceof JComponent) {
                    return child.isOpaque();
                } else {
                    return false;
                }
            }
        }
        return false;
    }
    static final void computeVisibleRect(Component c, Rectangle visibleRect) {
        Container p = c.getParent();
        Rectangle bounds = c.getBounds();
        if (p == null || p instanceof Window || p instanceof Applet) {
            visibleRect.setBounds(0, 0, bounds.width, bounds.height);
        } else {
            computeVisibleRect(p, visibleRect);
            visibleRect.x -= bounds.x;
            visibleRect.y -= bounds.y;
            SwingUtilities.computeIntersection(0,0,bounds.width,bounds.height,visibleRect);
        }
    }
    public void computeVisibleRect(Rectangle visibleRect) {
        computeVisibleRect(this, visibleRect);
    }
    public Rectangle getVisibleRect() {
        Rectangle visibleRect = new Rectangle();
        computeVisibleRect(visibleRect);
        return visibleRect;
    }
    public void firePropertyChange(String propertyName,
                                   boolean oldValue, boolean newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }
    public void firePropertyChange(String propertyName,
                                      int oldValue, int newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
        throws java.beans.PropertyVetoException
    {
        if (vetoableChangeSupport == null) {
            return;
        }
        vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }
    public synchronized void addVetoableChangeListener(VetoableChangeListener listener) {
        if (vetoableChangeSupport == null) {
            vetoableChangeSupport = new java.beans.VetoableChangeSupport(this);
        }
        vetoableChangeSupport.addVetoableChangeListener(listener);
    }
    public synchronized void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (vetoableChangeSupport == null) {
            return;
        }
        vetoableChangeSupport.removeVetoableChangeListener(listener);
    }
    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        if (vetoableChangeSupport == null) {
            return new VetoableChangeListener[0];
        }
        return vetoableChangeSupport.getVetoableChangeListeners();
    }
    public Container getTopLevelAncestor() {
        for(Container p = this; p != null; p = p.getParent()) {
            if(p instanceof Window || p instanceof Applet) {
                return p;
            }
        }
        return null;
    }
    private AncestorNotifier getAncestorNotifier() {
        return (AncestorNotifier)
            getClientProperty(JComponent_ANCESTOR_NOTIFIER);
    }
    public void addAncestorListener(AncestorListener listener) {
        AncestorNotifier ancestorNotifier = getAncestorNotifier();
        if (ancestorNotifier == null) {
            ancestorNotifier = new AncestorNotifier(this);
            putClientProperty(JComponent_ANCESTOR_NOTIFIER,
                              ancestorNotifier);
        }
        ancestorNotifier.addAncestorListener(listener);
    }
    public void removeAncestorListener(AncestorListener listener) {
        AncestorNotifier ancestorNotifier = getAncestorNotifier();
        if (ancestorNotifier == null) {
            return;
        }
        ancestorNotifier.removeAncestorListener(listener);
        if (ancestorNotifier.listenerList.getListenerList().length == 0) {
            ancestorNotifier.removeAllListeners();
            putClientProperty(JComponent_ANCESTOR_NOTIFIER, null);
        }
    }
    public AncestorListener[] getAncestorListeners() {
        AncestorNotifier ancestorNotifier = getAncestorNotifier();
        if (ancestorNotifier == null) {
            return new AncestorListener[0];
        }
        return ancestorNotifier.getAncestorListeners();
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        T[] result;
        if (listenerType == AncestorListener.class) {
            result = (T[])getAncestorListeners();
        }
        else if (listenerType == VetoableChangeListener.class) {
            result = (T[])getVetoableChangeListeners();
        }
        else if (listenerType == PropertyChangeListener.class) {
            result = (T[])getPropertyChangeListeners();
        }
        else {
            result = listenerList.getListeners(listenerType);
        }
        if (result.length == 0) {
            return super.getListeners(listenerType);
        }
        return result;
    }
    public void addNotify() {
        super.addNotify();
        firePropertyChange("ancestor", null, getParent());
        registerWithKeyboardManager(false);
        registerNextFocusableComponent();
    }
    public void removeNotify() {
        super.removeNotify();
        firePropertyChange("ancestor", getParent(), null);
        unregisterWithKeyboardManager();
        deregisterNextFocusableComponent();
        if (getCreatedDoubleBuffer()) {
            RepaintManager.currentManager(this).resetDoubleBuffer();
            setCreatedDoubleBuffer(false);
        }
        if (autoscrolls) {
            Autoscroller.stop(this);
        }
    }
    public void repaint(long tm, int x, int y, int width, int height) {
        RepaintManager.currentManager(this).addDirtyRegion(this, x, y, width, height);
    }
    public void repaint(Rectangle r) {
        repaint(0,r.x,r.y,r.width,r.height);
    }
    public void revalidate() {
        if (getParent() == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            invalidate();
            RepaintManager.currentManager(this).addInvalidComponent(this);
        }
        else {
            synchronized(this) {
                if (getFlag(REVALIDATE_RUNNABLE_SCHEDULED)) {
                    return;
                }
                setFlag(REVALIDATE_RUNNABLE_SCHEDULED, true);
            }
            Runnable callRevalidate = new Runnable() {
                public void run() {
                    synchronized(JComponent.this) {
                        setFlag(REVALIDATE_RUNNABLE_SCHEDULED, false);
                    }
                    revalidate();
                }
            };
            SwingUtilities.invokeLater(callRevalidate);
        }
    }
    @Override
    public boolean isValidateRoot() {
        return false;
    }
    public boolean isOptimizedDrawingEnabled() {
        return true;
    }
    protected boolean isPaintingOrigin() {
        return false;
    }
    public void paintImmediately(int x,int y,int w, int h) {
        Component c = this;
        Component parent;
        if(!isShowing()) {
            return;
        }
        JComponent paintingOigin = SwingUtilities.getPaintingOrigin(this);
        if (paintingOigin != null) {
            Rectangle rectangle = SwingUtilities.convertRectangle(
                    c, new Rectangle(x, y, w, h), paintingOigin);
            paintingOigin.paintImmediately(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            return;
        }
        while(!c.isOpaque()) {
            parent = c.getParent();
            if(parent != null) {
                x += c.getX();
                y += c.getY();
                c = parent;
            } else {
                break;
            }
            if(!(c instanceof JComponent)) {
                break;
            }
        }
        if(c instanceof JComponent) {
            ((JComponent)c)._paintImmediately(x,y,w,h);
        } else {
            c.repaint(x,y,w,h);
        }
    }
    public void paintImmediately(Rectangle r) {
        paintImmediately(r.x,r.y,r.width,r.height);
    }
    boolean alwaysOnTop() {
        return false;
    }
    void setPaintingChild(Component paintingChild) {
        this.paintingChild = paintingChild;
    }
    void _paintImmediately(int x, int y, int w, int h) {
        Graphics g;
        Container c;
        Rectangle b;
        int tmpX, tmpY, tmpWidth, tmpHeight;
        int offsetX=0,offsetY=0;
        boolean hasBuffer = false;
        JComponent bufferedComponent = null;
        JComponent paintingComponent = this;
        RepaintManager repaintManager = RepaintManager.currentManager(this);
        java.util.List<Component> path = new java.util.ArrayList<Component>(7);
        int pIndex = -1;
        int pCount = 0;
        tmpX = tmpY = tmpWidth = tmpHeight = 0;
        Rectangle paintImmediatelyClip = fetchRectangle();
        paintImmediatelyClip.x = x;
        paintImmediatelyClip.y = y;
        paintImmediatelyClip.width = w;
        paintImmediatelyClip.height = h;
        boolean ontop = alwaysOnTop() && isOpaque();
        if (ontop) {
            SwingUtilities.computeIntersection(0, 0, getWidth(), getHeight(),
                                               paintImmediatelyClip);
            if (paintImmediatelyClip.width == 0) {
                recycleRectangle(paintImmediatelyClip);
                return;
            }
        }
        Component child;
        for (c = this, child = null;
             c != null && !(c instanceof Window) && !(c instanceof Applet);
             child = c, c = c.getParent()) {
                JComponent jc = (c instanceof JComponent) ? (JComponent)c :
                                null;
                path.add(c);
                if(!ontop && jc != null && !jc.isOptimizedDrawingEnabled()) {
                    boolean resetPC;
                    if (c != this) {
                        if (jc.isPaintingOrigin()) {
                            resetPC = true;
                        }
                        else {
                            Component[] children = c.getComponents();
                            int i = 0;
                            for (; i<children.length; i++) {
                                if (children[i] == child) break;
                            }
                            switch (jc.getObscuredState(i,
                                            paintImmediatelyClip.x,
                                            paintImmediatelyClip.y,
                                            paintImmediatelyClip.width,
                                            paintImmediatelyClip.height)) {
                            case NOT_OBSCURED:
                                resetPC = false;
                                break;
                            case COMPLETELY_OBSCURED:
                                recycleRectangle(paintImmediatelyClip);
                                return;
                            default:
                                resetPC = true;
                                break;
                            }
                        }
                    }
                    else {
                        resetPC = false;
                    }
                    if (resetPC) {
                        paintingComponent = jc;
                        pIndex = pCount;
                        offsetX = offsetY = 0;
                        hasBuffer = false;
                    }
                }
                pCount++;
                if(repaintManager.isDoubleBufferingEnabled() && jc != null &&
                                  jc.isDoubleBuffered()) {
                    hasBuffer = true;
                    bufferedComponent = jc;
                }
                if (!ontop) {
                    int bx = c.getX();
                    int by = c.getY();
                    tmpWidth = c.getWidth();
                    tmpHeight = c.getHeight();
                    SwingUtilities.computeIntersection(tmpX,tmpY,tmpWidth,tmpHeight,paintImmediatelyClip);
                    paintImmediatelyClip.x += bx;
                    paintImmediatelyClip.y += by;
                    offsetX += bx;
                    offsetY += by;
                }
        }
        if(c == null || c.getPeer() == null ||
                        paintImmediatelyClip.width <= 0 ||
                        paintImmediatelyClip.height <= 0) {
            recycleRectangle(paintImmediatelyClip);
            return;
        }
        paintingComponent.setFlag(IS_REPAINTING, true);
        paintImmediatelyClip.x -= offsetX;
        paintImmediatelyClip.y -= offsetY;
        if(paintingComponent != this) {
            Component comp;
            int i = pIndex;
            for(; i > 0 ; i--) {
                comp = path.get(i);
                if(comp instanceof JComponent) {
                    ((JComponent)comp).setPaintingChild(path.get(i-1));
                }
            }
        }
        try {
            if ((g = safelyGetGraphics(paintingComponent, c)) != null) {
                try {
                    if (hasBuffer) {
                        RepaintManager rm = RepaintManager.currentManager(
                                bufferedComponent);
                        rm.beginPaint();
                        try {
                            rm.paint(paintingComponent, bufferedComponent, g,
                                    paintImmediatelyClip.x,
                                    paintImmediatelyClip.y,
                                    paintImmediatelyClip.width,
                                    paintImmediatelyClip.height);
                        } finally {
                            rm.endPaint();
                        }
                    } else {
                        g.setClip(paintImmediatelyClip.x, paintImmediatelyClip.y,
                                paintImmediatelyClip.width, paintImmediatelyClip.height);
                        paintingComponent.paint(g);
                    }
                } finally {
                    g.dispose();
                }
            }
        }
        finally {
            if(paintingComponent != this) {
                Component comp;
                int i = pIndex;
                for(; i > 0 ; i--) {
                    comp = path.get(i);
                    if(comp instanceof JComponent) {
                        ((JComponent)comp).setPaintingChild(null);
                    }
                }
            }
            paintingComponent.setFlag(IS_REPAINTING, false);
        }
        recycleRectangle(paintImmediatelyClip);
    }
    void paintToOffscreen(Graphics g, int x, int y, int w, int h, int maxX,
                          int maxY) {
        try {
            setFlag(ANCESTOR_USING_BUFFER, true);
            if ((y + h) < maxY || (x + w) < maxX) {
                setFlag(IS_PAINTING_TILE, true);
            }
            if (getFlag(IS_REPAINTING)) {
                paint(g);
            } else {
                if(!rectangleIsObscured(x, y, w, h)) {
                    paintComponent(g);
                    paintBorder(g);
                }
                paintChildren(g);
            }
        } finally {
            setFlag(ANCESTOR_USING_BUFFER, false);
            setFlag(IS_PAINTING_TILE, false);
        }
    }
    private int getObscuredState(int compIndex, int x, int y, int width,
                                 int height) {
        int retValue = NOT_OBSCURED;
        Rectangle tmpRect = fetchRectangle();
        for (int i = compIndex - 1 ; i >= 0 ; i--) {
            Component sibling = getComponent(i);
            if (!sibling.isVisible()) {
                continue;
            }
            Rectangle siblingRect;
            boolean opaque;
            if (sibling instanceof JComponent) {
                opaque = sibling.isOpaque();
                if (!opaque) {
                    if (retValue == PARTIALLY_OBSCURED) {
                        continue;
                    }
                }
            }
            else {
                opaque = true;
            }
            siblingRect = sibling.getBounds(tmpRect);
            if (opaque && x >= siblingRect.x && (x + width) <=
                     (siblingRect.x + siblingRect.width) &&
                     y >= siblingRect.y && (y + height) <=
                     (siblingRect.y + siblingRect.height)) {
                recycleRectangle(tmpRect);
                return COMPLETELY_OBSCURED;
            }
            else if (retValue == NOT_OBSCURED &&
                     !((x + width <= siblingRect.x) ||
                       (y + height <= siblingRect.y) ||
                       (x >= siblingRect.x + siblingRect.width) ||
                       (y >= siblingRect.y + siblingRect.height))) {
                retValue = PARTIALLY_OBSCURED;
            }
        }
        recycleRectangle(tmpRect);
        return retValue;
    }
    boolean checkIfChildObscuredBySibling() {
        return true;
    }
    private void setFlag(int aFlag, boolean aValue) {
        if(aValue) {
            flags |= (1 << aFlag);
        } else {
            flags &= ~(1 << aFlag);
        }
    }
    private boolean getFlag(int aFlag) {
        int mask = (1 << aFlag);
        return ((flags & mask) == mask);
    }
    static void setWriteObjCounter(JComponent comp, byte count) {
        comp.flags = (comp.flags & ~(0xFF << WRITE_OBJ_COUNTER_FIRST)) |
                     (count << WRITE_OBJ_COUNTER_FIRST);
    }
    static byte getWriteObjCounter(JComponent comp) {
        return (byte)((comp.flags >> WRITE_OBJ_COUNTER_FIRST) & 0xFF);
    }
    public void setDoubleBuffered(boolean aFlag) {
        setFlag(IS_DOUBLE_BUFFERED,aFlag);
    }
    public boolean isDoubleBuffered() {
        return getFlag(IS_DOUBLE_BUFFERED);
    }
    public JRootPane getRootPane() {
        return SwingUtilities.getRootPane(this);
    }
    void compWriteObjectNotify() {
        byte count = JComponent.getWriteObjCounter(this);
        JComponent.setWriteObjCounter(this, (byte)(count + 1));
        if (count != 0) {
            return;
        }
        uninstallUIAndProperties();
        if (getToolTipText() != null ||
            this instanceof javax.swing.table.JTableHeader) {
            ToolTipManager.sharedInstance().unregisterComponent(JComponent.this);
        }
    }
    private class ReadObjectCallback implements ObjectInputValidation
    {
        private final Vector<JComponent> roots = new Vector<JComponent>(1);
        private final ObjectInputStream inputStream;
        ReadObjectCallback(ObjectInputStream s) throws Exception {
            inputStream = s;
            s.registerValidation(this, 0);
        }
        public void validateObject() throws InvalidObjectException {
            try {
                for (JComponent root : roots) {
                    SwingUtilities.updateComponentTreeUI(root);
                }
            }
            finally {
                readObjectCallbacks.remove(inputStream);
            }
        }
        private void registerComponent(JComponent c)
        {
            for (JComponent root : roots) {
                for(Component p = c; p != null; p = p.getParent()) {
                    if (p == root) {
                        return;
                    }
                }
            }
            for(int i = 0; i < roots.size(); i++) {
                JComponent root = roots.elementAt(i);
                for(Component p = root.getParent(); p != null; p = p.getParent()) {
                    if (p == c) {
                        roots.removeElementAt(i--); 
                        break;
                    }
                }
            }
            roots.addElement(c);
        }
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        ReadObjectCallback cb = readObjectCallbacks.get(s);
        if (cb == null) {
            try {
                readObjectCallbacks.put(s, cb = new ReadObjectCallback(s));
            }
            catch (Exception e) {
                throw new IOException(e.toString());
            }
        }
        cb.registerComponent(this);
        int cpCount = s.readInt();
        if (cpCount > 0) {
            clientProperties = new ArrayTable();
            for (int counter = 0; counter < cpCount; counter++) {
                clientProperties.put(s.readObject(),
                                     s.readObject());
            }
        }
        if (getToolTipText() != null) {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        setWriteObjCounter(this, (byte)0);
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
        ArrayTable.writeArrayTable(s, clientProperties);
    }
    protected String paramString() {
        String preferredSizeString = (isPreferredSizeSet() ?
                                      getPreferredSize().toString() : "");
        String minimumSizeString = (isMinimumSizeSet() ?
                                    getMinimumSize().toString() : "");
        String maximumSizeString = (isMaximumSizeSet() ?
                                    getMaximumSize().toString() : "");
        String borderString = (border == null ? ""
                               : (border == this ? "this" : border.toString()));
        return super.paramString() +
        ",alignmentX=" + alignmentX +
        ",alignmentY=" + alignmentY +
        ",border=" + borderString +
        ",flags=" + flags +             
        ",maximumSize=" + maximumSizeString +
        ",minimumSize=" + minimumSizeString +
        ",preferredSize=" + preferredSizeString;
    }
}
