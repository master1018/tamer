public abstract class Interaction implements IStateMachine, EventHandler {
    protected List<IState> states;
    protected InitState initState;
    protected IState currentState;
    protected boolean activated;
    protected List<InteractionHandler> handlers;
    protected List<Event> stillProcessingEvents;
    protected TimeoutTransition currentTimeout;
    protected int lastHIDUsed;
    public Interaction() {
        this(new InitState());
    }
    public Interaction(final InitState initState) {
        super();
        if (initState == null) throw new IllegalArgumentException();
        currentTimeout = null;
        activated = true;
        states = new ArrayList<IState>();
        initState.stateMachine = this;
        this.initState = initState;
        addState(initState);
        reinit();
    }
    protected abstract void initStateMachine();
    @Override
    public void setActivated(final boolean activated) {
        this.activated = activated;
        if (!activated) {
            reinit();
            clearEventsStillInProcess();
        }
    }
    @Override
    public void reinit() {
        if (currentTimeout != null) currentTimeout.stopTimeout();
        currentTimeout = null;
        currentState = initState;
        lastHIDUsed = -1;
    }
    public List<InteractionHandler> getHandlers() {
        return handlers;
    }
    public void addHandler(final InteractionHandler handler) {
        if (handler != null) {
            if (handlers == null) handlers = new ArrayList<InteractionHandler>();
            handlers.add(handler);
        }
    }
    protected void notifyHandlersOnStart() throws MustAbortStateMachineException {
        try {
            if (handlers != null) for (final InteractionHandler handler : handlers) handler.interactionStarts(this);
        } catch (final MustAbortStateMachineException ex) {
            notifyHandlersOnAborting();
            throw ex;
        }
    }
    protected void notifyHandlersOnUpdate() throws MustAbortStateMachineException {
        try {
            if (handlers != null) for (final InteractionHandler handler : handlers) handler.interactionUpdates(this);
        } catch (final MustAbortStateMachineException ex) {
            notifyHandlersOnAborting();
            throw ex;
        }
    }
    protected void notifyHandlersOnStop() throws MustAbortStateMachineException {
        try {
            if (handlers != null) for (InteractionHandler handler : handlers) handler.interactionStops(this);
        } catch (MustAbortStateMachineException ex) {
            notifyHandlersOnAborting();
            throw ex;
        }
    }
    protected void notifyHandlersOnAborting() {
        if (handlers != null) for (final InteractionHandler handler : handlers) handler.interactionAborts(this);
    }
    public static Pickable getPickableAt(final double x, final double y, final Object source) {
        if (source == null) return null;
        if (source instanceof Picker) return ((Picker) source).getPickableAt(x, y);
        if (source instanceof Pickable) {
            Pickable srcPickable = (Pickable) source;
            if (srcPickable.contains(x, y)) return srcPickable;
            return srcPickable.getPicker().getPickableAt(x, y);
        }
        return null;
    }
    @Override
    public void addState(final IState state) {
        if (state != null) {
            states.add(state);
            state.setStateMachine(this);
        }
    }
    public void linkToEventable(final Eventable eventable) {
        if (eventable != null && eventable.hasEventManager()) eventable.getEventManager().addHandlers(this);
    }
    @Override
    public boolean isRunning() {
        return activated && currentState != initState;
    }
    private void executeTransition(final ITransition transition) {
        if (activated) try {
            if (transition != null) {
                transition.action();
                transition.getInputState().onOutgoing();
                currentState = transition.getOutputState();
                transition.getOutputState().onIngoing();
            }
        } catch (MustAbortStateMachineException ex) {
            reinit();
        }
    }
    protected void stopCurrentTimeout() {
        if (currentTimeout != null) {
            currentTimeout.stopTimeout();
            currentTimeout = null;
        }
    }
    private boolean checkTransition(final ITransition transition) {
        final boolean ok;
        if (transition.isGuardRespected()) {
            stopCurrentTimeout();
            executeTransition(transition);
            ok = true;
        } else ok = false;
        return ok;
    }
    @Override
    public void onTextChanged(final JTextComponent textComp) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof TextChangedTransition) {
                TextChangedTransition tct = (TextChangedTransition) t;
                tct.setTextComp(textComp);
                tct.setText(textComp.getText());
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onScroll(final int posX, final int posY, final int direction, final int amount, final int type, final int idHID, final Object src) {
        if (!activated) return;
        boolean again = true;
        ITransition transition;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            transition = currentState.getTransition(i);
            if (transition instanceof ScrollTransition) {
                ScrollTransition st = (ScrollTransition) transition;
                st.setAmount(amount);
                st.setDirection(direction);
                st.setSource(src);
                st.setType(type);
                st.setX(posX);
                st.setY(posY);
                st.setHid(idHID);
                again = !checkTransition(transition);
            }
        }
    }
    @Override
    public void onButtonPressed(final AbstractButton button) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof ButtonPressedTransition) {
                ((ButtonPressedTransition) t).setButton(button);
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onItemSelected(final ItemSelectable itemSelectable) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof ListTransition) {
                ((ListTransition) t).setList(itemSelectable);
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onSpinnerChanged(final JSpinner spinner) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof SpinnerTransition) {
                ((SpinnerTransition) t).setSpinner(spinner);
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onCheckBoxModified(final JCheckBox checkbox) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof CheckBoxTransition) {
                ((CheckBoxTransition) t).setCheckBox(checkbox);
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onMenuItemPressed(final JMenuItem menuItem) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof MenuItemTransition) {
                ((MenuItemTransition) t).setMenuItem(menuItem);
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onKeyPressure(final int key, final int idHID, final Object object) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof KeyPressureTransition) {
                final KeyPressureTransition kpt = (KeyPressureTransition) t;
                kpt.setKey(key);
                kpt.setSource(object);
                kpt.setHid(idHID);
                again = !checkTransition(t);
                if (!again) addEvent(new KeyPressEvent(idHID, key, object));
            }
        }
    }
    @Override
    public void onKeyRelease(final int key, final int idHID, final Object object) {
        boolean again = true;
        if (activated) {
            ITransition t;
            for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
                t = currentState.getTransition(i);
                if (t instanceof KeyReleaseTransition) {
                    final KeyReleaseTransition krt = (KeyReleaseTransition) t;
                    krt.setKey(key);
                    krt.setHid(idHID);
                    krt.setSource(object);
                    if (t.isGuardRespected()) {
                        removeKeyEvent(idHID, key);
                        again = !checkTransition(t);
                    }
                }
            }
        }
        if (again) removeKeyEvent(idHID, key);
    }
    @Override
    public void onMove(final int button, final int x, final int y, final boolean pressed, final int idHID, final Object source) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof MoveTransition) {
                MoveTransition mt = (MoveTransition) t;
                mt.setX(x);
                mt.setY(y);
                mt.setButton(button);
                mt.setSource(source);
                mt.setPressed(pressed);
                mt.setHid(idHID);
                again = !checkTransition(t);
            }
        }
    }
    @Override
    public void onPressure(final int button, final int x, final int y, final int idHID, final Object source) {
        if (!activated) return;
        boolean again = true;
        ITransition t;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            t = currentState.getTransition(i);
            if (t instanceof PressureTransition) {
                PressureTransition pt = (PressureTransition) t;
                pt.setX(x);
                pt.setY(y);
                pt.setButton(button);
                pt.setSource(source);
                pt.setHid(idHID);
                again = !checkTransition(t);
                if (!again) addEvent(new MousePressEvent(idHID, x, y, button, source));
            }
        }
    }
    @Override
    public void onRelease(final int button, final int x, final int y, final int idHID, final Object source) {
        boolean again = true;
        if (activated) {
            ITransition t;
            for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
                t = currentState.getTransition(i);
                if (t instanceof ReleaseTransition) {
                    ReleaseTransition rt = (ReleaseTransition) t;
                    rt.setX(x);
                    rt.setY(y);
                    rt.setButton(button);
                    rt.setSource(source);
                    t.setHid(idHID);
                    if (t.isGuardRespected()) {
                        removePressEvent(idHID);
                        again = !checkTransition(t);
                    }
                }
            }
        }
        if (again) removePressEvent(idHID);
    }
    @Override
    public void onWindowClosed(final MFrame frame) {
        if (!activated) return;
        ITransition transition;
        boolean again = true;
        for (int i = 0, j = currentState.getTransitions().size(); again && i < j; i++) {
            transition = currentState.getTransition(i);
            if (transition instanceof WindowClosedTransition) {
                ((WindowClosedTransition) transition).setFrame(frame);
                if (transition.isGuardRespected()) again = !checkTransition(transition);
            }
        }
    }
    @Override
    public void onTabChanged(final JTabbedPane tabbedPanel) {
        if (!activated) return;
        ITransition transition;
        boolean again = true;
        for (int i = 0, j = currentState.getTransitions().size(); again && i < j; i++) {
            transition = currentState.getTransition(i);
            if (transition instanceof TabSelectedTransition) {
                ((TabSelectedTransition) transition).setTabbedPane(tabbedPanel);
                if (transition.isGuardRespected()) again = !checkTransition(transition);
            }
        }
    }
    @Override
    public void onTimeout(final TimeoutTransition timeoutTransition) {
        if (!activated || timeoutTransition == null) return;
        executeTransition(timeoutTransition);
    }
    protected void addEvent(final Event event) {
        if (stillProcessingEvents == null) stillProcessingEvents = new ArrayList<Event>();
        stillProcessingEvents.add(event);
    }
    protected void removeKeyEvent(final int idHID, final int key) {
        if (stillProcessingEvents == null) return;
        boolean removed = false;
        Event event;
        for (int i = 0, size = stillProcessingEvents.size(); i < size && !removed; i++) {
            event = stillProcessingEvents.get(i);
            if (event instanceof KeyPressEvent && event.idHID == idHID && ((KeyPressEvent) event).keyCode == key) {
                removed = true;
                stillProcessingEvents.remove(i);
            }
        }
    }
    protected void removePressEvent(final int idHID) {
        if (stillProcessingEvents == null) return;
        boolean removed = false;
        Event event;
        for (int i = 0, size = stillProcessingEvents.size(); i < size && !removed; i++) {
            event = stillProcessingEvents.get(i);
            if (event instanceof MousePressEvent && event.idHID == idHID) {
                removed = true;
                stillProcessingEvents.remove(i);
            }
        }
    }
    protected void processEvents() {
        if (stillProcessingEvents != null) {
            Event event;
            List<Event> list = new ArrayList<Event>(stillProcessingEvents);
            while (!list.isEmpty()) {
                event = list.remove(0);
                stillProcessingEvents.remove(0);
                if (event instanceof MousePressEvent) {
                    MousePressEvent press = (MousePressEvent) event;
                    onPressure(press.button, press.x, press.y, press.idHID, press.source);
                } else if (event instanceof KeyPressEvent) {
                    KeyPressEvent key = (KeyPressEvent) event;
                    onKeyPressure(key.keyCode, key.idHID, key.source);
                }
            }
        }
    }
    @Override
    public void onTerminating() throws MustAbortStateMachineException {
        notifyHandlersOnStop();
        reinit();
        processEvents();
    }
    @Override
    public void onAborting() {
        notifyHandlersOnAborting();
        reinit();
        clearEventsStillInProcess();
    }
    @Override
    public void onStarting() throws MustAbortStateMachineException {
        notifyHandlersOnStart();
        checkTimeoutTransition();
    }
    @Override
    public void onUpdating() throws MustAbortStateMachineException {
        notifyHandlersOnUpdate();
        checkTimeoutTransition();
    }
    protected void checkTimeoutTransition() {
        boolean again = true;
        ITransition transition;
        for (int i = 0, j = currentState.getTransitions().size(); i < j && again; i++) {
            transition = currentState.getTransition(i);
            if (transition instanceof TimeoutTransition) {
                currentTimeout = (TimeoutTransition) transition;
                again = false;
                currentTimeout.startTimeout();
            }
        }
    }
    public int getLastHIDUsed() {
        return lastHIDUsed;
    }
    public void setLastHIDUsed(final int hid) {
        lastHIDUsed = hid;
    }
    public void clearEventsStillInProcess() {
        if (stillProcessingEvents != null) stillProcessingEvents.clear();
    }
}
class KeyPressEvent extends Event {
    protected int keyCode;
    protected Object source;
    public KeyPressEvent(final int idHID, final int keyCode, final Object source) {
        super(idHID);
        this.keyCode = keyCode;
        this.source = source;
    }
}
class MousePressEvent extends Event {
    protected int x;
    protected int y;
    protected Object source;
    protected int button;
    public MousePressEvent(final int idHID, final int x, final int y, final int button, final Object source) {
        super(idHID);
        this.x = x;
        this.y = y;
        this.button = button;
        this.source = source;
    }
}
abstract class Event {
    protected int idHID;
    public Event(final int idHID) {
        super();
        this.idHID = idHID;
    }
}
