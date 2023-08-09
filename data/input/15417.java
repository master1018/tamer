public class DefaultKeyboardFocusManager extends KeyboardFocusManager {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.DefaultKeyboardFocusManager");
    private static final WeakReference<Window> NULL_WINDOW_WR =
        new WeakReference<Window>(null);
    private static final WeakReference<Component> NULL_COMPONENT_WR =
        new WeakReference<Component>(null);
    private WeakReference<Window> realOppositeWindowWR = NULL_WINDOW_WR;
    private WeakReference<Component> realOppositeComponentWR = NULL_COMPONENT_WR;
    private int inSendMessage;
    private LinkedList enqueuedKeyEvents = new LinkedList(),
        typeAheadMarkers = new LinkedList();
    private boolean consumeNextKeyTyped;
    private static class TypeAheadMarker {
        long after;
        Component untilFocused;
        TypeAheadMarker(long after, Component untilFocused) {
            this.after = after;
            this.untilFocused = untilFocused;
        }
        public String toString() {
            return ">>> Marker after " + after + " on " + untilFocused;
        }
    }
    private Window getOwningFrameDialog(Window window) {
        while (window != null && !(window instanceof Frame ||
                                   window instanceof Dialog)) {
            window = (Window)window.getParent();
        }
        return window;
    }
    private void restoreFocus(FocusEvent fe, Window newFocusedWindow) {
        Component realOppositeComponent = this.realOppositeComponentWR.get();
        Component vetoedComponent = fe.getComponent();
        if (newFocusedWindow != null && restoreFocus(newFocusedWindow,
                                                     vetoedComponent, false))
        {
        } else if (realOppositeComponent != null &&
                   doRestoreFocus(realOppositeComponent, vetoedComponent, false)) {
        } else if (fe.getOppositeComponent() != null &&
                   doRestoreFocus(fe.getOppositeComponent(), vetoedComponent, false)) {
        } else {
            clearGlobalFocusOwner();
        }
    }
    private void restoreFocus(WindowEvent we) {
        Window realOppositeWindow = this.realOppositeWindowWR.get();
        if (realOppositeWindow != null
            && restoreFocus(realOppositeWindow, null, false))
        {
        } else if (we.getOppositeWindow() != null &&
                   restoreFocus(we.getOppositeWindow(), null, false))
        {
        } else {
            clearGlobalFocusOwner();
        }
    }
    private boolean restoreFocus(Window aWindow, Component vetoedComponent,
                                 boolean clearOnFailure) {
        Component toFocus =
            KeyboardFocusManager.getMostRecentFocusOwner(aWindow);
        if (toFocus != null && toFocus != vetoedComponent && doRestoreFocus(toFocus, vetoedComponent, false)) {
            return true;
        } else if (clearOnFailure) {
            clearGlobalFocusOwner();
            return true;
        } else {
            return false;
        }
    }
    private boolean restoreFocus(Component toFocus, boolean clearOnFailure) {
        return doRestoreFocus(toFocus, null, clearOnFailure);
    }
    private boolean doRestoreFocus(Component toFocus, Component vetoedComponent,
                                   boolean clearOnFailure)
    {
        if (toFocus != vetoedComponent && toFocus.isShowing() && toFocus.canBeFocusOwner() &&
            toFocus.requestFocus(false, CausedFocusEvent.Cause.ROLLBACK))
        {
            return true;
        } else {
            Component nextFocus = toFocus.getNextFocusCandidate();
            if (nextFocus != null && nextFocus != vetoedComponent &&
                nextFocus.requestFocusInWindow(CausedFocusEvent.Cause.ROLLBACK))
            {
                return true;
            } else if (clearOnFailure) {
                clearGlobalFocusOwner();
                return true;
            } else {
                return false;
            }
        }
    }
    private static class DefaultKeyboardFocusManagerSentEvent
        extends SentEvent
    {
        private static final long serialVersionUID = -2924743257508701758L;
        public DefaultKeyboardFocusManagerSentEvent(AWTEvent nested,
                                                    AppContext toNotify) {
            super(nested, toNotify);
        }
        public final void dispatch() {
            KeyboardFocusManager manager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            DefaultKeyboardFocusManager defaultManager =
                (manager instanceof DefaultKeyboardFocusManager)
                ? (DefaultKeyboardFocusManager)manager
                : null;
            if (defaultManager != null) {
                synchronized (defaultManager) {
                    defaultManager.inSendMessage++;
                }
            }
            super.dispatch();
            if (defaultManager != null) {
                synchronized (defaultManager) {
                    defaultManager.inSendMessage--;
                }
            }
        }
    }
    static boolean sendMessage(Component target, AWTEvent e) {
        e.isPosted = true;
        AppContext myAppContext = AppContext.getAppContext();
        final AppContext targetAppContext = target.appContext;
        final SentEvent se =
            new DefaultKeyboardFocusManagerSentEvent(e, myAppContext);
        if (myAppContext == targetAppContext) {
            se.dispatch();
        } else {
            if (targetAppContext.isDisposed()) {
                return false;
            }
            SunToolkit.postEvent(targetAppContext, se);
            if (EventQueue.isDispatchThread()) {
                EventDispatchThread edt = (EventDispatchThread)
                    Thread.currentThread();
                edt.pumpEvents(SentEvent.ID, new Conditional() {
                        public boolean evaluate() {
                            return !se.dispatched && !targetAppContext.isDisposed();
                        }
                    });
            } else {
                synchronized (se) {
                    while (!se.dispatched && !targetAppContext.isDisposed()) {
                        try {
                            se.wait(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            }
        }
        return se.dispatched;
    }
    public boolean dispatchEvent(AWTEvent e) {
        if (focusLog.isLoggable(PlatformLogger.FINE) && (e instanceof WindowEvent || e instanceof FocusEvent)) focusLog.fine("" + e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_GAINED_FOCUS: {
                WindowEvent we = (WindowEvent)e;
                Window oldFocusedWindow = getGlobalFocusedWindow();
                Window newFocusedWindow = we.getWindow();
                if (newFocusedWindow == oldFocusedWindow) {
                    break;
                }
                if (!(newFocusedWindow.isFocusableWindow()
                      && newFocusedWindow.isVisible()
                      && newFocusedWindow.isDisplayable()))
                {
                    restoreFocus(we);
                    break;
                }
                if (oldFocusedWindow != null) {
                    boolean isEventDispatched =
                        sendMessage(oldFocusedWindow,
                                new WindowEvent(oldFocusedWindow,
                                                WindowEvent.WINDOW_LOST_FOCUS,
                                                newFocusedWindow));
                    if (!isEventDispatched) {
                        setGlobalFocusOwner(null);
                        setGlobalFocusedWindow(null);
                    }
                }
                Window newActiveWindow =
                    getOwningFrameDialog(newFocusedWindow);
                Window currentActiveWindow = getGlobalActiveWindow();
                if (newActiveWindow != currentActiveWindow) {
                    sendMessage(newActiveWindow,
                                new WindowEvent(newActiveWindow,
                                                WindowEvent.WINDOW_ACTIVATED,
                                                currentActiveWindow));
                    if (newActiveWindow != getGlobalActiveWindow()) {
                        restoreFocus(we);
                        break;
                    }
                }
                setGlobalFocusedWindow(newFocusedWindow);
                if (newFocusedWindow != getGlobalFocusedWindow()) {
                    restoreFocus(we);
                    break;
                }
                if (inSendMessage == 0) {
                    Component toFocus = KeyboardFocusManager.
                        getMostRecentFocusOwner(newFocusedWindow);
                    if ((toFocus == null) &&
                        newFocusedWindow.isFocusableWindow())
                    {
                        toFocus = newFocusedWindow.getFocusTraversalPolicy().
                            getInitialComponent(newFocusedWindow);
                    }
                    Component tempLost = null;
                    synchronized(KeyboardFocusManager.class) {
                        tempLost = newFocusedWindow.setTemporaryLostComponent(null);
                    }
                    if (focusLog.isLoggable(PlatformLogger.FINER)) {
                        focusLog.finer("tempLost {0}, toFocus {1}",
                                       tempLost, toFocus);
                    }
                    if (tempLost != null) {
                        tempLost.requestFocusInWindow(CausedFocusEvent.Cause.ACTIVATION);
                    }
                    if (toFocus != null && toFocus != tempLost) {
                        toFocus.requestFocusInWindow(CausedFocusEvent.Cause.ACTIVATION);
                    }
                }
                Window realOppositeWindow = this.realOppositeWindowWR.get();
                if (realOppositeWindow != we.getOppositeWindow()) {
                    we = new WindowEvent(newFocusedWindow,
                                         WindowEvent.WINDOW_GAINED_FOCUS,
                                         realOppositeWindow);
                }
                return typeAheadAssertions(newFocusedWindow, we);
            }
            case WindowEvent.WINDOW_ACTIVATED: {
                WindowEvent we = (WindowEvent)e;
                Window oldActiveWindow = getGlobalActiveWindow();
                Window newActiveWindow = we.getWindow();
                if (oldActiveWindow == newActiveWindow) {
                    break;
                }
                if (oldActiveWindow != null) {
                    boolean isEventDispatched =
                        sendMessage(oldActiveWindow,
                                new WindowEvent(oldActiveWindow,
                                                WindowEvent.WINDOW_DEACTIVATED,
                                                newActiveWindow));
                    if (!isEventDispatched) {
                        setGlobalActiveWindow(null);
                    }
                    if (getGlobalActiveWindow() != null) {
                        break;
                    }
                }
                setGlobalActiveWindow(newActiveWindow);
                if (newActiveWindow != getGlobalActiveWindow()) {
                    break;
                }
                return typeAheadAssertions(newActiveWindow, we);
            }
            case FocusEvent.FOCUS_GAINED: {
                FocusEvent fe = (FocusEvent)e;
                CausedFocusEvent.Cause cause = (fe instanceof CausedFocusEvent) ?
                    ((CausedFocusEvent)fe).getCause() : CausedFocusEvent.Cause.UNKNOWN;
                Component oldFocusOwner = getGlobalFocusOwner();
                Component newFocusOwner = fe.getComponent();
                if (oldFocusOwner == newFocusOwner) {
                    if (focusLog.isLoggable(PlatformLogger.FINE)) {
                        focusLog.fine("Skipping {0} because focus owner is the same", e);
                    }
                    dequeueKeyEvents(-1, newFocusOwner);
                    break;
                }
                if (oldFocusOwner != null) {
                    boolean isEventDispatched =
                        sendMessage(oldFocusOwner,
                                    new CausedFocusEvent(oldFocusOwner,
                                                   FocusEvent.FOCUS_LOST,
                                                   fe.isTemporary(),
                                                   newFocusOwner, cause));
                    if (!isEventDispatched) {
                        setGlobalFocusOwner(null);
                        if (!fe.isTemporary()) {
                            setGlobalPermanentFocusOwner(null);
                        }
                    }
                }
                final Window newFocusedWindow = SunToolkit.getContainingWindow(newFocusOwner);
                final Window currentFocusedWindow = getGlobalFocusedWindow();
                if (newFocusedWindow != null &&
                    newFocusedWindow != currentFocusedWindow)
                {
                    sendMessage(newFocusedWindow,
                                new WindowEvent(newFocusedWindow,
                                        WindowEvent.WINDOW_GAINED_FOCUS,
                                                currentFocusedWindow));
                    if (newFocusedWindow != getGlobalFocusedWindow()) {
                        dequeueKeyEvents(-1, newFocusOwner);
                        break;
                    }
                }
                if (!(newFocusOwner.isFocusable() && newFocusOwner.isShowing() &&
                    (newFocusOwner.isEnabled() || cause.equals(CausedFocusEvent.Cause.UNKNOWN))))
                {
                    dequeueKeyEvents(-1, newFocusOwner);
                    if (KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                        if (newFocusedWindow == null) {
                            restoreFocus(fe, currentFocusedWindow);
                        } else {
                            restoreFocus(fe, newFocusedWindow);
                        }
                    }
                    break;
                }
                setGlobalFocusOwner(newFocusOwner);
                if (newFocusOwner != getGlobalFocusOwner()) {
                    dequeueKeyEvents(-1, newFocusOwner);
                    if (KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                        restoreFocus(fe, (Window)newFocusedWindow);
                    }
                    break;
                }
                if (!fe.isTemporary()) {
                    setGlobalPermanentFocusOwner(newFocusOwner);
                    if (newFocusOwner != getGlobalPermanentFocusOwner()) {
                        dequeueKeyEvents(-1, newFocusOwner);
                        if (KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                            restoreFocus(fe, (Window)newFocusedWindow);
                        }
                        break;
                    }
                }
                setNativeFocusOwner(getHeavyweight(newFocusOwner));
                Component realOppositeComponent = this.realOppositeComponentWR.get();
                if (realOppositeComponent != null &&
                    realOppositeComponent != fe.getOppositeComponent()) {
                    fe = new CausedFocusEvent(newFocusOwner,
                                        FocusEvent.FOCUS_GAINED,
                                        fe.isTemporary(),
                                        realOppositeComponent, cause);
                    ((AWTEvent) fe).isPosted = true;
                }
                return typeAheadAssertions(newFocusOwner, fe);
            }
            case FocusEvent.FOCUS_LOST: {
                FocusEvent fe = (FocusEvent)e;
                Component currentFocusOwner = getGlobalFocusOwner();
                if (currentFocusOwner == null) {
                    if (focusLog.isLoggable(PlatformLogger.FINE))
                        focusLog.fine("Skipping {0} because focus owner is null", e);
                    break;
                }
                if (currentFocusOwner == fe.getOppositeComponent()) {
                    if (focusLog.isLoggable(PlatformLogger.FINE))
                        focusLog.fine("Skipping {0} because current focus owner is equal to opposite", e);
                    break;
                }
                setGlobalFocusOwner(null);
                if (getGlobalFocusOwner() != null) {
                    restoreFocus(currentFocusOwner, true);
                    break;
                }
                if (!fe.isTemporary()) {
                    setGlobalPermanentFocusOwner(null);
                    if (getGlobalPermanentFocusOwner() != null) {
                        restoreFocus(currentFocusOwner, true);
                        break;
                    }
                } else {
                    Window owningWindow = currentFocusOwner.getContainingWindow();
                    if (owningWindow != null) {
                        owningWindow.setTemporaryLostComponent(currentFocusOwner);
                    }
                }
                setNativeFocusOwner(null);
                fe.setSource(currentFocusOwner);
                realOppositeComponentWR = (fe.getOppositeComponent() != null)
                    ? new WeakReference<Component>(currentFocusOwner)
                    : NULL_COMPONENT_WR;
                return typeAheadAssertions(currentFocusOwner, fe);
            }
            case WindowEvent.WINDOW_DEACTIVATED: {
                WindowEvent we = (WindowEvent)e;
                Window currentActiveWindow = getGlobalActiveWindow();
                if (currentActiveWindow == null) {
                    break;
                }
                if (currentActiveWindow != e.getSource()) {
                    break;
                }
                setGlobalActiveWindow(null);
                if (getGlobalActiveWindow() != null) {
                    break;
                }
                we.setSource(currentActiveWindow);
                return typeAheadAssertions(currentActiveWindow, we);
            }
            case WindowEvent.WINDOW_LOST_FOCUS: {
                WindowEvent we = (WindowEvent)e;
                Window currentFocusedWindow = getGlobalFocusedWindow();
                Window losingFocusWindow = we.getWindow();
                Window activeWindow = getGlobalActiveWindow();
                Window oppositeWindow = we.getOppositeWindow();
                if (focusLog.isLoggable(PlatformLogger.FINE))
                    focusLog.fine("Active {0}, Current focused {1}, losing focus {2} opposite {3}",
                                  activeWindow, currentFocusedWindow,
                                  losingFocusWindow, oppositeWindow);
                if (currentFocusedWindow == null) {
                    break;
                }
                if (inSendMessage == 0 && losingFocusWindow == activeWindow &&
                    oppositeWindow == currentFocusedWindow)
                {
                    break;
                }
                Component currentFocusOwner = getGlobalFocusOwner();
                if (currentFocusOwner != null) {
                    Component oppositeComp = null;
                    if (oppositeWindow != null) {
                        oppositeComp = oppositeWindow.getTemporaryLostComponent();
                        if (oppositeComp == null) {
                            oppositeComp = oppositeWindow.getMostRecentFocusOwner();
                        }
                    }
                    if (oppositeComp == null) {
                        oppositeComp = oppositeWindow;
                    }
                    sendMessage(currentFocusOwner,
                                new CausedFocusEvent(currentFocusOwner,
                                               FocusEvent.FOCUS_LOST,
                                               true,
                                               oppositeComp, CausedFocusEvent.Cause.ACTIVATION));
                }
                setGlobalFocusedWindow(null);
                if (getGlobalFocusedWindow() != null) {
                    restoreFocus(currentFocusedWindow, null, true);
                    break;
                }
                we.setSource(currentFocusedWindow);
                realOppositeWindowWR = (oppositeWindow != null)
                    ? new WeakReference<Window>(currentFocusedWindow)
                    : NULL_WINDOW_WR;
                typeAheadAssertions(currentFocusedWindow, we);
                if (oppositeWindow == null) {
                    sendMessage(activeWindow,
                                new WindowEvent(activeWindow,
                                                WindowEvent.WINDOW_DEACTIVATED,
                                                null));
                    if (getGlobalActiveWindow() != null) {
                        restoreFocus(currentFocusedWindow, null, true);
                    }
                }
                break;
            }
            case KeyEvent.KEY_TYPED:
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED:
                return typeAheadAssertions(null, e);
            default:
                return false;
        }
        return true;
    }
    public boolean dispatchKeyEvent(KeyEvent e) {
        Component focusOwner = (((AWTEvent)e).isPosted) ? getFocusOwner() : e.getComponent();
        if (focusOwner != null && focusOwner.isShowing() && focusOwner.canBeFocusOwner()) {
            if (!e.isConsumed()) {
                Component comp = e.getComponent();
                if (comp != null && comp.isEnabled()) {
                    redispatchEvent(comp, e);
                }
            }
        }
        boolean stopPostProcessing = false;
        java.util.List processors = getKeyEventPostProcessors();
        if (processors != null) {
            for (java.util.Iterator iter = processors.iterator();
                 !stopPostProcessing && iter.hasNext(); )
            {
                stopPostProcessing = (((KeyEventPostProcessor)(iter.next())).
                            postProcessKeyEvent(e));
            }
        }
        if (!stopPostProcessing) {
            postProcessKeyEvent(e);
        }
        Component source = e.getComponent();
        ComponentPeer peer = source.getPeer();
        if (peer == null || peer instanceof LightweightPeer) {
            Container target = source.getNativeContainer();
            if (target != null) {
                peer = target.getPeer();
            }
        }
        if (peer != null) {
            peer.handleEvent(e);
        }
        return true;
    }
    public boolean postProcessKeyEvent(KeyEvent e) {
        if (!e.isConsumed()) {
            Component target = e.getComponent();
            Container p = (Container)
                (target instanceof Container ? target : target.getParent());
            if (p != null) {
                p.postProcessKeyEvent(e);
            }
        }
        return true;
    }
    private void pumpApprovedKeyEvents() {
        KeyEvent ke;
        do {
            ke = null;
            synchronized (this) {
                if (enqueuedKeyEvents.size() != 0) {
                    ke = (KeyEvent)enqueuedKeyEvents.getFirst();
                    if (typeAheadMarkers.size() != 0) {
                        TypeAheadMarker marker = (TypeAheadMarker)
                            typeAheadMarkers.getFirst();
                        if (ke.getWhen() > marker.after) {
                            ke = null;
                        }
                    }
                    if (ke != null) {
                        focusLog.finer("Pumping approved event {0}", ke);
                        enqueuedKeyEvents.removeFirst();
                    }
                }
            }
            if (ke != null) {
                preDispatchKeyEvent(ke);
            }
        } while (ke != null);
    }
    void dumpMarkers() {
        if (focusLog.isLoggable(PlatformLogger.FINEST)) {
            focusLog.finest(">>> Markers dump, time: {0}", System.currentTimeMillis());
            synchronized (this) {
                if (typeAheadMarkers.size() != 0) {
                    Iterator iter = typeAheadMarkers.iterator();
                    while (iter.hasNext()) {
                        TypeAheadMarker marker = (TypeAheadMarker)iter.next();
                        focusLog.finest("    {0}", marker);
                    }
                }
            }
        }
    }
    private boolean typeAheadAssertions(Component target, AWTEvent e) {
        pumpApprovedKeyEvents();
        switch (e.getID()) {
            case KeyEvent.KEY_TYPED:
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED: {
                KeyEvent ke = (KeyEvent)e;
                synchronized (this) {
                    if (e.isPosted && typeAheadMarkers.size() != 0) {
                        TypeAheadMarker marker = (TypeAheadMarker)
                            typeAheadMarkers.getFirst();
                        if (ke.getWhen() > marker.after) {
                            focusLog.finer("Storing event {0} because of marker {1}", ke, marker);
                            enqueuedKeyEvents.addLast(ke);
                            return true;
                        }
                    }
                }
                return preDispatchKeyEvent(ke);
            }
            case FocusEvent.FOCUS_GAINED:
                focusLog.finest("Markers before FOCUS_GAINED on {0}", target);
                dumpMarkers();
                synchronized (this) {
                    boolean found = false;
                    if (hasMarker(target)) {
                        for (Iterator iter = typeAheadMarkers.iterator();
                             iter.hasNext(); )
                        {
                            if (((TypeAheadMarker)iter.next()).untilFocused ==
                                target)
                            {
                                found = true;
                            } else if (found) {
                                break;
                            }
                            iter.remove();
                        }
                    } else {
                        focusLog.finer("Event without marker {0}", e);
                    }
                }
                focusLog.finest("Markers after FOCUS_GAINED");
                dumpMarkers();
                redispatchEvent(target, e);
                pumpApprovedKeyEvents();
                return true;
            default:
                redispatchEvent(target, e);
                return true;
        }
    }
    private boolean hasMarker(Component comp) {
        for (Iterator iter = typeAheadMarkers.iterator(); iter.hasNext(); ) {
            if (((TypeAheadMarker)iter.next()).untilFocused == comp) {
                return true;
            }
        }
        return false;
    }
    void clearMarkers() {
        synchronized(this) {
            typeAheadMarkers.clear();
        }
    }
    private boolean preDispatchKeyEvent(KeyEvent ke) {
        if (((AWTEvent) ke).isPosted) {
            Component focusOwner = getFocusOwner();
            ke.setSource(((focusOwner != null) ? focusOwner : getFocusedWindow()));
        }
        if (ke.getSource() == null) {
            return true;
        }
        EventQueue.setCurrentEventAndMostRecentTime(ke);
        if (KeyboardFocusManager.isProxyActive(ke)) {
            Component source = (Component)ke.getSource();
            Container target = source.getNativeContainer();
            if (target != null) {
                ComponentPeer peer = target.getPeer();
                if (peer != null) {
                    peer.handleEvent(ke);
                    ke.consume();
                }
            }
            return true;
        }
        java.util.List dispatchers = getKeyEventDispatchers();
        if (dispatchers != null) {
            for (java.util.Iterator iter = dispatchers.iterator();
                 iter.hasNext(); )
             {
                 if (((KeyEventDispatcher)(iter.next())).
                     dispatchKeyEvent(ke))
                 {
                     return true;
                 }
             }
        }
        return dispatchKeyEvent(ke);
    }
    private void consumeNextKeyTyped(KeyEvent e) {
        consumeNextKeyTyped = true;
    }
    private void consumeTraversalKey(KeyEvent e) {
        e.consume();
        consumeNextKeyTyped = (e.getID() == KeyEvent.KEY_PRESSED) &&
                              !e.isActionKey();
    }
    private boolean consumeProcessedKeyEvent(KeyEvent e) {
        if ((e.getID() == KeyEvent.KEY_TYPED) && consumeNextKeyTyped) {
            e.consume();
            consumeNextKeyTyped = false;
            return true;
        }
        return false;
    }
    public void processKeyEvent(Component focusedComponent, KeyEvent e) {
        if (consumeProcessedKeyEvent(e)) {
            return;
        }
        if (e.getID() == KeyEvent.KEY_TYPED) {
            return;
        }
        if (focusedComponent.getFocusTraversalKeysEnabled() &&
            !e.isConsumed())
        {
            AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e),
                oppStroke = AWTKeyStroke.getAWTKeyStroke(stroke.getKeyCode(),
                                                 stroke.getModifiers(),
                                                 !stroke.isOnKeyRelease());
            Set toTest;
            boolean contains, containsOpp;
            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    focusNextComponent(focusedComponent);
                }
                return;
            } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                consumeNextKeyTyped = false;
            }
            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    focusPreviousComponent(focusedComponent);
                }
                return;
            }
            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    upFocusCycle(focusedComponent);
                }
                return;
            }
            if (!((focusedComponent instanceof Container) &&
                  ((Container)focusedComponent).isFocusCycleRoot())) {
                return;
            }
            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    downFocusCycle((Container)focusedComponent);
                }
            }
        }
    }
    protected synchronized void enqueueKeyEvents(long after,
                                                 Component untilFocused) {
        if (untilFocused == null) {
            return;
        }
        focusLog.finer("Enqueue at {0} for {1}",
                       after, untilFocused);
        int insertionIndex = 0,
            i = typeAheadMarkers.size();
        ListIterator iter = typeAheadMarkers.listIterator(i);
        for (; i > 0; i--) {
            TypeAheadMarker marker = (TypeAheadMarker)iter.previous();
            if (marker.after <= after) {
                insertionIndex = i;
                break;
            }
        }
        typeAheadMarkers.add(insertionIndex,
                             new TypeAheadMarker(after, untilFocused));
    }
    protected synchronized void dequeueKeyEvents(long after,
                                                 Component untilFocused) {
        if (untilFocused == null) {
            return;
        }
        focusLog.finer("Dequeue at {0} for {1}",
                       after, untilFocused);
        TypeAheadMarker marker;
        ListIterator iter = typeAheadMarkers.listIterator
            ((after >= 0) ? typeAheadMarkers.size() : 0);
        if (after < 0) {
            while (iter.hasNext()) {
                marker = (TypeAheadMarker)iter.next();
                if (marker.untilFocused == untilFocused)
                {
                    iter.remove();
                    return;
                }
            }
        } else {
            while (iter.hasPrevious()) {
                marker = (TypeAheadMarker)iter.previous();
                if (marker.untilFocused == untilFocused &&
                    marker.after == after)
                {
                    iter.remove();
                    return;
                }
            }
        }
    }
    protected synchronized void discardKeyEvents(Component comp) {
        if (comp == null) {
            return;
        }
        long start = -1;
        for (Iterator iter = typeAheadMarkers.iterator(); iter.hasNext(); ) {
            TypeAheadMarker marker = (TypeAheadMarker)iter.next();
            Component toTest = marker.untilFocused;
            boolean match = (toTest == comp);
            while (!match && toTest != null && !(toTest instanceof Window)) {
                toTest = toTest.getParent();
                match = (toTest == comp);
            }
            if (match) {
                if (start < 0) {
                    start = marker.after;
                }
                iter.remove();
            } else if (start >= 0) {
                purgeStampedEvents(start, marker.after);
                start = -1;
            }
        }
        purgeStampedEvents(start, -1);
    }
    private void purgeStampedEvents(long start, long end) {
        if (start < 0) {
            return;
        }
        for (Iterator iter = enqueuedKeyEvents.iterator(); iter.hasNext(); ) {
            KeyEvent ke = (KeyEvent)iter.next();
            long time = ke.getWhen();
            if (start < time && (end < 0 || time <= end)) {
                iter.remove();
            }
            if (end >= 0 && time > end) {
                break;
            }
        }
    }
    public void focusPreviousComponent(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocusBackward();
        }
    }
    public void focusNextComponent(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocus();
        }
    }
    public void upFocusCycle(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocusUpCycle();
        }
    }
    public void downFocusCycle(Container aContainer) {
        if (aContainer != null && aContainer.isFocusCycleRoot()) {
            aContainer.transferFocusDownCycle();
        }
    }
}
