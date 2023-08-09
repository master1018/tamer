public abstract class Component implements ImageObserver, MenuContainer, Serializable {
    private static final long serialVersionUID = -7644114512714619750L;
    public static final float TOP_ALIGNMENT = 0.0f;
    public static final float CENTER_ALIGNMENT = 0.5f;
    public static final float BOTTOM_ALIGNMENT = 1.0f;
    public static final float LEFT_ALIGNMENT = 0.0f;
    public static final float RIGHT_ALIGNMENT = 1.0f;
    private static final Hashtable<Class<?>, Boolean> childClassesFlags = new Hashtable<Class<?>, Boolean>();
    private static final ComponentPeer peer = new ComponentPeer() {
    };
    private static final boolean incrementalImageUpdate;
    final transient Toolkit toolkit = Toolkit.getDefaultToolkit();
    protected class BltBufferStrategy extends BufferStrategy {
        protected VolatileImage[] backBuffers;
        protected BufferCapabilities caps;
        protected int width;
        protected int height;
        protected boolean validatedContents;
        protected BltBufferStrategy(int numBuffers, BufferCapabilities caps)
                throws org.apache.harmony.luni.util.NotImplementedException {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        @Override
        public boolean contentsLost() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return false;
        }
        @Override
        public boolean contentsRestored() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return false;
        }
        protected void createBackBuffers(int numBuffers) {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        @Override
        public BufferCapabilities getCapabilities() {
            return (BufferCapabilities)caps.clone();
        }
        @Override
        public Graphics getDrawGraphics() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return null;
        }
        protected void revalidate() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        @Override
        public void show() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
    }
    protected class FlipBufferStrategy extends BufferStrategy {
        protected BufferCapabilities caps;
        protected Image drawBuffer;
        protected VolatileImage drawVBuffer;
        protected int numBuffers;
        protected boolean validatedContents;
        protected FlipBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
            this.numBuffers = numBuffers;
            this.caps = (BufferCapabilities)caps.clone();
        }
        @Override
        public boolean contentsLost() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return false;
        }
        @Override
        public boolean contentsRestored() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return false;
        }
        protected void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {
            if (numBuffers < 2) {
                throw new IllegalArgumentException(Messages.getString("awt.14C")); 
            }
            if (!caps.isPageFlipping()) {
                throw new IllegalArgumentException(Messages.getString("awt.14D")); 
            }
            if (!Component.this.behaviour.isDisplayable()) {
                throw new IllegalStateException(Messages.getString("awt.14E")); 
            }
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        protected void destroyBuffers() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        protected void flip(BufferCapabilities.FlipContents flipAction) {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        protected Image getBackBuffer() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return null;
        }
        @Override
        public BufferCapabilities getCapabilities() {
            return (BufferCapabilities)caps.clone();
        }
        @Override
        public Graphics getDrawGraphics() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
            return null;
        }
        protected void revalidate() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
        @Override
        public void show() {
            if (true) {
                throw new RuntimeException("Method is not implemented"); 
            }
        }
    }
    class ComponentState implements State {
        private Dimension defaultMinimumSize = new Dimension();
        public boolean isEnabled() {
            return enabled;
        }
        public boolean isVisible() {
            return visible;
        }
        public boolean isFocused() {
            return false;
        }
        public Font getFont() {
            return Component.this.getFont();
        }
        public boolean isFontSet() {
            return font != null;
        }
        public Color getBackground() {
            Color c = Component.this.getBackground();
            return (c != null) ? c : getDefaultBackground();
        }
        public boolean isBackgroundSet() {
            return backColor != null;
        }
        public Color getTextColor() {
            Color c = getForeground();
            return (c != null) ? c : getDefaultForeground();
        }
        public boolean isTextColorSet() {
            return foreColor != null;
        }
        @SuppressWarnings("deprecation")
        public FontMetrics getFontMetrics() {
            return toolkit.getFontMetrics(Component.this.getFont());
        }
        public Rectangle getBounds() {
            return new Rectangle(x, y, w, h);
        }
        public Dimension getSize() {
            return new Dimension(w, h);
        }
        public long getWindowId() {
            NativeWindow win = getNativeWindow();
            return (win != null) ? win.getId() : 0;
        }
        public Dimension getDefaultMinimumSize() {
            if (defaultMinimumSize == null) {
                calculate();
            }
            return defaultMinimumSize;
        }
        public void setDefaultMinimumSize(Dimension size) {
            defaultMinimumSize = size;
        }
        public void reset() {
            defaultMinimumSize = null;
        }
        public void calculate() {
        }
    }
    final transient ComponentBehavior behaviour;
    private String name;
    private boolean autoName = true;
    private Font font;
    private Color backColor;
    private Color foreColor;
    boolean deprecatedEventHandler = true;
    private long enabledEvents;
    private long enabledAWTEvents;
    private final AWTListenerList<ComponentListener> componentListeners = new AWTListenerList<ComponentListener>(
            this);
    private final AWTListenerList<FocusListener> focusListeners = new AWTListenerList<FocusListener>(
            this);
    private final AWTListenerList<HierarchyListener> hierarchyListeners = new AWTListenerList<HierarchyListener>(
            this);
    private final AWTListenerList<HierarchyBoundsListener> hierarchyBoundsListeners = new AWTListenerList<HierarchyBoundsListener>(
            this);
    private final AWTListenerList<KeyListener> keyListeners = new AWTListenerList<KeyListener>(this);
    private final AWTListenerList<MouseListener> mouseListeners = new AWTListenerList<MouseListener>(
            this);
    private final AWTListenerList<MouseMotionListener> mouseMotionListeners = new AWTListenerList<MouseMotionListener>(
            this);
    private final AWTListenerList<MouseWheelListener> mouseWheelListeners = new AWTListenerList<MouseWheelListener>(
            this);
    private final AWTListenerList<InputMethodListener> inputMethodListeners = new AWTListenerList<InputMethodListener>(
            this);
    int x;
    int y;
    int w;
    int h;
    private Dimension maximumSize;
    private Dimension minimumSize;
    private Dimension preferredSize;
    private int boundsMaskParam;
    private boolean ignoreRepaint;
    private boolean enabled = true;
    private boolean inputMethodsEnabled = true;
    transient boolean dispatchToIM = true;
    private boolean focusable = true; 
    boolean visible = true;
    private boolean calledSetFocusable;
    private boolean overridenIsFocusable = true;
    private boolean focusTraversalKeysEnabled = true;
    private final Map<Integer, Set<? extends AWTKeyStroke>> traversalKeys = new HashMap<Integer, Set<? extends AWTKeyStroke>>();
    int[] traversalIDs;
    private Locale locale;
    private ComponentOrientation orientation;
    private PropertyChangeSupport propertyChangeSupport;
    private boolean coalescer;
    private Hashtable<Integer, LinkedList<AWTEvent>> eventsTable;
    private LinkedList<AWTEvent> eventsList;
    private int hierarchyChangingCounter;
    private boolean wasShowing;
    private boolean wasDisplayable;
    Cursor cursor;
    private boolean mouseExitedExpected;
    transient MultiRectArea repaintRegion;
    transient Object redrawManager;
    private boolean valid;
    private HashMap<Image, ImageParameters> updatedImages;
    private class ComponentLock {
    }
    private final transient Object componentLock = new ComponentLock();
    static {
        PrivilegedAction<String[]> action = new PrivilegedAction<String[]>() {
            public String[] run() {
                String properties[] = new String[2];
                properties[0] = System.getProperty("awt.image.redrawrate", "100"); 
                properties[1] = System.getProperty("awt.image.incrementaldraw", "true"); 
                return properties;
            }
        };
        String properties[] = AccessController.doPrivileged(action);
        incrementalImageUpdate = properties[1].equals("true"); 
    }
    protected Component() {
        toolkit.lockAWT();
        try {
            orientation = ComponentOrientation.UNKNOWN;
            redrawManager = null;
            behaviour = null;
            deriveCoalescerFlag();
        } finally {
            toolkit.unlockAWT();
        }
    }
    private void deriveCoalescerFlag() {
        Class<?> thisClass = getClass();
        boolean flag = true;
        synchronized (childClassesFlags) {
            Boolean flagWrapper = childClassesFlags.get(thisClass);
            if (flagWrapper == null) {
                Method coalesceMethod = null;
                for (Class<?> c = thisClass; c != Component.class; c = c.getSuperclass()) {
                    try {
                        coalesceMethod = c.getDeclaredMethod("coalesceEvents", new Class[] { 
                                        Class.forName("java.awt.AWTEvent"), 
                                        Class.forName("java.awt.AWTEvent")}); 
                    } catch (Exception e) {
                    }
                    if (coalesceMethod != null) {
                        break;
                    }
                }
                flag = (coalesceMethod != null);
                childClassesFlags.put(thisClass, Boolean.valueOf(flag));
            } else {
                flag = flagWrapper.booleanValue();
            }
        }
        coalescer = flag;
        if (flag) {
            eventsTable = new Hashtable<Integer, LinkedList<AWTEvent>>();
        } else {
            eventsTable = null;
        }
    }
    public void setName(String name) {
        String oldName;
        toolkit.lockAWT();
        try {
            autoName = false;
            oldName = this.name;
            this.name = name;
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("name", oldName, name); 
    }
    public String getName() {
        toolkit.lockAWT();
        try {
            if ((name == null) && autoName) {
                name = autoName();
            }
            return name;
        } finally {
            toolkit.unlockAWT();
        }
    }
    String autoName() {
        String name = getClass().getName();
        if (name.indexOf("$") != -1) { 
            return null;
        }
        int number = 0;
        name = name.substring(name.lastIndexOf(".") + 1) + Integer.toString(number); 
        return name;
    }
    @Override
    public String toString() {
        toolkit.lockAWT();
        try {
            return getClass().getName() + "[" + paramString() + "]"; 
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean contains(Point p) {
        toolkit.lockAWT();
        try {
            return contains(p.x, p.y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean contains(int x, int y) {
        toolkit.lockAWT();
        try {
            return inside(x, y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public Dimension size() {
        toolkit.lockAWT();
        try {
            return new Dimension(w, h);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void list(PrintStream out, int indent) {
        toolkit.lockAWT();
        try {
            out.println(getIndentStr(indent) + this);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void list(PrintWriter out) {
        toolkit.lockAWT();
        try {
            list(out, 1);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void list(PrintWriter out, int indent) {
        toolkit.lockAWT();
        try {
            out.println(getIndentStr(indent) + this);
        } finally {
            toolkit.unlockAWT();
        }
    }
    String getIndentStr(int indent) {
        char[] ind = new char[indent];
        for (int i = 0; i < indent; ind[i++] = ' ') {
            ;
        }
        return new String(ind);
    }
    public void list(PrintStream out) {
        toolkit.lockAWT();
        try {
            list(out, 1);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void list() {
        toolkit.lockAWT();
        try {
            list(System.out);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void print(Graphics g) {
        toolkit.lockAWT();
        try {
            paint(g);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void printAll(Graphics g) {
        toolkit.lockAWT();
        try {
            paintAll(g);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setSize(int width, int height) {
        toolkit.lockAWT();
        try {
            resize(width, height);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setSize(Dimension d) {
        toolkit.lockAWT();
        try {
            resize(d);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void resize(int width, int height) {
        toolkit.lockAWT();
        try {
            boundsMaskParam = NativeWindow.BOUNDS_NOMOVE;
            setBounds(x, y, width, height);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void resize(Dimension size) {
        toolkit.lockAWT();
        try {
            setSize(size.width, size.height);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isOpaque() {
        toolkit.lockAWT();
        try {
            return behaviour.isOpaque();
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void disable() {
        toolkit.lockAWT();
        try {
            setEnabledImpl(false);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void enable() {
        toolkit.lockAWT();
        try {
            setEnabledImpl(true);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void enable(boolean b) {
        toolkit.lockAWT();
        try {
            if (b) {
                enable();
            } else {
                disable();
            }
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Point getLocation(Point rv) {
        toolkit.lockAWT();
        try {
            if (rv == null) {
                rv = new Point();
            }
            rv.setLocation(getX(), getY());
            return rv;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Point getLocation() {
        toolkit.lockAWT();
        try {
            return location();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Dimension getSize() {
        toolkit.lockAWT();
        try {
            return size();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Dimension getSize(Dimension rv) {
        toolkit.lockAWT();
        try {
            if (rv == null) {
                rv = new Dimension();
            }
            rv.setSize(getWidth(), getHeight());
            return rv;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isValid() {
        toolkit.lockAWT();
        try {
            return valid && behaviour.isDisplayable();
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public Point location() {
        toolkit.lockAWT();
        try {
            return new Point(x, y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void addNotify() {
        toolkit.lockAWT();
        try {
            prepare4HierarchyChange();
            behaviour.addNotify();
        } finally {
            toolkit.unlockAWT();
        }
    }
    void mapToDisplay(boolean b) {
    }
    public Toolkit getToolkit() {
        return toolkit;
    }
    public final Object getTreeLock() {
        return toolkit.awtTreeLock;
    }
    @Deprecated
    public boolean action(Event evt, Object what) {
        return false;
    }
    private PropertyChangeSupport getPropertyChangeSupport() {
        synchronized (componentLock) {
            if (propertyChangeSupport == null) {
                propertyChangeSupport = new PropertyChangeSupport(this);
            }
            return propertyChangeSupport;
        }
    }
    public boolean areFocusTraversalKeysSet(int id) {
        toolkit.lockAWT();
        try {
            Integer Id = new Integer(id);
            if (traversalKeys.containsKey(Id)) {
                return traversalKeys.get(Id) != null;
            }
            throw new IllegalArgumentException(Messages.getString("awt.14F")); 
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public Rectangle bounds() {
        toolkit.lockAWT();
        try {
            return new Rectangle(x, y, w, h);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public int checkImage(Image image, int width, int height, ImageObserver observer) {
        toolkit.lockAWT();
        try {
            return toolkit.checkImage(image, width, height, observer);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public int checkImage(Image image, ImageObserver observer) {
        toolkit.lockAWT();
        try {
            return toolkit.checkImage(image, -1, -1, observer);
        } finally {
            toolkit.unlockAWT();
        }
    }
    protected AWTEvent coalesceEvents(AWTEvent existingEvent, AWTEvent newEvent) {
        toolkit.lockAWT();
        try {
            return null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    boolean isCoalescer() {
        return coalescer;
    }
    AWTEvent getRelativeEvent(int id) {
        Integer idWrapper = new Integer(id);
        eventsList = eventsTable.get(idWrapper);
        if (eventsList == null) {
            eventsList = new LinkedList<AWTEvent>();
            eventsTable.put(idWrapper, eventsList);
            return null;
        }
        if (eventsList.isEmpty()) {
            return null;
        }
        return eventsList.getLast();
    }
    void addNewEvent(AWTEvent event) {
        eventsList.addLast(event);
    }
    void removeRelativeEvent() {
        eventsList.removeLast();
    }
    void removeNextEvent(int id) {
        eventsTable.get(new Integer(id)).removeFirst();
    }
    public Image createImage(ImageProducer producer) {
        toolkit.lockAWT();
        try {
            return toolkit.createImage(producer);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Image createImage(int width, int height) {
        toolkit.lockAWT();
        try {
            if (!isDisplayable()) {
                return null;
            }
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (gc == null) {
                return null;
            }
            ColorModel cm = gc.getColorModel(Transparency.OPAQUE);
            WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
            Image image = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
            fillImageBackground(image, width, height);
            return image;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public VolatileImage createVolatileImage(int width, int height, ImageCapabilities caps)
            throws AWTException {
        toolkit.lockAWT();
        try {
            if (!isDisplayable()) {
                return null;
            }
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (gc == null) {
                return null;
            }
            VolatileImage image = gc.createCompatibleVolatileImage(width, height, caps);
            fillImageBackground(image, width, height);
            return image;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public VolatileImage createVolatileImage(int width, int height) {
        toolkit.lockAWT();
        try {
            if (!isDisplayable()) {
                return null;
            }
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (gc == null) {
                return null;
            }
            VolatileImage image = gc.createCompatibleVolatileImage(width, height);
            fillImageBackground(image, width, height);
            return image;
        } finally {
            toolkit.unlockAWT();
        }
    }
    private void fillImageBackground(Image image, int width, int height) {
        Graphics gr = image.getGraphics();
        gr.setColor(getBackground());
        gr.fillRect(0, 0, width, height);
        gr.dispose();
    }
    @Deprecated
    public void deliverEvent(Event evt) {
        postEvent(evt);
    }
    public void doLayout() {
        toolkit.lockAWT();
        try {
            layout();
        } finally {
            toolkit.unlockAWT();
        }
    }
    private void firePropertyChangeImpl(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeSupport pcs;
        synchronized (componentLock) {
            if (propertyChangeSupport == null) {
                return;
            }
            pcs = propertyChangeSupport;
        }
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        firePropertyChangeImpl(propertyName, new Integer(oldValue), new Integer(newValue));
    }
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        firePropertyChangeImpl(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }
    protected void firePropertyChange(final String propertyName, final Object oldValue,
            final Object newValue) {
        firePropertyChangeImpl(propertyName, oldValue, newValue);
    }
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
        firePropertyChangeImpl(propertyName, new Byte(oldValue), new Byte(newValue));
    }
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        firePropertyChangeImpl(propertyName, new Character(oldValue), new Character(newValue));
    }
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
        firePropertyChangeImpl(propertyName, new Short(oldValue), new Short(newValue));
    }
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
        firePropertyChangeImpl(propertyName, new Long(oldValue), new Long(newValue));
    }
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
        firePropertyChangeImpl(propertyName, new Float(oldValue), new Float(newValue));
    }
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
        firePropertyChangeImpl(propertyName, new Double(oldValue), new Double(newValue));
    }
    public float getAlignmentX() {
        toolkit.lockAWT();
        try {
            return CENTER_ALIGNMENT;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public float getAlignmentY() {
        toolkit.lockAWT();
        try {
            return CENTER_ALIGNMENT;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Color getBackground() {
        toolkit.lockAWT();
        try {
            return backColor;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Rectangle getBounds() {
        toolkit.lockAWT();
        try {
            return bounds();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Rectangle getBounds(Rectangle rv) {
        toolkit.lockAWT();
        try {
            if (rv == null) {
                rv = new Rectangle();
            }
            rv.setBounds(x, y, w, h);
            return rv;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public ColorModel getColorModel() {
        toolkit.lockAWT();
        try {
            return getToolkit().getColorModel();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Component getComponentAt(Point p) {
        toolkit.lockAWT();
        try {
            return getComponentAt(p.x, p.y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Component getComponentAt(int x, int y) {
        toolkit.lockAWT();
        try {
            return locate(x, y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public ComponentOrientation getComponentOrientation() {
        toolkit.lockAWT();
        try {
            return orientation;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Cursor getCursor() {
        toolkit.lockAWT();
        try {
            if (cursor != null) {
                return cursor;
            }
            return Cursor.getDefaultCursor();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean getFocusTraversalKeysEnabled() {
        toolkit.lockAWT();
        try {
            return focusTraversalKeysEnabled;
        } finally {
            toolkit.unlockAWT();
        }
    }
    @SuppressWarnings("deprecation")
    public FontMetrics getFontMetrics(Font f) {
        return toolkit.getFontMetrics(f);
    }
    public Color getForeground() {
        toolkit.lockAWT();
        try {
            return foreColor;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Graphics getGraphics() {
        toolkit.lockAWT();
        try {
            if (!isDisplayable()) {
                return null;
            }
            Graphics g = behaviour.getGraphics(0, 0, w, h);
            g.setColor(foreColor);
            g.setFont(font);
            return g;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public GraphicsConfiguration getGraphicsConfiguration() {
        return null;
    }
    public int getHeight() {
        toolkit.lockAWT();
        try {
            return h;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean getIgnoreRepaint() {
        toolkit.lockAWT();
        try {
            return ignoreRepaint;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public InputContext getInputContext() {
        toolkit.lockAWT();
        try {
            return null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public InputMethodRequests getInputMethodRequests() {
        return null;
    }
    public Locale getLocale() {
        toolkit.lockAWT();
        try {
            return locale;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Point getLocationOnScreen() throws IllegalComponentStateException {
        toolkit.lockAWT();
        try {
            Point p = new Point();
            if (isShowing()) {
                return p;
            }
            throw new IllegalComponentStateException(Messages.getString("awt.151")); 
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public ComponentPeer getPeer() {
        toolkit.lockAWT();
        try {
            if (behaviour.isDisplayable()) {
                return peer;
            }
            return null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return getPropertyChangeSupport().getPropertyChangeListeners();
    }
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return getPropertyChangeSupport().getPropertyChangeListeners(propertyName);
    }
    public int getWidth() {
        toolkit.lockAWT();
        try {
            return w;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public int getX() {
        toolkit.lockAWT();
        try {
            return x;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public int getY() {
        toolkit.lockAWT();
        try {
            return y;
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public boolean gotFocus(Event evt, Object what) {
        return false;
    }
    @Deprecated
    public boolean handleEvent(Event evt) {
        switch (evt.id) {
            case Event.ACTION_EVENT:
                return action(evt, evt.arg);
            case Event.GOT_FOCUS:
                return gotFocus(evt, null);
            case Event.LOST_FOCUS:
                return lostFocus(evt, null);
            case Event.MOUSE_DOWN:
                return mouseDown(evt, evt.x, evt.y);
            case Event.MOUSE_DRAG:
                return mouseDrag(evt, evt.x, evt.y);
            case Event.MOUSE_ENTER:
                return mouseEnter(evt, evt.x, evt.y);
            case Event.MOUSE_EXIT:
                return mouseExit(evt, evt.x, evt.y);
            case Event.MOUSE_MOVE:
                return mouseMove(evt, evt.x, evt.y);
            case Event.MOUSE_UP:
                return mouseUp(evt, evt.x, evt.y);
            case Event.KEY_ACTION:
            case Event.KEY_PRESS:
                return keyDown(evt, evt.key);
            case Event.KEY_ACTION_RELEASE:
            case Event.KEY_RELEASE:
                return keyUp(evt, evt.key);
        }
        return false;
    }
    public boolean hasFocus() {
        toolkit.lockAWT();
        try {
            return false;
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void hide() {
        toolkit.lockAWT();
        try {
            if (!visible) {
                return;
            }
            prepare4HierarchyChange();
            visible = false;
            moveFocusOnHide();
            behaviour.setVisible(false);
            postEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN));
            notifyInputMethod(null);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public boolean inside(int x, int y) {
        toolkit.lockAWT();
        try {
            return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void invalidate() {
        toolkit.lockAWT();
        try {
            valid = false;
            resetDefaultSize();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isBackgroundSet() {
        toolkit.lockAWT();
        try {
            return backColor != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isCursorSet() {
        toolkit.lockAWT();
        try {
            return cursor != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isDisplayable() {
        toolkit.lockAWT();
        try {
            return behaviour.isDisplayable();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isDoubleBuffered() {
        toolkit.lockAWT();
        try {
            return false;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isEnabled() {
        toolkit.lockAWT();
        try {
            return enabled;
        } finally {
            toolkit.unlockAWT();
        }
    }
    boolean isIndirectlyEnabled() {
        Component comp = this;
        while (comp != null) {
            if (!comp.isLightweight() && !comp.isEnabled()) {
                return false;
            }
        }
        return true;
    }
    boolean isKeyEnabled() {
        if (!isEnabled()) {
            return false;
        }
        return isIndirectlyEnabled();
    }
    @Deprecated
    public boolean isFocusTraversable() {
        toolkit.lockAWT();
        try {
            overridenIsFocusable = false;
            return focusable; 
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isFocusable() {
        toolkit.lockAWT();
        try {
            return isFocusTraversable();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isFontSet() {
        toolkit.lockAWT();
        try {
            return font != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isForegroundSet() {
        toolkit.lockAWT();
        try {
            return foreColor != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isLightweight() {
        toolkit.lockAWT();
        try {
            return behaviour.isLightweight();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isShowing() {
        return false;
    }
    public boolean isVisible() {
        toolkit.lockAWT();
        try {
            return visible;
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public boolean keyDown(Event evt, int key) {
        return false;
    }
    @Deprecated
    public boolean keyUp(Event evt, int key) {
        return false;
    }
    @Deprecated
    public void layout() {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public Component locate(int x, int y) {
        toolkit.lockAWT();
        try {
            if (contains(x, y)) {
                return this;
            }
            return null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public boolean lostFocus(Event evt, Object what) {
        return false;
    }
    @Deprecated
    public boolean mouseDown(Event evt, int x, int y) {
        return false;
    }
    @Deprecated
    public boolean mouseDrag(Event evt, int x, int y) {
        return false;
    }
    @Deprecated
    public boolean mouseEnter(Event evt, int x, int y) {
        return false;
    }
    @Deprecated
    public boolean mouseExit(Event evt, int x, int y) {
        return false;
    }
    @Deprecated
    public boolean mouseMove(Event evt, int x, int y) {
        return false;
    }
    @Deprecated
    public boolean mouseUp(Event evt, int x, int y) {
        return false;
    }
    @Deprecated
    public void move(int x, int y) {
        toolkit.lockAWT();
        try {
            boundsMaskParam = NativeWindow.BOUNDS_NOSIZE;
            setBounds(x, y, w, h);
        } finally {
            toolkit.unlockAWT();
        }
    }
    protected String paramString() {
        toolkit.lockAWT();
        try {
            return getName() + "," + getX() + "," + getY() + "," + getWidth() + "x" 
                    + getHeight() + (!isVisible() ? ",hidden" : ""); 
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean postEvent(Event evt) {
        boolean handled = handleEvent(evt);
        if (handled) {
            return true;
        }
        return false;
    }
    public boolean prepareImage(Image image, ImageObserver observer) {
        toolkit.lockAWT();
        try {
            return toolkit.prepareImage(image, -1, -1, observer);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
        toolkit.lockAWT();
        try {
            return toolkit.prepareImage(image, width, height, observer);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void removeNotify() {
        toolkit.lockAWT();
        try {
            prepare4HierarchyChange();
            behaviour.removeNotify();
            removeNotifyInputContext();
        } finally {
            toolkit.unlockAWT();
        }
    }
    private void removeNotifyInputContext() {
        if (!inputMethodsEnabled) {
            return;
        }
        InputContext ic = getInputContext();
        if (ic != null) {
        }
    }
    void moveFocusOnHide() {
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(propertyName, listener);
    }
    public void repaint(long tm, int x, int y, int width, int height) {
    }
    void postEvent(AWTEvent e) {
        getToolkit().getSystemEventQueueImpl().postEvent(e);
    }
    public void repaint(int x, int y, int width, int height) {
        toolkit.lockAWT();
        try {
            repaint(0, x, y, width, height);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void repaint() {
        toolkit.lockAWT();
        try {
            if (w > 0 && h > 0) {
                repaint(0, 0, 0, w, h);
            }
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void repaint(long tm) {
        toolkit.lockAWT();
        try {
            repaint(tm, 0, 0, w, h);
        } finally {
            toolkit.unlockAWT();
        }
    }
    protected boolean requestFocus(boolean temporary) {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
        return false;
    }
    public void requestFocus() {
        toolkit.lockAWT();
        try {
            requestFocus(false);
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void reshape(int x, int y, int w, int h) {
        toolkit.lockAWT();
        try {
            setBounds(x, y, w, h, boundsMaskParam, true);
            boundsMaskParam = 0;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setBounds(int x, int y, int w, int h) {
        toolkit.lockAWT();
        try {
            reshape(x, y, w, h);
        } finally {
            toolkit.unlockAWT();
        }
    }
    void setBounds(int x, int y, int w, int h, int bMask, boolean updateBehavior) {
        int oldX = this.x;
        int oldY = this.y;
        int oldW = this.w;
        int oldH = this.h;
        setBoundsFields(x, y, w, h, bMask);
        if ((oldX != this.x) || (oldY != this.y)) {
            postEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_MOVED));
            spreadHierarchyBoundsEvents(this, HierarchyEvent.ANCESTOR_MOVED);
        }
        if ((oldW != this.w) || (oldH != this.h)) {
            invalidate();
            postEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
            spreadHierarchyBoundsEvents(this, HierarchyEvent.ANCESTOR_RESIZED);
        }
        if (updateBehavior) {
            behaviour.setBounds(this.x, this.y, this.w, this.h, bMask);
        }
        notifyInputMethod(new Rectangle(x, y, w, h));
    }
    void notifyInputMethod(Rectangle bounds) {
    }
    private void setBoundsFields(int x, int y, int w, int h, int bMask) {
        if ((bMask & NativeWindow.BOUNDS_NOSIZE) == 0) {
            this.w = w;
            this.h = h;
        }
        if ((bMask & NativeWindow.BOUNDS_NOMOVE) == 0) {
            this.x = x;
            this.y = y;
        }
    }
    Insets getNativeInsets() {
        return new Insets(0, 0, 0, 0);
    }
    Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }
    boolean isMouseExitedExpected() {
        return mouseExitedExpected;
    }
    void setMouseExitedExpected(boolean expected) {
        mouseExitedExpected = expected;
    }
    public void setBounds(Rectangle r) {
        toolkit.lockAWT();
        try {
            setBounds(r.x, r.y, r.width, r.height);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setComponentOrientation(ComponentOrientation o) {
        ComponentOrientation oldOrientation;
        toolkit.lockAWT();
        try {
            oldOrientation = orientation;
            orientation = o;
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("componentOrientation", oldOrientation, orientation); 
        invalidate();
    }
    public void setCursor(Cursor cursor) {
        toolkit.lockAWT();
        try {
            this.cursor = cursor;
            setCursor();
        } finally {
            toolkit.unlockAWT();
        }
    }
    void setCursor() {
        if (isDisplayable() && isShowing()) {
            Rectangle absRect = new Rectangle(getLocationOnScreen(), getSize());
            Point absPointerPos = toolkit.dispatcher.mouseDispatcher.getPointerPos();
        }
    }
    public void setEnabled(boolean value) {
        toolkit.lockAWT();
        try {
            enable(value);
        } finally {
            toolkit.unlockAWT();
        }
    }
    void setEnabledImpl(boolean value) {
        if (enabled != value) {
            enabled = value;
            setCursor();
            if (!enabled) {
                moveFocusOnHide();
            }
            behaviour.setEnabled(value);
        }
    }
    public void setFocusTraversalKeysEnabled(boolean value) {
        boolean oldFocusTraversalKeysEnabled;
        toolkit.lockAWT();
        try {
            oldFocusTraversalKeysEnabled = focusTraversalKeysEnabled;
            focusTraversalKeysEnabled = value;
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("focusTraversalKeysEnabled", oldFocusTraversalKeysEnabled, 
                focusTraversalKeysEnabled);
    }
    public void setFont(Font f) {
        Font oldFont;
        toolkit.lockAWT();
        try {
            oldFont = font;
            setFontImpl(f);
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("font", oldFont, font); 
    }
    void setFontImpl(Font f) {
        font = f;
        invalidate();
        if (isShowing()) {
            repaint();
        }
    }
    boolean propagateFont() {
        if (font == null) {
            invalidate();
            return true;
        }
        return false;
    }
    public void setForeground(Color c) {
        Color oldFgColor;
        toolkit.lockAWT();
        try {
            oldFgColor = foreColor;
            foreColor = c;
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("foreground", oldFgColor, foreColor); 
        repaint();
    }
    public void setBackground(Color c) {
        Color oldBkColor;
        toolkit.lockAWT();
        try {
            oldBkColor = backColor;
            backColor = c;
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("background", oldBkColor, backColor); 
        repaint();
    }
    public void setIgnoreRepaint(boolean value) {
        toolkit.lockAWT();
        try {
            ignoreRepaint = value;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setLocale(Locale locale) {
        Locale oldLocale;
        toolkit.lockAWT();
        try {
            oldLocale = this.locale;
            this.locale = locale;
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("locale", oldLocale, locale); 
    }
    public void setLocation(Point p) {
        toolkit.lockAWT();
        try {
            setLocation(p.x, p.y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setLocation(int x, int y) {
        toolkit.lockAWT();
        try {
            move(x, y);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setVisible(boolean b) {
        show(b);
    }
    @Deprecated
    public void show() {
        toolkit.lockAWT();
        try {
            if (visible) {
                return;
            }
            prepare4HierarchyChange();
            mapToDisplay(true);
            validate();
            visible = true;
            behaviour.setVisible(true);
            postEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_SHOWN));
            notifyInputMethod(new Rectangle(x, y, w, h));
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public void show(boolean b) {
        if (b) {
            show();
        } else {
            hide();
        }
    }
    public void validate() {
        toolkit.lockAWT();
        try {
            if (!behaviour.isDisplayable()) {
                return;
            }
            validateImpl();
        } finally {
            toolkit.unlockAWT();
        }
    }
    void validateImpl() {
        valid = true;
    }
    NativeWindow getNativeWindow() {
        return behaviour.getNativeWindow();
    }
    public boolean isMaximumSizeSet() {
        toolkit.lockAWT();
        try {
            return maximumSize != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isMinimumSizeSet() {
        toolkit.lockAWT();
        try {
            return minimumSize != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public boolean isPreferredSizeSet() {
        toolkit.lockAWT();
        try {
            return preferredSize != null;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Dimension getMaximumSize() {
        toolkit.lockAWT();
        try {
            return isMaximumSizeSet() ? new Dimension(maximumSize) : new Dimension(Short.MAX_VALUE,
                    Short.MAX_VALUE);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Dimension getMinimumSize() {
        toolkit.lockAWT();
        try {
            return minimumSize();
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public Dimension minimumSize() {
        toolkit.lockAWT();
        try {
            if (isMinimumSizeSet()) {
                return (Dimension)minimumSize.clone();
            }
            Dimension defSize = getDefaultMinimumSize();
            if (defSize != null) {
                return (Dimension)defSize.clone();
            }
            return isDisplayable() ? new Dimension(1, 1) : new Dimension(w, h);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Dimension getPreferredSize() {
        toolkit.lockAWT();
        try {
            return preferredSize();
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Deprecated
    public Dimension preferredSize() {
        toolkit.lockAWT();
        try {
            if (isPreferredSizeSet()) {
                return new Dimension(preferredSize);
            }
            Dimension defSize = getDefaultPreferredSize();
            if (defSize != null) {
                return new Dimension(defSize);
            }
            return new Dimension(getMinimumSize());
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setMaximumSize(Dimension maximumSize) {
        Dimension oldMaximumSize;
        toolkit.lockAWT();
        try {
            oldMaximumSize = this.maximumSize;
            if (oldMaximumSize != null) {
                oldMaximumSize = oldMaximumSize.getSize();
            }
            if (this.maximumSize == null) {
                if (maximumSize != null) {
                    this.maximumSize = new Dimension(maximumSize);
                }
            } else {
                if (maximumSize != null) {
                    this.maximumSize.setSize(maximumSize);
                } else {
                    this.maximumSize = null;
                }
            }
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("maximumSize", oldMaximumSize, this.maximumSize); 
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setMinimumSize(Dimension minimumSize) {
        Dimension oldMinimumSize;
        toolkit.lockAWT();
        try {
            oldMinimumSize = this.minimumSize;
            if (oldMinimumSize != null) {
                oldMinimumSize = oldMinimumSize.getSize();
            }
            if (this.minimumSize == null) {
                if (minimumSize != null) {
                    this.minimumSize = new Dimension(minimumSize);
                }
            } else {
                if (minimumSize != null) {
                    this.minimumSize.setSize(minimumSize);
                } else {
                    this.minimumSize = null;
                }
            }
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("minimumSize", oldMinimumSize, this.minimumSize); 
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setPreferredSize(Dimension preferredSize) {
        Dimension oldPreferredSize;
        toolkit.lockAWT();
        try {
            oldPreferredSize = this.preferredSize;
            if (oldPreferredSize != null) {
                oldPreferredSize = oldPreferredSize.getSize();
            }
            if (this.preferredSize == null) {
                if (preferredSize != null) {
                    this.preferredSize = new Dimension(preferredSize);
                }
            } else {
                if (preferredSize != null) {
                    this.preferredSize.setSize(preferredSize);
                } else {
                    this.preferredSize = null;
                }
            }
        } finally {
            toolkit.unlockAWT();
        }
        firePropertyChange("preferredSize", oldPreferredSize, this.preferredSize); 
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    boolean isFocusabilityExplicitlySet() {
        return calledSetFocusable || overridenIsFocusable;
    }
    public void paintAll(Graphics g) {
        toolkit.lockAWT();
        try {
            paint(g);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void update(Graphics g) {
        toolkit.lockAWT();
        try {
            if (!isLightweight() && !isPrepainter()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, w, h);
                g.setColor(getForeground());
            }
            paint(g);
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void paint(Graphics g) {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    void prepaint(Graphics g) {
    }
    boolean isPrepainter() {
        return false;
    }
    void prepare4HierarchyChange() {
        if (hierarchyChangingCounter++ == 0) {
            wasShowing = isShowing();
            wasDisplayable = isDisplayable();
            prepareChildren4HierarchyChange();
        }
    }
    void prepareChildren4HierarchyChange() {
    }
    void spreadHierarchyBoundsEvents(Component changed, int id) {
    }
    public final void dispatchEvent(AWTEvent e) {
    }
    private void postDeprecatedEvent(AWTEvent e) {
        if (deprecatedEventHandler) {
            Event evt = e.getEvent();
            if (evt != null) {
                postEvent(evt);
            }
        }
    }
    void postprocessEvent(AWTEvent e, long eventMask) {
        toolkit.lockAWT();
        try {
            if (eventMask == AWTEvent.FOCUS_EVENT_MASK) {
                preprocessFocusEvent((FocusEvent)e);
            } else if (eventMask == AWTEvent.KEY_EVENT_MASK) {
                preprocessKeyEvent((KeyEvent)e);
            } else if (eventMask == AWTEvent.MOUSE_EVENT_MASK) {
                preprocessMouseEvent((MouseEvent)e);
            } else if (eventMask == AWTEvent.MOUSE_MOTION_EVENT_MASK) {
                preprocessMouseMotionEvent((MouseEvent)e);
            } else if (eventMask == AWTEvent.COMPONENT_EVENT_MASK) {
                preprocessComponentEvent((ComponentEvent)e);
            } else if (eventMask == AWTEvent.MOUSE_WHEEL_EVENT_MASK) {
                preprocessMouseWheelEvent((MouseWheelEvent)e);
            } else if (eventMask == AWTEvent.INPUT_METHOD_EVENT_MASK) {
                preprocessInputMethodEvent((InputMethodEvent)e);
            }
        } finally {
            toolkit.unlockAWT();
        }
    }
    private void preprocessInputMethodEvent(InputMethodEvent e) {
        processInputMethodEventImpl(e, inputMethodListeners.getSystemListeners());
    }
    private void preprocessMouseWheelEvent(MouseWheelEvent e) {
        processMouseWheelEventImpl(e, mouseWheelListeners.getSystemListeners());
    }
    private void processMouseWheelEventImpl(MouseWheelEvent e, Collection<MouseWheelListener> c) {
        for (MouseWheelListener listener : c) {
            switch (e.getID()) {
                case MouseEvent.MOUSE_WHEEL:
                    listener.mouseWheelMoved(e);
                    break;
            }
        }
    }
    private void preprocessComponentEvent(ComponentEvent e) {
        processComponentEventImpl(e, componentListeners.getSystemListeners());
    }
    void preprocessMouseMotionEvent(MouseEvent e) {
        processMouseMotionEventImpl(e, mouseMotionListeners.getSystemListeners());
    }
    void preprocessMouseEvent(MouseEvent e) {
        processMouseEventImpl(e, mouseListeners.getSystemListeners());
    }
    void preprocessKeyEvent(KeyEvent e) {
        processKeyEventImpl(e, keyListeners.getSystemListeners());
    }
    void preprocessFocusEvent(FocusEvent e) {
        processFocusEventImpl(e, focusListeners.getSystemListeners());
    }
    protected void processEvent(AWTEvent e) {
        long eventMask = toolkit.eventTypeLookup.getEventMask(e);
        if (eventMask == AWTEvent.COMPONENT_EVENT_MASK) {
            processComponentEvent((ComponentEvent)e);
        } else if (eventMask == AWTEvent.FOCUS_EVENT_MASK) {
            processFocusEvent((FocusEvent)e);
        } else if (eventMask == AWTEvent.KEY_EVENT_MASK) {
            processKeyEvent((KeyEvent)e);
        } else if (eventMask == AWTEvent.MOUSE_EVENT_MASK) {
            processMouseEvent((MouseEvent)e);
        } else if (eventMask == AWTEvent.MOUSE_WHEEL_EVENT_MASK) {
            processMouseWheelEvent((MouseWheelEvent)e);
        } else if (eventMask == AWTEvent.MOUSE_MOTION_EVENT_MASK) {
            processMouseMotionEvent((MouseEvent)e);
        } else if (eventMask == AWTEvent.HIERARCHY_EVENT_MASK) {
            processHierarchyEvent((HierarchyEvent)e);
        } else if (eventMask == AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) {
            processHierarchyBoundsEvent((HierarchyEvent)e);
        } else if (eventMask == AWTEvent.INPUT_METHOD_EVENT_MASK) {
            processInputMethodEvent((InputMethodEvent)e);
        }
    }
    @SuppressWarnings("unchecked")
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        if (ComponentListener.class.isAssignableFrom(listenerType)) {
            return (T[])getComponentListeners();
        } else if (FocusListener.class.isAssignableFrom(listenerType)) {
            return (T[])getFocusListeners();
        } else if (HierarchyBoundsListener.class.isAssignableFrom(listenerType)) {
            return (T[])getHierarchyBoundsListeners();
        } else if (HierarchyListener.class.isAssignableFrom(listenerType)) {
            return (T[])getHierarchyListeners();
        } else if (InputMethodListener.class.isAssignableFrom(listenerType)) {
            return (T[])getInputMethodListeners();
        } else if (KeyListener.class.isAssignableFrom(listenerType)) {
            return (T[])getKeyListeners();
        } else if (MouseWheelListener.class.isAssignableFrom(listenerType)) {
            return (T[])getMouseWheelListeners();
        } else if (MouseMotionListener.class.isAssignableFrom(listenerType)) {
            return (T[])getMouseMotionListeners();
        } else if (MouseListener.class.isAssignableFrom(listenerType)) {
            return (T[])getMouseListeners();
        } else if (PropertyChangeListener.class.isAssignableFrom(listenerType)) {
            return (T[])getPropertyChangeListeners();
        }
        return (T[])Array.newInstance(listenerType, 0);
    }
    private void processPaintEvent(PaintEvent event) {
        if (redrawManager == null) {
            return;
        }
        Rectangle clipRect = event.getUpdateRect();
        if ((clipRect.width <= 0) || (clipRect.height <= 0)) {
            return;
        }
        Graphics g = getGraphics();
        if (g == null) {
            return;
        }
        initGraphics(g, event);
        if (!getIgnoreRepaint()) {
            if (event.getID() == PaintEvent.PAINT) {
                paint(g);
            } else {
                update(g);
            }
        }
        g.dispose();
    }
    void initGraphics(Graphics g, PaintEvent e) {
        Rectangle clip = e.getUpdateRect();
        if (clip instanceof ClipRegion) {
            g.setClip(((ClipRegion)clip).getClip());
        } else {
            g.setClip(clip);
        }
        if (isPrepainter()) {
            prepaint(g);
        } else if (!isLightweight() && (e.getID() == PaintEvent.PAINT)) {
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
        }
        g.setFont(getFont());
        g.setColor(getForeground());
    }
    protected final void enableEvents(long eventsToEnable) {
        toolkit.lockAWT();
        try {
            enabledEvents |= eventsToEnable;
            deprecatedEventHandler = false;
        } finally {
            toolkit.unlockAWT();
        }
    }
    private void enableAWTEvents(long eventsToEnable) {
        enabledAWTEvents |= eventsToEnable;
    }
    protected final void disableEvents(long eventsToDisable) {
        toolkit.lockAWT();
        try {
            enabledEvents &= ~eventsToDisable;
        } finally {
            toolkit.unlockAWT();
        }
    }
    boolean isMouseEventEnabled(long eventMask) {
        return (isEventEnabled(eventMask) || (enabledAWTEvents & eventMask) != 0);
    }
    boolean isEventEnabled(long eventMask) {
        return ((enabledEvents & eventMask) != 0);
    }
    public void enableInputMethods(boolean enable) {
        toolkit.lockAWT();
        try {
            if (!enable) {
                removeNotifyInputContext();
            }
            inputMethodsEnabled = enable;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public ComponentListener[] getComponentListeners() {
        return componentListeners.getUserListeners(new ComponentListener[0]);
    }
    public void addComponentListener(ComponentListener l) {
        componentListeners.addUserListener(l);
    }
    public void removeComponentListener(ComponentListener l) {
        componentListeners.removeUserListener(l);
    }
    protected void processComponentEvent(ComponentEvent e) {
        processComponentEventImpl(e, componentListeners.getUserListeners());
    }
    private void processComponentEventImpl(ComponentEvent e, Collection<ComponentListener> c) {
        for (ComponentListener listener : c) {
            switch (e.getID()) {
                case ComponentEvent.COMPONENT_HIDDEN:
                    listener.componentHidden(e);
                    break;
                case ComponentEvent.COMPONENT_MOVED:
                    listener.componentMoved(e);
                    break;
                case ComponentEvent.COMPONENT_RESIZED:
                    listener.componentResized(e);
                    break;
                case ComponentEvent.COMPONENT_SHOWN:
                    listener.componentShown(e);
                    break;
            }
        }
    }
    public FocusListener[] getFocusListeners() {
        return focusListeners.getUserListeners(new FocusListener[0]);
    }
    public void addFocusListener(FocusListener l) {
        focusListeners.addUserListener(l);
    }
    void addAWTFocusListener(FocusListener l) {
        enableAWTEvents(AWTEvent.FOCUS_EVENT_MASK);
        focusListeners.addSystemListener(l);
    }
    public void removeFocusListener(FocusListener l) {
        focusListeners.removeUserListener(l);
    }
    protected void processFocusEvent(FocusEvent e) {
        processFocusEventImpl(e, focusListeners.getUserListeners());
    }
    private void processFocusEventImpl(FocusEvent e, Collection<FocusListener> c) {
        for (FocusListener listener : c) {
            switch (e.getID()) {
                case FocusEvent.FOCUS_GAINED:
                    listener.focusGained(e);
                    break;
                case FocusEvent.FOCUS_LOST:
                    listener.focusLost(e);
                    break;
            }
        }
    }
    public HierarchyListener[] getHierarchyListeners() {
        return hierarchyListeners.getUserListeners(new HierarchyListener[0]);
    }
    public void addHierarchyListener(HierarchyListener l) {
        hierarchyListeners.addUserListener(l);
    }
    public void removeHierarchyListener(HierarchyListener l) {
        hierarchyListeners.removeUserListener(l);
    }
    protected void processHierarchyEvent(HierarchyEvent e) {
        for (HierarchyListener listener : hierarchyListeners.getUserListeners()) {
            switch (e.getID()) {
                case HierarchyEvent.HIERARCHY_CHANGED:
                    listener.hierarchyChanged(e);
                    break;
            }
        }
    }
    public HierarchyBoundsListener[] getHierarchyBoundsListeners() {
        return hierarchyBoundsListeners.getUserListeners(new HierarchyBoundsListener[0]);
    }
    public void addHierarchyBoundsListener(HierarchyBoundsListener l) {
        hierarchyBoundsListeners.addUserListener(l);
    }
    public void removeHierarchyBoundsListener(HierarchyBoundsListener l) {
        hierarchyBoundsListeners.removeUserListener(l);
    }
    protected void processHierarchyBoundsEvent(HierarchyEvent e) {
        for (HierarchyBoundsListener listener : hierarchyBoundsListeners.getUserListeners()) {
            switch (e.getID()) {
                case HierarchyEvent.ANCESTOR_MOVED:
                    listener.ancestorMoved(e);
                    break;
                case HierarchyEvent.ANCESTOR_RESIZED:
                    listener.ancestorResized(e);
                    break;
            }
        }
    }
    public KeyListener[] getKeyListeners() {
        return keyListeners.getUserListeners(new KeyListener[0]);
    }
    public void addKeyListener(KeyListener l) {
        keyListeners.addUserListener(l);
    }
    void addAWTKeyListener(KeyListener l) {
        enableAWTEvents(AWTEvent.KEY_EVENT_MASK);
        keyListeners.addSystemListener(l);
    }
    public void removeKeyListener(KeyListener l) {
        keyListeners.removeUserListener(l);
    }
    protected void processKeyEvent(KeyEvent e) {
        processKeyEventImpl(e, keyListeners.getUserListeners());
    }
    private void processKeyEventImpl(KeyEvent e, Collection<KeyListener> c) {
        for (KeyListener listener : c) {
            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    listener.keyPressed(e);
                    break;
                case KeyEvent.KEY_RELEASED:
                    listener.keyReleased(e);
                    break;
                case KeyEvent.KEY_TYPED:
                    listener.keyTyped(e);
                    break;
            }
        }
    }
    public MouseListener[] getMouseListeners() {
        return mouseListeners.getUserListeners(new MouseListener[0]);
    }
    public void addMouseListener(MouseListener l) {
        mouseListeners.addUserListener(l);
    }
    void addAWTMouseListener(MouseListener l) {
        enableAWTEvents(AWTEvent.MOUSE_EVENT_MASK);
        mouseListeners.addSystemListener(l);
    }
    void addAWTMouseMotionListener(MouseMotionListener l) {
        enableAWTEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        mouseMotionListeners.addSystemListener(l);
    }
    void addAWTComponentListener(ComponentListener l) {
        enableAWTEvents(AWTEvent.COMPONENT_EVENT_MASK);
        componentListeners.addSystemListener(l);
    }
    void addAWTInputMethodListener(InputMethodListener l) {
        enableAWTEvents(AWTEvent.INPUT_METHOD_EVENT_MASK);
        inputMethodListeners.addSystemListener(l);
    }
    void addAWTMouseWheelListener(MouseWheelListener l) {
        enableAWTEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        mouseWheelListeners.addSystemListener(l);
    }
    public void removeMouseListener(MouseListener l) {
        mouseListeners.removeUserListener(l);
    }
    protected void processMouseEvent(MouseEvent e) {
        processMouseEventImpl(e, mouseListeners.getUserListeners());
    }
    private void processMouseEventImpl(MouseEvent e, Collection<MouseListener> c) {
        for (MouseListener listener : c) {
            switch (e.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    listener.mouseEntered(e);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    listener.mouseExited(e);
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mouseReleased(e);
                    break;
            }
        }
    }
    private void processMouseMotionEventImpl(MouseEvent e, Collection<MouseMotionListener> c) {
        for (MouseMotionListener listener : c) {
            switch (e.getID()) {
                case MouseEvent.MOUSE_DRAGGED:
                    listener.mouseDragged(e);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    listener.mouseMoved(e);
                    break;
            }
        }
    }
    public MouseMotionListener[] getMouseMotionListeners() {
        return mouseMotionListeners.getUserListeners(new MouseMotionListener[0]);
    }
    public void addMouseMotionListener(MouseMotionListener l) {
        mouseMotionListeners.addUserListener(l);
    }
    public void removeMouseMotionListener(MouseMotionListener l) {
        mouseMotionListeners.removeUserListener(l);
    }
    protected void processMouseMotionEvent(MouseEvent e) {
        processMouseMotionEventImpl(e, mouseMotionListeners.getUserListeners());
    }
    public MouseWheelListener[] getMouseWheelListeners() {
        return mouseWheelListeners.getUserListeners(new MouseWheelListener[0]);
    }
    public void addMouseWheelListener(MouseWheelListener l) {
        mouseWheelListeners.addUserListener(l);
    }
    public void removeMouseWheelListener(MouseWheelListener l) {
        mouseWheelListeners.removeUserListener(l);
    }
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        processMouseWheelEventImpl(e, mouseWheelListeners.getUserListeners());
    }
    public InputMethodListener[] getInputMethodListeners() {
        return inputMethodListeners.getUserListeners(new InputMethodListener[0]);
    }
    public void addInputMethodListener(InputMethodListener l) {
        inputMethodListeners.addUserListener(l);
    }
    public void removeInputMethodListener(InputMethodListener l) {
        inputMethodListeners.removeUserListener(l);
    }
    protected void processInputMethodEvent(InputMethodEvent e) {
        processInputMethodEventImpl(e, inputMethodListeners.getUserListeners());
    }
    private void processInputMethodEventImpl(InputMethodEvent e, Collection<InputMethodListener> c) {
        for (InputMethodListener listener : c) {
            switch (e.getID()) {
                case InputMethodEvent.CARET_POSITION_CHANGED:
                    listener.caretPositionChanged(e);
                    break;
                case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
                    listener.inputMethodTextChanged(e);
                    break;
            }
        }
    }
    void setCaretPos(final int x, final int y) {
        Runnable r = new Runnable() {
            public void run() {
                toolkit.lockAWT();
                try {
                    setCaretPosImpl(x, y);
                } finally {
                    toolkit.unlockAWT();
                }
            }
        };
        if (Thread.currentThread() instanceof EventDispatchThread) {
            r.run();
        } else {
            toolkit.getSystemEventQueueImpl().postEvent(new InvocationEvent(this, r));
        }
    }
    void setCaretPosImpl(int x, int y) {
        Component c = this;
        while ((c != null) && c.behaviour.isLightweight()) {
            x += c.x;
            y += c.y;
        }
        if (c == null) {
            return;
        }
    }
    Dimension getDefaultMinimumSize() {
        return null;
    }
    Dimension getDefaultPreferredSize() {
        return null;
    }
    void resetDefaultSize() {
    }
    Color getDefaultBackground() {
        return getBackground();
    }
    Color getDefaultForeground() {
        return getForeground();
    }
    void nativeWindowCreated(NativeWindow win) {
    }
    final void onDrawImage(Image image, Point destLocation, Dimension destSize, Rectangle source) {
        ImageParameters imageParams;
        if (updatedImages == null) {
            updatedImages = new HashMap<Image, ImageParameters>();
        }
        imageParams = updatedImages.get(image);
        if (imageParams == null) {
            imageParams = new ImageParameters();
            updatedImages.put(image, imageParams);
        }
        imageParams.addDrawing(destLocation, destSize, source);
    }
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        toolkit.lockAWT();
        try {
            boolean done = false;
            if ((infoflags & (ALLBITS | FRAMEBITS)) != 0) {
                done = true;
            } else if ((infoflags & SOMEBITS) != 0 && incrementalImageUpdate) {
                done = true;
            }
            if (done) {
                repaint();
            }
            return (infoflags & (ABORT | ALLBITS)) == 0;
        } finally {
            toolkit.unlockAWT();
        }
    }
    private class ImageParameters {
        private final LinkedList<DrawingParameters> drawingParams = new LinkedList<DrawingParameters>();
        Dimension size = new Dimension(Component.this.w, Component.this.h);
        void addDrawing(Point destLocation, Dimension destSize, Rectangle source) {
            drawingParams.add(new DrawingParameters(destLocation, destSize, source));
        }
        Iterator<DrawingParameters> drawingParametersIterator() {
            return drawingParams.iterator();
        }
        class DrawingParameters {
            Point destLocation;
            Dimension destSize;
            Rectangle source;
            DrawingParameters(Point destLocation, Dimension destSize, Rectangle source) {
                this.destLocation = new Point(destLocation);
                if (destSize != null) {
                    this.destSize = new Dimension(destSize);
                } else {
                    this.destSize = null;
                }
                if (source != null) {
                    this.source = new Rectangle(source);
                } else {
                    this.source = null;
                }
            }
        }
    }
    private boolean dispatchEventToIM(AWTEvent e) {
        InputContext ic = getInputContext();
        if (ic == null) {
            return false;
        }
        int id = e.getID();
        boolean isInputEvent = ((id >= KeyEvent.KEY_FIRST) && (id <= KeyEvent.KEY_LAST))
                || ((id >= MouseEvent.MOUSE_FIRST) && (id <= MouseEvent.MOUSE_LAST));
        if (((id >= FocusEvent.FOCUS_FIRST) && (id <= FocusEvent.FOCUS_LAST)) || isInputEvent) {
            ic.dispatchEvent(e);
        }
        return e.isConsumed();
    }
}
