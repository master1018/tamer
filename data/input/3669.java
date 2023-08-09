public abstract class KeyboardFocusManager
    implements KeyEventDispatcher, KeyEventPostProcessor
{
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.KeyboardFocusManager");
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setKeyboardFocusManagerAccessor(
            new AWTAccessor.KeyboardFocusManagerAccessor() {
                public int shouldNativelyFocusHeavyweight(Component heavyweight,
                                                   Component descendant,
                                                   boolean temporary,
                                                   boolean focusedWindowChangeAllowed,
                                                   long time,
                                                   CausedFocusEvent.Cause cause)
                {
                    return KeyboardFocusManager.shouldNativelyFocusHeavyweight(
                        heavyweight, descendant, temporary, focusedWindowChangeAllowed, time, cause);
                }
                public boolean processSynchronousLightweightTransfer(Component heavyweight,
                                                              Component descendant,
                                                              boolean temporary,
                                                              boolean focusedWindowChangeAllowed,
                                                              long time)
                {
                    return KeyboardFocusManager.processSynchronousLightweightTransfer(
                        heavyweight, descendant, temporary, focusedWindowChangeAllowed, time);
                }
                public void removeLastFocusRequest(Component heavyweight) {
                    KeyboardFocusManager.removeLastFocusRequest(heavyweight);
                }
                public void setMostRecentFocusOwner(Window window, Component component) {
                    KeyboardFocusManager.setMostRecentFocusOwner(window, component);
                }
            }
        );
    }
    transient KeyboardFocusManagerPeer peer;
    private static native void initIDs();
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.KeyboardFocusManager");
    public static final int FORWARD_TRAVERSAL_KEYS = 0;
    public static final int BACKWARD_TRAVERSAL_KEYS = 1;
    public static final int UP_CYCLE_TRAVERSAL_KEYS = 2;
    public static final int DOWN_CYCLE_TRAVERSAL_KEYS = 3;
    static final int TRAVERSAL_KEY_LENGTH = DOWN_CYCLE_TRAVERSAL_KEYS + 1;
    public static KeyboardFocusManager getCurrentKeyboardFocusManager() {
        return getCurrentKeyboardFocusManager(AppContext.getAppContext());
    }
    synchronized static KeyboardFocusManager
        getCurrentKeyboardFocusManager(AppContext appcontext)
    {
        KeyboardFocusManager manager = (KeyboardFocusManager)
            appcontext.get(KeyboardFocusManager.class);
        if (manager == null) {
            manager = new DefaultKeyboardFocusManager();
            appcontext.put(KeyboardFocusManager.class, manager);
        }
        return manager;
    }
    public static void setCurrentKeyboardFocusManager(
        KeyboardFocusManager newManager) throws SecurityException
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (replaceKeyboardFocusManagerPermission == null) {
                replaceKeyboardFocusManagerPermission =
                    new AWTPermission("replaceKeyboardFocusManager");
            }
            security.
                checkPermission(replaceKeyboardFocusManagerPermission);
        }
        KeyboardFocusManager oldManager = null;
        synchronized (KeyboardFocusManager.class) {
            AppContext appcontext = AppContext.getAppContext();
            if (newManager != null) {
                oldManager = getCurrentKeyboardFocusManager(appcontext);
                appcontext.put(KeyboardFocusManager.class, newManager);
            } else {
                oldManager = getCurrentKeyboardFocusManager(appcontext);
                appcontext.remove(KeyboardFocusManager.class);
            }
        }
        if (oldManager != null) {
            oldManager.firePropertyChange("managingFocus",
                                          Boolean.TRUE,
                                          Boolean.FALSE);
        }
        if (newManager != null) {
            newManager.firePropertyChange("managingFocus",
                                          Boolean.FALSE,
                                          Boolean.TRUE);
        }
    }
    private static Component focusOwner;
    private static Component permanentFocusOwner;
    private static Window focusedWindow;
    private static Window activeWindow;
    private FocusTraversalPolicy defaultPolicy =
        new DefaultFocusTraversalPolicy();
    private static final String[] defaultFocusTraversalKeyPropertyNames = {
        "forwardDefaultFocusTraversalKeys",
        "backwardDefaultFocusTraversalKeys",
        "upCycleDefaultFocusTraversalKeys",
        "downCycleDefaultFocusTraversalKeys"
    };
    private static final AWTKeyStroke[][] defaultFocusTraversalKeyStrokes = {
        {
            AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0, false),
            AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK, false),
        },
        {
            AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK, false),
            AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
                                         InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK,
                                         false),
        },
        {},
        {},
      };
    private Set[] defaultFocusTraversalKeys = new Set[4];
    private static Container currentFocusCycleRoot;
    private VetoableChangeSupport vetoableSupport;
    private PropertyChangeSupport changeSupport;
    private java.util.LinkedList keyEventDispatchers;
    private java.util.LinkedList keyEventPostProcessors;
    private static java.util.Map mostRecentFocusOwners = new WeakHashMap();
    private static final String notPrivileged = "this KeyboardFocusManager is not installed in the current thread's context";
    private static AWTPermission replaceKeyboardFocusManagerPermission;
    transient SequencedEvent currentSequencedEvent = null;
    final void setCurrentSequencedEvent(SequencedEvent current) {
        synchronized (SequencedEvent.class) {
            assert(current == null || currentSequencedEvent == null);
            currentSequencedEvent = current;
        }
    }
    final SequencedEvent getCurrentSequencedEvent() {
        synchronized (SequencedEvent.class) {
            return currentSequencedEvent;
        }
    }
    static Set initFocusTraversalKeysSet(String value, Set targetSet) {
        StringTokenizer tokens = new StringTokenizer(value, ",");
        while (tokens.hasMoreTokens()) {
            targetSet.add(AWTKeyStroke.getAWTKeyStroke(tokens.nextToken()));
        }
        return (targetSet.isEmpty())
            ? Collections.EMPTY_SET
            : Collections.unmodifiableSet(targetSet);
    }
    public KeyboardFocusManager() {
        for (int i = 0; i < TRAVERSAL_KEY_LENGTH; i++) {
            Set work_set = new HashSet();
            for (int j = 0; j < defaultFocusTraversalKeyStrokes[i].length; j++) {
                work_set.add(defaultFocusTraversalKeyStrokes[i][j]);
            }
            defaultFocusTraversalKeys[i] = (work_set.isEmpty())
                ? Collections.EMPTY_SET
                : Collections.unmodifiableSet(work_set);
        }
        initPeer();
    }
    private void initPeer() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        KeyboardFocusManagerPeerProvider peerProvider = (KeyboardFocusManagerPeerProvider)tk;
        peer = peerProvider.createKeyboardFocusManagerPeer(this);
    }
    public Component getFocusOwner() {
        synchronized (KeyboardFocusManager.class) {
            if (focusOwner == null) {
                return null;
            }
            return (focusOwner.appContext == AppContext.getAppContext())
                ? focusOwner
                : null;
        }
    }
    protected Component getGlobalFocusOwner() throws SecurityException {
        synchronized (KeyboardFocusManager.class) {
            if (this == getCurrentKeyboardFocusManager()) {
                return focusOwner;
            } else {
                if (focusLog.isLoggable(PlatformLogger.FINER)) {
                    focusLog.finer("This manager is " + this + ", current is " + getCurrentKeyboardFocusManager());
                }
                throw new SecurityException(notPrivileged);
            }
        }
    }
    protected void setGlobalFocusOwner(Component focusOwner) {
        Component oldFocusOwner = null;
        boolean shouldFire = false;
        if (focusOwner == null || focusOwner.isFocusable()) {
            synchronized (KeyboardFocusManager.class) {
                oldFocusOwner = getFocusOwner();
                try {
                    fireVetoableChange("focusOwner", oldFocusOwner,
                                       focusOwner);
                } catch (PropertyVetoException e) {
                    return;
                }
                KeyboardFocusManager.focusOwner = focusOwner;
                if (focusOwner != null &&
                    (getCurrentFocusCycleRoot() == null ||
                     !focusOwner.isFocusCycleRoot(getCurrentFocusCycleRoot())))
                {
                    Container rootAncestor =
                        focusOwner.getFocusCycleRootAncestor();
                    if (rootAncestor == null && (focusOwner instanceof Window))
                    {
                        rootAncestor = (Container)focusOwner;
                    }
                    if (rootAncestor != null) {
                        setGlobalCurrentFocusCycleRoot(rootAncestor);
                    }
                }
                shouldFire = true;
            }
        }
        if (shouldFire) {
            firePropertyChange("focusOwner", oldFocusOwner, focusOwner);
        }
    }
    public void clearGlobalFocusOwner() {
        if (!GraphicsEnvironment.isHeadless()) {
            Toolkit.getDefaultToolkit();
            _clearGlobalFocusOwner();
        }
    }
    private void _clearGlobalFocusOwner() {
        Window activeWindow = markClearGlobalFocusOwner();
        peer.clearGlobalFocusOwner(activeWindow);
    }
    Component getNativeFocusOwner() {
        return peer.getCurrentFocusOwner();
    }
    void setNativeFocusOwner(Component comp) {
        if (focusLog.isLoggable(PlatformLogger.FINEST)) {
            focusLog.finest("Calling peer {0} setCurrentFocusOwner for {1}",
                            String.valueOf(peer), String.valueOf(comp));
        }
        peer.setCurrentFocusOwner(comp);
    }
    Window getNativeFocusedWindow() {
        return peer.getCurrentFocusedWindow();
    }
    public Component getPermanentFocusOwner() {
        synchronized (KeyboardFocusManager.class) {
            if (permanentFocusOwner == null) {
                return null;
            }
            return (permanentFocusOwner.appContext ==
                    AppContext.getAppContext())
                ? permanentFocusOwner
                : null;
        }
    }
    protected Component getGlobalPermanentFocusOwner()
        throws SecurityException
    {
        synchronized (KeyboardFocusManager.class) {
            if (this == getCurrentKeyboardFocusManager()) {
                return permanentFocusOwner;
            } else {
                if (focusLog.isLoggable(PlatformLogger.FINER)) {
                    focusLog.finer("This manager is " + this + ", current is " + getCurrentKeyboardFocusManager());
                }
                throw new SecurityException(notPrivileged);
            }
        }
    }
    protected void setGlobalPermanentFocusOwner(Component permanentFocusOwner)
    {
        Component oldPermanentFocusOwner = null;
        boolean shouldFire = false;
        if (permanentFocusOwner == null || permanentFocusOwner.isFocusable()) {
            synchronized (KeyboardFocusManager.class) {
                oldPermanentFocusOwner = getPermanentFocusOwner();
                try {
                    fireVetoableChange("permanentFocusOwner",
                                       oldPermanentFocusOwner,
                                       permanentFocusOwner);
                } catch (PropertyVetoException e) {
                    return;
                }
                KeyboardFocusManager.permanentFocusOwner = permanentFocusOwner;
                KeyboardFocusManager.
                    setMostRecentFocusOwner(permanentFocusOwner);
                shouldFire = true;
            }
        }
        if (shouldFire) {
            firePropertyChange("permanentFocusOwner", oldPermanentFocusOwner,
                               permanentFocusOwner);
        }
    }
    public Window getFocusedWindow() {
        synchronized (KeyboardFocusManager.class) {
            if (focusedWindow == null) {
                return null;
            }
            return (focusedWindow.appContext == AppContext.getAppContext())
                ? focusedWindow
                : null;
        }
    }
    protected Window getGlobalFocusedWindow() throws SecurityException {
        synchronized (KeyboardFocusManager.class) {
            if (this == getCurrentKeyboardFocusManager()) {
               return focusedWindow;
            } else {
                if (focusLog.isLoggable(PlatformLogger.FINER)) {
                    focusLog.finer("This manager is " + this + ", current is " + getCurrentKeyboardFocusManager());
                }
                throw new SecurityException(notPrivileged);
            }
        }
    }
    protected void setGlobalFocusedWindow(Window focusedWindow) {
        Window oldFocusedWindow = null;
        boolean shouldFire = false;
        if (focusedWindow == null || focusedWindow.isFocusableWindow()) {
            synchronized (KeyboardFocusManager.class) {
                oldFocusedWindow = getFocusedWindow();
                try {
                    fireVetoableChange("focusedWindow", oldFocusedWindow,
                                       focusedWindow);
                } catch (PropertyVetoException e) {
                    return;
                }
                KeyboardFocusManager.focusedWindow = focusedWindow;
                shouldFire = true;
            }
        }
        if (shouldFire) {
            firePropertyChange("focusedWindow", oldFocusedWindow,
                               focusedWindow);
        }
    }
    public Window getActiveWindow() {
        synchronized (KeyboardFocusManager.class) {
            if (activeWindow == null) {
                return null;
            }
            return (activeWindow.appContext == AppContext.getAppContext())
                ? activeWindow
                : null;
        }
    }
    protected Window getGlobalActiveWindow() throws SecurityException {
        synchronized (KeyboardFocusManager.class) {
            if (this == getCurrentKeyboardFocusManager()) {
               return activeWindow;
            } else {
                if (focusLog.isLoggable(PlatformLogger.FINER)) {
                    focusLog.finer("This manager is " + this + ", current is " + getCurrentKeyboardFocusManager());
                }
                throw new SecurityException(notPrivileged);
            }
        }
    }
    protected void setGlobalActiveWindow(Window activeWindow) {
        Window oldActiveWindow;
        synchronized (KeyboardFocusManager.class) {
            oldActiveWindow = getActiveWindow();
            if (focusLog.isLoggable(PlatformLogger.FINER)) {
                focusLog.finer("Setting global active window to " + activeWindow + ", old active " + oldActiveWindow);
            }
            try {
                fireVetoableChange("activeWindow", oldActiveWindow,
                                   activeWindow);
            } catch (PropertyVetoException e) {
                return;
            }
            KeyboardFocusManager.activeWindow = activeWindow;
        }
        firePropertyChange("activeWindow", oldActiveWindow, activeWindow);
    }
    public synchronized FocusTraversalPolicy getDefaultFocusTraversalPolicy() {
        return defaultPolicy;
    }
    public void setDefaultFocusTraversalPolicy(FocusTraversalPolicy
                                               defaultPolicy) {
        if (defaultPolicy == null) {
            throw new IllegalArgumentException("default focus traversal policy cannot be null");
        }
        FocusTraversalPolicy oldPolicy;
        synchronized (this) {
            oldPolicy = this.defaultPolicy;
            this.defaultPolicy = defaultPolicy;
        }
        firePropertyChange("defaultFocusTraversalPolicy", oldPolicy,
                           defaultPolicy);
    }
    public void
        setDefaultFocusTraversalKeys(int id,
                                     Set<? extends AWTKeyStroke> keystrokes)
    {
        if (id < 0 || id >= TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        if (keystrokes == null) {
            throw new IllegalArgumentException("cannot set null Set of default focus traversal keys");
        }
        Set oldKeys;
        synchronized (this) {
            for (Iterator iter = keystrokes.iterator(); iter.hasNext(); ) {
                Object obj = iter.next();
                if (obj == null) {
                    throw new IllegalArgumentException("cannot set null focus traversal key");
                }
                if (!(obj instanceof AWTKeyStroke)) {
                    throw new IllegalArgumentException("object is expected to be AWTKeyStroke");
                }
                AWTKeyStroke keystroke = (AWTKeyStroke)obj;
                if (keystroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                    throw new IllegalArgumentException("focus traversal keys cannot map to KEY_TYPED events");
                }
                for (int i = 0; i < TRAVERSAL_KEY_LENGTH; i++) {
                    if (i == id) {
                        continue;
                    }
                    if (defaultFocusTraversalKeys[i].contains(keystroke)) {
                        throw new IllegalArgumentException("focus traversal keys must be unique for a Component");
                    }
                }
            }
            oldKeys = defaultFocusTraversalKeys[id];
            defaultFocusTraversalKeys[id] =
                Collections.unmodifiableSet(new HashSet(keystrokes));
        }
        firePropertyChange(defaultFocusTraversalKeyPropertyNames[id],
                           oldKeys, keystrokes);
    }
    public Set<AWTKeyStroke> getDefaultFocusTraversalKeys(int id) {
        if (id < 0 || id >= TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        return defaultFocusTraversalKeys[id];
    }
    public Container getCurrentFocusCycleRoot() {
        synchronized (KeyboardFocusManager.class) {
            if (currentFocusCycleRoot == null) {
                return null;
            }
            return (currentFocusCycleRoot.appContext ==
                    AppContext.getAppContext())
                ? currentFocusCycleRoot
                : null;
        }
    }
    protected Container getGlobalCurrentFocusCycleRoot()
        throws SecurityException
    {
        synchronized (KeyboardFocusManager.class) {
            if (this == getCurrentKeyboardFocusManager()) {
                return currentFocusCycleRoot;
            } else {
                if (focusLog.isLoggable(PlatformLogger.FINER)) {
                    focusLog.finer("This manager is " + this + ", current is " + getCurrentKeyboardFocusManager());
                }
                throw new SecurityException(notPrivileged);
            }
        }
    }
    public void setGlobalCurrentFocusCycleRoot(Container newFocusCycleRoot) {
        Container oldFocusCycleRoot;
        synchronized (KeyboardFocusManager.class) {
            oldFocusCycleRoot  = getCurrentFocusCycleRoot();
            currentFocusCycleRoot = newFocusCycleRoot;
        }
        firePropertyChange("currentFocusCycleRoot", oldFocusCycleRoot,
                           newFocusCycleRoot);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                }
                changeSupport.addPropertyChangeListener(listener);
            }
        }
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport != null) {
                    changeSupport.removePropertyChangeListener(listener);
                }
            }
        }
    }
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        return changeSupport.getPropertyChangeListeners();
    }
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                }
                changeSupport.addPropertyChangeListener(propertyName,
                                                        listener);
            }
        }
    }
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport != null) {
                    changeSupport.removePropertyChangeListener(propertyName,
                                                               listener);
                }
            }
        }
    }
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        return changeSupport.getPropertyChangeListeners(propertyName);
    }
    protected void firePropertyChange(String propertyName, Object oldValue,
                                      Object newValue)
    {
        if (oldValue == newValue) {
            return;
        }
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport != null) {
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport == null) {
                    vetoableSupport =
                        new VetoableChangeSupport(this);
                }
                vetoableSupport.addVetoableChangeListener(listener);
            }
        }
    }
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport != null) {
                    vetoableSupport.removeVetoableChangeListener(listener);
                }
            }
        }
    }
    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        if (vetoableSupport == null) {
            vetoableSupport = new VetoableChangeSupport(this);
        }
        return vetoableSupport.getVetoableChangeListeners();
    }
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport == null) {
                    vetoableSupport =
                        new VetoableChangeSupport(this);
                }
                vetoableSupport.addVetoableChangeListener(propertyName,
                                                          listener);
            }
        }
    }
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport != null) {
                    vetoableSupport.removeVetoableChangeListener(propertyName,
                                                                 listener);
                }
            }
        }
    }
    public synchronized VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        if (vetoableSupport == null) {
            vetoableSupport = new VetoableChangeSupport(this);
        }
        return vetoableSupport.getVetoableChangeListeners(propertyName);
    }
    protected void fireVetoableChange(String propertyName, Object oldValue,
                                      Object newValue)
        throws PropertyVetoException
    {
        if (oldValue == newValue) {
            return;
        }
        VetoableChangeSupport vetoableSupport =
            this.vetoableSupport;
        if (vetoableSupport != null) {
            vetoableSupport.fireVetoableChange(propertyName, oldValue,
                                               newValue);
        }
    }
    public void addKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        if (dispatcher != null) {
            synchronized (this) {
                if (keyEventDispatchers == null) {
                    keyEventDispatchers = new java.util.LinkedList();
                }
                keyEventDispatchers.add(dispatcher);
            }
        }
    }
    public void removeKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        if (dispatcher != null) {
            synchronized (this) {
                if (keyEventDispatchers != null) {
                    keyEventDispatchers.remove(dispatcher);
                }
            }
        }
    }
    protected synchronized java.util.List<KeyEventDispatcher>
        getKeyEventDispatchers()
    {
        return (keyEventDispatchers != null)
            ? (java.util.List)keyEventDispatchers.clone()
            : null;
    }
    public void addKeyEventPostProcessor(KeyEventPostProcessor processor) {
        if (processor != null) {
            synchronized (this) {
                if (keyEventPostProcessors == null) {
                    keyEventPostProcessors = new java.util.LinkedList();
                }
                keyEventPostProcessors.add(processor);
            }
        }
    }
    public void removeKeyEventPostProcessor(KeyEventPostProcessor processor) {
        if (processor != null) {
            synchronized (this) {
                if (keyEventPostProcessors != null) {
                    keyEventPostProcessors.remove(processor);
                }
            }
        }
    }
    protected java.util.List<KeyEventPostProcessor>
        getKeyEventPostProcessors()
    {
        return (keyEventPostProcessors != null)
            ? (java.util.List)keyEventPostProcessors.clone()
            : null;
    }
    static void setMostRecentFocusOwner(Component component) {
        Component window = component;
        while (window != null && !(window instanceof Window)) {
            window = window.parent;
        }
        if (window != null) {
            setMostRecentFocusOwner((Window)window, component);
        }
    }
    static synchronized void setMostRecentFocusOwner(Window window,
                                                     Component component) {
        WeakReference weakValue = null;
        if (component != null) {
            weakValue = new WeakReference(component);
        }
        mostRecentFocusOwners.put(window, weakValue);
    }
    static void clearMostRecentFocusOwner(Component comp) {
        Container window;
        if (comp == null) {
            return;
        }
        synchronized (comp.getTreeLock()) {
            window = comp.getParent();
            while (window != null && !(window instanceof Window)) {
                window = window.getParent();
            }
        }
        synchronized (KeyboardFocusManager.class) {
            if ((window != null)
                && (getMostRecentFocusOwner((Window)window) == comp))
            {
                setMostRecentFocusOwner((Window)window, null);
            }
            if (window != null) {
                Window realWindow = (Window)window;
                if (realWindow.getTemporaryLostComponent() == comp) {
                    realWindow.setTemporaryLostComponent(null);
                }
            }
        }
    }
    static synchronized Component getMostRecentFocusOwner(Window window) {
        WeakReference weakValue =
            (WeakReference)mostRecentFocusOwners.get(window);
        return weakValue == null ? null : (Component)weakValue.get();
    }
    public abstract boolean dispatchEvent(AWTEvent e);
    public final void redispatchEvent(Component target, AWTEvent e) {
        e.focusManagerIsDispatching = true;
        target.dispatchEvent(e);
        e.focusManagerIsDispatching = false;
    }
    public abstract boolean dispatchKeyEvent(KeyEvent e);
    public abstract boolean postProcessKeyEvent(KeyEvent e);
    public abstract void processKeyEvent(Component focusedComponent,
                                         KeyEvent e);
    protected abstract void enqueueKeyEvents(long after,
                                             Component untilFocused);
    protected abstract void dequeueKeyEvents(long after,
                                             Component untilFocused);
    protected abstract void discardKeyEvents(Component comp);
    public abstract void focusNextComponent(Component aComponent);
    public abstract void focusPreviousComponent(Component aComponent);
    public abstract void upFocusCycle(Component aComponent);
    public abstract void downFocusCycle(Container aContainer);
    public final void focusNextComponent() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null) {
            focusNextComponent(focusOwner);
        }
    }
    public final void focusPreviousComponent() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null) {
            focusPreviousComponent(focusOwner);
        }
    }
    public final void upFocusCycle() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null) {
            upFocusCycle(focusOwner);
        }
    }
    public final void downFocusCycle() {
        Component focusOwner = getFocusOwner();
        if (focusOwner instanceof Container) {
            downFocusCycle((Container)focusOwner);
        }
    }
    void dumpRequests() {
        System.err.println(">>> Requests dump, time: " + System.currentTimeMillis());
        synchronized (heavyweightRequests) {
            for (HeavyweightFocusRequest req : heavyweightRequests) {
                System.err.println(">>> Req: " + req);
            }
        }
        System.err.println("");
    }
    private static final class LightweightFocusRequest {
        final Component component;
        final boolean temporary;
        final CausedFocusEvent.Cause cause;
        LightweightFocusRequest(Component component, boolean temporary, CausedFocusEvent.Cause cause) {
            this.component = component;
            this.temporary = temporary;
            this.cause = cause;
        }
        public String toString() {
            return "LightweightFocusRequest[component=" + component +
                ",temporary=" + temporary + ", cause=" + cause + "]";
        }
    }
    private static final class HeavyweightFocusRequest {
        final Component heavyweight;
        final LinkedList<LightweightFocusRequest> lightweightRequests;
        static final HeavyweightFocusRequest CLEAR_GLOBAL_FOCUS_OWNER =
            new HeavyweightFocusRequest();
        private HeavyweightFocusRequest() {
            heavyweight = null;
            lightweightRequests = null;
        }
        HeavyweightFocusRequest(Component heavyweight, Component descendant,
                                boolean temporary, CausedFocusEvent.Cause cause) {
            if (log.isLoggable(PlatformLogger.FINE)) {
                if (heavyweight == null) {
                    log.fine("Assertion (heavyweight != null) failed");
                }
            }
            this.heavyweight = heavyweight;
            this.lightweightRequests = new LinkedList<LightweightFocusRequest>();
            addLightweightRequest(descendant, temporary, cause);
        }
        boolean addLightweightRequest(Component descendant,
                                      boolean temporary, CausedFocusEvent.Cause cause) {
            if (log.isLoggable(PlatformLogger.FINE)) {
                if (this == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) {
                    log.fine("Assertion (this != HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) failed");
                }
                if (descendant == null) {
                    log.fine("Assertion (descendant != null) failed");
                }
            }
            Component lastDescendant = ((lightweightRequests.size() > 0)
                ? lightweightRequests.getLast().component
                : null);
            if (descendant != lastDescendant) {
                lightweightRequests.add
                    (new LightweightFocusRequest(descendant, temporary, cause));
                return true;
            } else {
                return false;
            }
        }
        LightweightFocusRequest getFirstLightweightRequest() {
            if (this == CLEAR_GLOBAL_FOCUS_OWNER) {
                return null;
            }
            return lightweightRequests.getFirst();
        }
        public String toString() {
            boolean first = true;
            String str = "HeavyweightFocusRequest[heavweight=" + heavyweight +
                ",lightweightRequests=";
            if (lightweightRequests == null) {
                str += null;
            } else {
                str += "[";
                for (LightweightFocusRequest lwRequest : lightweightRequests) {
                    if (first) {
                        first = false;
                    } else {
                        str += ",";
                    }
                    str += lwRequest;
                }
                str += "]";
            }
            str += "]";
            return str;
        }
    }
    private static LinkedList<HeavyweightFocusRequest> heavyweightRequests =
        new LinkedList<HeavyweightFocusRequest>();
    private static LinkedList<LightweightFocusRequest> currentLightweightRequests;
    private static boolean clearingCurrentLightweightRequests;
    private static boolean allowSyncFocusRequests = true;
    private static Component newFocusOwner = null;
    private static volatile boolean disableRestoreFocus;
    static final int SNFH_FAILURE = 0;
    static final int SNFH_SUCCESS_HANDLED = 1;
    static final int SNFH_SUCCESS_PROCEED = 2;
    static boolean processSynchronousLightweightTransfer(Component heavyweight, Component descendant,
                                                  boolean temporary, boolean focusedWindowChangeAllowed,
                                                  long time)
    {
        Window parentWindow = SunToolkit.getContainingWindow(heavyweight);
        if (parentWindow == null || !parentWindow.syncLWRequests) {
            return false;
        }
        if (descendant == null) {
            descendant = heavyweight;
        }
        KeyboardFocusManager manager = getCurrentKeyboardFocusManager(SunToolkit.targetToAppContext(descendant));
        FocusEvent currentFocusOwnerEvent = null;
        FocusEvent newFocusOwnerEvent = null;
        Component currentFocusOwner = manager.getGlobalFocusOwner();
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (hwFocusRequest == null &&
                heavyweight == manager.getNativeFocusOwner() &&
                allowSyncFocusRequests)
            {
                if (descendant == currentFocusOwner) {
                    return true;
                }
                manager.enqueueKeyEvents(time, descendant);
                hwFocusRequest =
                    new HeavyweightFocusRequest(heavyweight, descendant,
                                                temporary, CausedFocusEvent.Cause.UNKNOWN);
                heavyweightRequests.add(hwFocusRequest);
                if (currentFocusOwner != null) {
                    currentFocusOwnerEvent =
                        new FocusEvent(currentFocusOwner,
                                       FocusEvent.FOCUS_LOST,
                                       temporary, descendant);
                }
                newFocusOwnerEvent =
                    new FocusEvent(descendant, FocusEvent.FOCUS_GAINED,
                                   temporary, currentFocusOwner);
            }
        }
        boolean result = false;
        final boolean clearing = clearingCurrentLightweightRequests;
        Throwable caughtEx = null;
        try {
            clearingCurrentLightweightRequests = false;
            synchronized(Component.LOCK) {
                if (currentFocusOwnerEvent != null && currentFocusOwner != null) {
                    ((AWTEvent) currentFocusOwnerEvent).isPosted = true;
                    caughtEx = dispatchAndCatchException(caughtEx, currentFocusOwner, currentFocusOwnerEvent);
                    result = true;
                }
                if (newFocusOwnerEvent != null && descendant != null) {
                    ((AWTEvent) newFocusOwnerEvent).isPosted = true;
                    caughtEx = dispatchAndCatchException(caughtEx, descendant, newFocusOwnerEvent);
                    result = true;
                }
            }
        } finally {
            clearingCurrentLightweightRequests = clearing;
        }
        if (caughtEx instanceof RuntimeException) {
            throw (RuntimeException)caughtEx;
        } else if (caughtEx instanceof Error) {
            throw (Error)caughtEx;
        }
        return result;
    }
    static int shouldNativelyFocusHeavyweight
        (Component heavyweight, Component descendant, boolean temporary,
         boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause)
    {
        if (log.isLoggable(PlatformLogger.FINE)) {
            if (heavyweight == null) {
                log.fine("Assertion (heavyweight != null) failed");
            }
            if (time == 0) {
                log.fine("Assertion (time != 0) failed");
            }
        }
        if (descendant == null) {
            descendant = heavyweight;
        }
        KeyboardFocusManager manager =
            getCurrentKeyboardFocusManager(SunToolkit.targetToAppContext(descendant));
        KeyboardFocusManager thisManager = getCurrentKeyboardFocusManager();
        Component currentFocusOwner = thisManager.getGlobalFocusOwner();
        Component nativeFocusOwner = thisManager.getNativeFocusOwner();
        Window nativeFocusedWindow = thisManager.getNativeFocusedWindow();
        if (focusLog.isLoggable(PlatformLogger.FINER)) {
            focusLog.finer("SNFH for {0} in {1}",
                           String.valueOf(descendant), String.valueOf(heavyweight));
        }
        if (focusLog.isLoggable(PlatformLogger.FINEST)) {
            focusLog.finest("0. Current focus owner {0}",
                            String.valueOf(currentFocusOwner));
            focusLog.finest("0. Native focus owner {0}",
                            String.valueOf(nativeFocusOwner));
            focusLog.finest("0. Native focused window {0}",
                            String.valueOf(nativeFocusedWindow));
        }
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (focusLog.isLoggable(PlatformLogger.FINEST)) {
                focusLog.finest("Request {0}", String.valueOf(hwFocusRequest));
            }
            if (hwFocusRequest == null &&
                heavyweight == nativeFocusOwner)
            {
                if (descendant == currentFocusOwner) {
                    if (focusLog.isLoggable(PlatformLogger.FINEST))
                        focusLog.finest("1. SNFH_FAILURE for {0}",
                                        String.valueOf(descendant));
                    return SNFH_FAILURE;
                }
                manager.enqueueKeyEvents(time, descendant);
                hwFocusRequest =
                    new HeavyweightFocusRequest(heavyweight, descendant,
                                                temporary, cause);
                heavyweightRequests.add(hwFocusRequest);
                if (currentFocusOwner != null) {
                    FocusEvent currentFocusOwnerEvent =
                        new CausedFocusEvent(currentFocusOwner,
                                       FocusEvent.FOCUS_LOST,
                                       temporary, descendant, cause);
                    SunToolkit.postEvent(currentFocusOwner.appContext,
                                         currentFocusOwnerEvent);
                }
                FocusEvent newFocusOwnerEvent =
                    new CausedFocusEvent(descendant, FocusEvent.FOCUS_GAINED,
                                   temporary, currentFocusOwner, cause);
                SunToolkit.postEvent(descendant.appContext, newFocusOwnerEvent);
                if (focusLog.isLoggable(PlatformLogger.FINEST))
                    focusLog.finest("2. SNFH_HANDLED for {0}", String.valueOf(descendant));
                return SNFH_SUCCESS_HANDLED;
            } else if (hwFocusRequest != null &&
                       hwFocusRequest.heavyweight == heavyweight) {
                if (hwFocusRequest.addLightweightRequest(descendant,
                                                         temporary, cause)) {
                    manager.enqueueKeyEvents(time, descendant);
                }
                if (focusLog.isLoggable(PlatformLogger.FINEST))
                    focusLog.finest("3. SNFH_HANDLED for lightweight" +
                                    descendant + " in " + heavyweight);
                return SNFH_SUCCESS_HANDLED;
            } else {
                if (!focusedWindowChangeAllowed) {
                    if (hwFocusRequest ==
                        HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
                    {
                        int size = heavyweightRequests.size();
                        hwFocusRequest = (HeavyweightFocusRequest)((size >= 2)
                            ? heavyweightRequests.get(size - 2)
                            : null);
                    }
                    if (focusedWindowChanged(heavyweight,
                                             (hwFocusRequest != null)
                                             ? hwFocusRequest.heavyweight
                                             : nativeFocusedWindow)) {
                        if (focusLog.isLoggable(PlatformLogger.FINEST))
                            focusLog.finest("4. SNFH_FAILURE for " + descendant);
                        return SNFH_FAILURE;
                    }
                }
                manager.enqueueKeyEvents(time, descendant);
                heavyweightRequests.add
                    (new HeavyweightFocusRequest(heavyweight, descendant,
                                                 temporary, cause));
                if (focusLog.isLoggable(PlatformLogger.FINEST))
                    focusLog.finest("5. SNFH_PROCEED for " + descendant);
                return SNFH_SUCCESS_PROCEED;
            }
        }
    }
    static Window markClearGlobalFocusOwner() {
        final Component nativeFocusedWindow =
                getCurrentKeyboardFocusManager().getNativeFocusedWindow();
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (hwFocusRequest ==
                HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
            {
                return null;
            }
            heavyweightRequests.add
                (HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER);
            Component activeWindow = ((hwFocusRequest != null)
                ? SunToolkit.getContainingWindow(hwFocusRequest.heavyweight)
                : nativeFocusedWindow);
            while (activeWindow != null &&
                   !((activeWindow instanceof Frame) ||
                     (activeWindow instanceof Dialog)))
            {
                activeWindow = activeWindow.getParent_NoClientCode();
            }
            return (Window) activeWindow;
        }
    }
    Component getCurrentWaitingRequest(Component parent) {
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();
            if (hwFocusRequest != null) {
                if (hwFocusRequest.heavyweight == parent) {
                    LightweightFocusRequest lwFocusRequest =
                        hwFocusRequest.lightweightRequests.getFirst();
                    if (lwFocusRequest != null) {
                        return lwFocusRequest.component;
                    }
                }
            }
        }
        return null;
    }
    static boolean isAutoFocusTransferEnabled() {
        synchronized (heavyweightRequests) {
            return (heavyweightRequests.size() == 0)
                    && !disableRestoreFocus
                    && (null == currentLightweightRequests);
        }
    }
    static boolean isAutoFocusTransferEnabledFor(Component comp) {
        return isAutoFocusTransferEnabled() && comp.isAutoFocusTransferOnDisposal();
    }
    static private Throwable dispatchAndCatchException(Throwable ex, Component comp, FocusEvent event) {
        Throwable retEx = null;
        try {
            comp.dispatchEvent(event);
        } catch (RuntimeException re) {
            retEx = re;
        } catch (Error er) {
            retEx = er;
        }
        if (retEx != null) {
            if (ex != null) {
                handleException(ex);
            }
            return retEx;
        }
        return ex;
    }
    static private void handleException(Throwable ex) {
        ex.printStackTrace();
    }
    static void processCurrentLightweightRequests() {
        KeyboardFocusManager manager = getCurrentKeyboardFocusManager();
        LinkedList<LightweightFocusRequest> localLightweightRequests = null;
        Component globalFocusOwner = manager.getGlobalFocusOwner();
        if ((globalFocusOwner != null) &&
            (globalFocusOwner.appContext != AppContext.getAppContext()))
        {
            return;
        }
        synchronized(heavyweightRequests) {
            if (currentLightweightRequests != null) {
                clearingCurrentLightweightRequests = true;
                disableRestoreFocus = true;
                localLightweightRequests = currentLightweightRequests;
                allowSyncFocusRequests = (localLightweightRequests.size() < 2);
                currentLightweightRequests = null;
            } else {
                return;
            }
        }
        Throwable caughtEx = null;
        try {
            if (localLightweightRequests != null) {
                Component lastFocusOwner = null;
                Component currentFocusOwner = null;
                for (Iterator iter = localLightweightRequests.iterator(); iter.hasNext(); ) {
                    currentFocusOwner = manager.getGlobalFocusOwner();
                    LightweightFocusRequest lwFocusRequest =
                        (LightweightFocusRequest)iter.next();
                    if (!iter.hasNext()) {
                        disableRestoreFocus = false;
                    }
                    FocusEvent currentFocusOwnerEvent = null;
                    if (currentFocusOwner != null) {
                        currentFocusOwnerEvent = new CausedFocusEvent(currentFocusOwner,
                                       FocusEvent.FOCUS_LOST,
                                       lwFocusRequest.temporary,
                                       lwFocusRequest.component, lwFocusRequest.cause);
                    }
                    FocusEvent newFocusOwnerEvent =
                        new CausedFocusEvent(lwFocusRequest.component,
                                       FocusEvent.FOCUS_GAINED,
                                       lwFocusRequest.temporary,
                                       currentFocusOwner == null ? lastFocusOwner : currentFocusOwner,
                                       lwFocusRequest.cause);
                    if (currentFocusOwner != null) {
                        ((AWTEvent) currentFocusOwnerEvent).isPosted = true;
                        caughtEx = dispatchAndCatchException(caughtEx, currentFocusOwner, currentFocusOwnerEvent);
                    }
                    ((AWTEvent) newFocusOwnerEvent).isPosted = true;
                    caughtEx = dispatchAndCatchException(caughtEx, lwFocusRequest.component, newFocusOwnerEvent);
                    if (manager.getGlobalFocusOwner() == lwFocusRequest.component) {
                        lastFocusOwner = lwFocusRequest.component;
                    }
                }
            }
        } finally {
            clearingCurrentLightweightRequests = false;
            disableRestoreFocus = false;
            localLightweightRequests = null;
            allowSyncFocusRequests = true;
        }
        if (caughtEx instanceof RuntimeException) {
            throw (RuntimeException)caughtEx;
        } else if (caughtEx instanceof Error) {
            throw (Error)caughtEx;
        }
    }
    static FocusEvent retargetUnexpectedFocusEvent(FocusEvent fe) {
        synchronized (heavyweightRequests) {
            if (removeFirstRequest()) {
                return (FocusEvent)retargetFocusEvent(fe);
            }
            Component source = fe.getComponent();
            Component opposite = fe.getOppositeComponent();
            boolean temporary = false;
            if (fe.getID() == FocusEvent.FOCUS_LOST &&
                (opposite == null || isTemporary(opposite, source)))
            {
                temporary = true;
            }
            return new CausedFocusEvent(source, fe.getID(), temporary, opposite,
                                        CausedFocusEvent.Cause.NATIVE_SYSTEM);
        }
    }
    static FocusEvent retargetFocusGained(FocusEvent fe) {
        assert (fe.getID() == FocusEvent.FOCUS_GAINED);
        Component currentFocusOwner = getCurrentKeyboardFocusManager().
            getGlobalFocusOwner();
        Component source = fe.getComponent();
        Component opposite = fe.getOppositeComponent();
        Component nativeSource = getHeavyweight(source);
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();
            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
            {
                return retargetUnexpectedFocusEvent(fe);
            }
            if (source != null && nativeSource == null && hwFocusRequest != null) {
                if (source == hwFocusRequest.getFirstLightweightRequest().component)
                {
                    source = hwFocusRequest.heavyweight;
                    nativeSource = source; 
                }
            }
            if (hwFocusRequest != null &&
                nativeSource == hwFocusRequest.heavyweight)
            {
                heavyweightRequests.removeFirst();
                LightweightFocusRequest lwFocusRequest =
                    hwFocusRequest.lightweightRequests.removeFirst();
                Component newSource = lwFocusRequest.component;
                if (currentFocusOwner != null) {
                    newFocusOwner = newSource;
                }
                boolean temporary = (opposite == null ||
                                     isTemporary(newSource, opposite))
                        ? false
                        : lwFocusRequest.temporary;
                if (hwFocusRequest.lightweightRequests.size() > 0) {
                    currentLightweightRequests =
                        hwFocusRequest.lightweightRequests;
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                processCurrentLightweightRequests();
                            }
                        });
                }
                return new CausedFocusEvent(newSource,
                                      FocusEvent.FOCUS_GAINED, temporary,
                                      opposite, lwFocusRequest.cause);
            }
            if (currentFocusOwner != null
                && currentFocusOwner.getContainingWindow() == source
                && (hwFocusRequest == null || source != hwFocusRequest.heavyweight))
            {
                return new CausedFocusEvent(currentFocusOwner, FocusEvent.FOCUS_GAINED, false,
                                            null, CausedFocusEvent.Cause.ACTIVATION);
            }
            return retargetUnexpectedFocusEvent(fe);
        } 
    }
    static FocusEvent retargetFocusLost(FocusEvent fe) {
        assert (fe.getID() == FocusEvent.FOCUS_LOST);
        Component currentFocusOwner = getCurrentKeyboardFocusManager().
            getGlobalFocusOwner();
        Component opposite = fe.getOppositeComponent();
        Component nativeOpposite = getHeavyweight(opposite);
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();
            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
            {
                if (currentFocusOwner != null) {
                    heavyweightRequests.removeFirst();
                    return new CausedFocusEvent(currentFocusOwner,
                                                FocusEvent.FOCUS_LOST, false, null,
                                                CausedFocusEvent.Cause.CLEAR_GLOBAL_FOCUS_OWNER);
                }
            } else if (opposite == null)
            {
                if (currentFocusOwner != null) {
                    return new CausedFocusEvent(currentFocusOwner,
                                                FocusEvent.FOCUS_LOST,
                                                true, null, CausedFocusEvent.Cause.ACTIVATION);
                } else {
                    return fe;
                }
            } else if (hwFocusRequest != null &&
                       (nativeOpposite == hwFocusRequest.heavyweight ||
                        nativeOpposite == null &&
                        opposite == hwFocusRequest.getFirstLightweightRequest().component))
            {
                if (currentFocusOwner == null) {
                    return fe;
                }
                LightweightFocusRequest lwFocusRequest =
                    hwFocusRequest.lightweightRequests.getFirst();
                boolean temporary = isTemporary(opposite, currentFocusOwner)
                    ? true
                    : lwFocusRequest.temporary;
                return new CausedFocusEvent(currentFocusOwner, FocusEvent.FOCUS_LOST,
                                            temporary, lwFocusRequest.component, lwFocusRequest.cause);
            } else if (focusedWindowChanged(opposite, currentFocusOwner)) {
                if (!fe.isTemporary() && currentFocusOwner != null) {
                    fe = new CausedFocusEvent(currentFocusOwner, FocusEvent.FOCUS_LOST,
                                              true, opposite, CausedFocusEvent.Cause.ACTIVATION);
                }
                return fe;
            }
            return retargetUnexpectedFocusEvent(fe);
        }  
    }
    static AWTEvent retargetFocusEvent(AWTEvent event) {
        if (clearingCurrentLightweightRequests) {
            return event;
        }
        KeyboardFocusManager manager = getCurrentKeyboardFocusManager();
        if (focusLog.isLoggable(PlatformLogger.FINER)) {
            if (event instanceof FocusEvent || event instanceof WindowEvent) {
                focusLog.finer(">>> {0}", String.valueOf(event));
            }
            if (focusLog.isLoggable(PlatformLogger.FINER) && event instanceof KeyEvent) {
                focusLog.finer("    focus owner is {0}",
                               String.valueOf(manager.getGlobalFocusOwner()));
                focusLog.finer(">>> {0}", String.valueOf(event));
            }
        }
        synchronized(heavyweightRequests) {
            if (newFocusOwner != null &&
                event.getID() == FocusEvent.FOCUS_LOST)
            {
                FocusEvent fe = (FocusEvent)event;
                if (manager.getGlobalFocusOwner() == fe.getComponent() &&
                    fe.getOppositeComponent() == newFocusOwner)
                {
                    newFocusOwner = null;
                    return event;
                }
            }
        }
        processCurrentLightweightRequests();
        switch (event.getID()) {
            case FocusEvent.FOCUS_GAINED: {
                event = retargetFocusGained((FocusEvent)event);
                break;
            }
            case FocusEvent.FOCUS_LOST: {
                event = retargetFocusLost((FocusEvent)event);
                break;
            }
            default:
        }
        return event;
    }
    void clearMarkers() {
    }
    static boolean removeFirstRequest() {
        KeyboardFocusManager manager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        synchronized(heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();
            if (hwFocusRequest != null) {
                heavyweightRequests.removeFirst();
                if (hwFocusRequest.lightweightRequests != null) {
                    for (Iterator lwIter = hwFocusRequest.lightweightRequests.
                             iterator();
                         lwIter.hasNext(); )
                    {
                        manager.dequeueKeyEvents
                            (-1, ((LightweightFocusRequest)lwIter.next()).
                             component);
                    }
                }
            }
            if (heavyweightRequests.size() == 0) {
                manager.clearMarkers();
            }
            return (heavyweightRequests.size() > 0);
        }
    }
    static void removeLastFocusRequest(Component heavyweight) {
        if (log.isLoggable(PlatformLogger.FINE)) {
            if (heavyweight == null) {
                log.fine("Assertion (heavyweight != null) failed");
            }
        }
        KeyboardFocusManager manager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        synchronized(heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (hwFocusRequest != null &&
                hwFocusRequest.heavyweight == heavyweight) {
                heavyweightRequests.removeLast();
            }
            if (heavyweightRequests.size() == 0) {
                manager.clearMarkers();
            }
        }
    }
    private static boolean focusedWindowChanged(Component to, Component from) {
        Window wto = SunToolkit.getContainingWindow(to);
        Window wfrom = SunToolkit.getContainingWindow(from);
        if (wto == null && wfrom == null) {
            return true;
        }
        if (wto == null) {
            return true;
        }
        if (wfrom == null) {
            return true;
        }
        return (wto != wfrom);
    }
    private static boolean isTemporary(Component to, Component from) {
        Window wto = SunToolkit.getContainingWindow(to);
        Window wfrom = SunToolkit.getContainingWindow(from);
        if (wto == null && wfrom == null) {
            return false;
        }
        if (wto == null) {
            return true;
        }
        if (wfrom == null) {
            return false;
        }
        return (wto != wfrom);
    }
    static Component getHeavyweight(Component comp) {
        if (comp == null || comp.getPeer() == null) {
            return null;
        } else if (comp.getPeer() instanceof LightweightPeer) {
            return comp.getNativeContainer();
        } else {
            return comp;
        }
    }
    static Field proxyActive;
    private static boolean isProxyActiveImpl(KeyEvent e) {
        if (proxyActive == null) {
            proxyActive = (Field) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        Field field = null;
                        try {
                            field = KeyEvent.class.getDeclaredField("isProxyActive");
                            if (field != null) {
                                field.setAccessible(true);
                            }
                        } catch (NoSuchFieldException nsf) {
                            assert(false);
                        }
                        return field;
                    }
                });
        }
        try {
            return proxyActive.getBoolean(e);
        } catch (IllegalAccessException iae) {
            assert(false);
        }
        return false;
    }
    static boolean isProxyActive(KeyEvent e) {
        if (!GraphicsEnvironment.isHeadless()) {
            return isProxyActiveImpl(e);
        } else {
            return false;
        }
    }
    private static HeavyweightFocusRequest getLastHWRequest() {
        synchronized(heavyweightRequests) {
            return (heavyweightRequests.size() > 0)
                ? heavyweightRequests.getLast()
                : null;
        }
    }
    private static HeavyweightFocusRequest getFirstHWRequest() {
        synchronized(heavyweightRequests) {
            return (heavyweightRequests.size() > 0)
                ? heavyweightRequests.getFirst()
                : null;
        }
    }
}
