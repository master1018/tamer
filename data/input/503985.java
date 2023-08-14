public abstract class AWTEvent extends EventObject {
    private static final long serialVersionUID = -1825314779160409405L;
    public static final long COMPONENT_EVENT_MASK = 1;
    public static final long CONTAINER_EVENT_MASK = 2;
    public static final long FOCUS_EVENT_MASK = 4;
    public static final long KEY_EVENT_MASK = 8;
    public static final long MOUSE_EVENT_MASK = 16;
    public static final long MOUSE_MOTION_EVENT_MASK = 32;
    public static final long WINDOW_EVENT_MASK = 64;
    public static final long ACTION_EVENT_MASK = 128;
    public static final long ADJUSTMENT_EVENT_MASK = 256;
    public static final long ITEM_EVENT_MASK = 512;
    public static final long TEXT_EVENT_MASK = 1024;
    public static final long INPUT_METHOD_EVENT_MASK = 2048;
    public static final long PAINT_EVENT_MASK = 8192;
    public static final long INVOCATION_EVENT_MASK = 16384;
    public static final long HIERARCHY_EVENT_MASK = 32768;
    public static final long HIERARCHY_BOUNDS_EVENT_MASK = 65536;
    public static final long MOUSE_WHEEL_EVENT_MASK = 131072;
    public static final long WINDOW_STATE_EVENT_MASK = 262144;
    public static final long WINDOW_FOCUS_EVENT_MASK = 524288;
    public static final int RESERVED_ID_MAX = 1999;
    private static final Hashtable<Integer, EventDescriptor> eventsMap = new Hashtable<Integer, EventDescriptor>();
    private static EventConverter converter;
    protected int id;
    protected boolean consumed;
    boolean dispatchedByKFM;
    transient boolean isPosted;
    static {
        eventsMap.put(new Integer(KeyEvent.KEY_TYPED), new EventDescriptor(KEY_EVENT_MASK,
                KeyListener.class));
        eventsMap.put(new Integer(KeyEvent.KEY_PRESSED), new EventDescriptor(KEY_EVENT_MASK,
                KeyListener.class));
        eventsMap.put(new Integer(KeyEvent.KEY_RELEASED), new EventDescriptor(KEY_EVENT_MASK,
                KeyListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_CLICKED), new EventDescriptor(MOUSE_EVENT_MASK,
                MouseListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_PRESSED), new EventDescriptor(MOUSE_EVENT_MASK,
                MouseListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_RELEASED), new EventDescriptor(MOUSE_EVENT_MASK,
                MouseListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_MOVED), new EventDescriptor(
                MOUSE_MOTION_EVENT_MASK, MouseMotionListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_ENTERED), new EventDescriptor(MOUSE_EVENT_MASK,
                MouseListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_EXITED), new EventDescriptor(MOUSE_EVENT_MASK,
                MouseListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_DRAGGED), new EventDescriptor(
                MOUSE_MOTION_EVENT_MASK, MouseMotionListener.class));
        eventsMap.put(new Integer(MouseEvent.MOUSE_WHEEL), new EventDescriptor(
                MOUSE_WHEEL_EVENT_MASK, MouseWheelListener.class));
        eventsMap.put(new Integer(ComponentEvent.COMPONENT_MOVED), new EventDescriptor(
                COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(new Integer(ComponentEvent.COMPONENT_RESIZED), new EventDescriptor(
                COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(new Integer(ComponentEvent.COMPONENT_SHOWN), new EventDescriptor(
                COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(new Integer(ComponentEvent.COMPONENT_HIDDEN), new EventDescriptor(
                COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(new Integer(FocusEvent.FOCUS_GAINED), new EventDescriptor(FOCUS_EVENT_MASK,
                FocusListener.class));
        eventsMap.put(new Integer(FocusEvent.FOCUS_LOST), new EventDescriptor(FOCUS_EVENT_MASK,
                FocusListener.class));
        eventsMap.put(new Integer(PaintEvent.PAINT), new EventDescriptor(PAINT_EVENT_MASK, null));
        eventsMap.put(new Integer(PaintEvent.UPDATE), new EventDescriptor(PAINT_EVENT_MASK, null));
        eventsMap.put(new Integer(WindowEvent.WINDOW_OPENED), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_CLOSING), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_CLOSED), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_DEICONIFIED), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_ICONIFIED), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_STATE_CHANGED), new EventDescriptor(
                WINDOW_STATE_EVENT_MASK, WindowStateListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_LOST_FOCUS), new EventDescriptor(
                WINDOW_FOCUS_EVENT_MASK, WindowFocusListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_GAINED_FOCUS), new EventDescriptor(
                WINDOW_FOCUS_EVENT_MASK, WindowFocusListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_DEACTIVATED), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(WindowEvent.WINDOW_ACTIVATED), new EventDescriptor(
                WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(new Integer(HierarchyEvent.HIERARCHY_CHANGED), new EventDescriptor(
                HIERARCHY_EVENT_MASK, HierarchyListener.class));
        eventsMap.put(new Integer(HierarchyEvent.ANCESTOR_MOVED), new EventDescriptor(
                HIERARCHY_BOUNDS_EVENT_MASK, HierarchyBoundsListener.class));
        eventsMap.put(new Integer(HierarchyEvent.ANCESTOR_RESIZED), new EventDescriptor(
                HIERARCHY_BOUNDS_EVENT_MASK, HierarchyBoundsListener.class));
        eventsMap.put(new Integer(ContainerEvent.COMPONENT_ADDED), new EventDescriptor(
                CONTAINER_EVENT_MASK, ContainerListener.class));
        eventsMap.put(new Integer(ContainerEvent.COMPONENT_REMOVED), new EventDescriptor(
                CONTAINER_EVENT_MASK, ContainerListener.class));
        eventsMap.put(new Integer(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED), new EventDescriptor(
                INPUT_METHOD_EVENT_MASK, InputMethodListener.class));
        eventsMap.put(new Integer(InputMethodEvent.CARET_POSITION_CHANGED), new EventDescriptor(
                INPUT_METHOD_EVENT_MASK, InputMethodListener.class));
        eventsMap.put(new Integer(InvocationEvent.INVOCATION_DEFAULT), new EventDescriptor(
                INVOCATION_EVENT_MASK, null));
        eventsMap.put(new Integer(ItemEvent.ITEM_STATE_CHANGED), new EventDescriptor(
                ITEM_EVENT_MASK, ItemListener.class));
        eventsMap.put(new Integer(TextEvent.TEXT_VALUE_CHANGED), new EventDescriptor(
                TEXT_EVENT_MASK, TextListener.class));
        eventsMap.put(new Integer(ActionEvent.ACTION_PERFORMED), new EventDescriptor(
                ACTION_EVENT_MASK, ActionListener.class));
        eventsMap.put(new Integer(AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED), new EventDescriptor(
                ADJUSTMENT_EVENT_MASK, AdjustmentListener.class));
        converter = new EventConverter();
    }
    public AWTEvent(Event event) {
        this(event.target, event.id);
    }
    public AWTEvent(Object source, int id) {
        super(source);
        this.id = id;
        consumed = false;
    }
    public int getID() {
        return id;
    }
    public void setSource(Object newSource) {
        source = newSource;
    }
    @Override
    public String toString() {
        String name = ""; 
        if (source instanceof Component && (source != null)) {
            Component comp = (Component)getSource();
            name = comp.getName();
            if (name == null) {
                name = ""; 
            }
        }
        return (getClass().getName() + "[" + paramString() + "]" 
                + " on " + (name.length() > 0 ? name : source)); 
    }
    public String paramString() {
        return ""; 
    }
    protected boolean isConsumed() {
        return consumed;
    }
    protected void consume() {
        consumed = true;
    }
    Event getEvent() {
        if (id == ActionEvent.ACTION_PERFORMED) {
            ActionEvent ae = (ActionEvent)this;
            return converter.convertActionEvent(ae);
        } else if (id == AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED) {
            AdjustmentEvent ae = (AdjustmentEvent)this;
            return converter.convertAdjustmentEvent(ae);
        } else if (id >= FocusEvent.FOCUS_FIRST && id <= FocusEvent.FOCUS_LAST) {
        } else if (id == KeyEvent.KEY_PRESSED || id == KeyEvent.KEY_RELEASED) {
            KeyEvent ke = (KeyEvent)this;
            return converter.convertKeyEvent(ke);
        } else if (id >= MouseEvent.MOUSE_FIRST && id <= MouseEvent.MOUSE_LAST) {
            MouseEvent me = (MouseEvent)this;
            return converter.convertMouseEvent(me);
        } else if (id == WindowEvent.WINDOW_CLOSING || id == WindowEvent.WINDOW_ICONIFIED
                || id == WindowEvent.WINDOW_DEICONIFIED) {
        } else {
            return null;
        }
        return new Event(source, id, null);
    }
    static final class EventDescriptor {
        final long eventMask;
        final Class<? extends EventListener> listenerType;
        EventDescriptor(long eventMask, Class<? extends EventListener> listenerType) {
            this.eventMask = eventMask;
            this.listenerType = listenerType;
        }
    }
    static final class EventTypeLookup {
        private AWTEvent lastEvent = null;
        private EventDescriptor lastEventDescriptor = null;
        EventDescriptor getEventDescriptor(AWTEvent event) {
            synchronized (this) {
                if (event != lastEvent) {
                    lastEvent = event;
                    lastEventDescriptor = eventsMap.get(new Integer(event.id));
                }
                return lastEventDescriptor;
            }
        }
        long getEventMask(AWTEvent event) {
            final EventDescriptor ed = getEventDescriptor(event);
            return ed == null ? -1 : ed.eventMask;
        }
    }
    static final class EventConverter {
        static final int OLD_MOD_MASK = Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK
                | Event.SHIFT_MASK;
        Event convertActionEvent(ActionEvent ae) {
            Event evt = new Event(ae.getSource(), ae.getID(), ae.getActionCommand());
            evt.when = ae.getWhen();
            evt.modifiers = ae.getModifiers() & OLD_MOD_MASK;
            return evt;
        }
        Event convertAdjustmentEvent(AdjustmentEvent ae) {
            return new Event(ae.source, ae.id + ae.getAdjustmentType() - 1, new Integer(ae
                    .getValue()));
        }
        Event convertComponentEvent(ComponentEvent ce) {
            Component comp = ce.getComponent();
            Event evt = new Event(comp, Event.WINDOW_MOVED, null);
            evt.x = comp.getX();
            evt.y = comp.getY();
            return evt;
        }
        Event convertKeyEvent(KeyEvent ke) {
            int oldId = ke.id;
            int mod = ke.getModifiers() & OLD_MOD_MASK;
            Component comp = ke.getComponent();
            char keyChar = ke.getKeyChar();
            int keyCode = ke.getKeyCode();
            int key = convertKey(keyChar, keyCode);
            if (key >= Event.HOME && key <= Event.INSERT) {
                oldId += 2; 
            }
            return new Event(comp, ke.getWhen(), oldId, 0, 0, key, mod);
        }
        Event convertMouseEvent(MouseEvent me) {
            int id = me.id;
            if (id != MouseEvent.MOUSE_CLICKED) {
                Event evt = new Event(me.source, id, null);
                evt.x = me.getX();
                evt.y = me.getY();
                int mod = me.getModifiers();
                evt.modifiers = mod & (Event.ALT_MASK | Event.META_MASK);
                if (id == MouseEvent.MOUSE_PRESSED) {
                    evt.clickCount = me.getClickCount();
                }
                return evt;
            }
            return null;
        }
        int convertKey(char keyChar, int keyCode) {
            int key;
            if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
                key = Event.F1 + keyCode - KeyEvent.VK_F1;
            } else {
                switch (keyCode) {
                    default: 
                        key = keyChar;
                        break;
                    case KeyEvent.VK_HOME:
                        key = Event.HOME;
                        break;
                    case KeyEvent.VK_END:
                        key = Event.END;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        key = Event.PGUP;
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        key = Event.PGDN;
                        break;
                    case KeyEvent.VK_UP:
                        key = Event.UP;
                        break;
                    case KeyEvent.VK_DOWN:
                        key = Event.DOWN;
                        break;
                    case KeyEvent.VK_LEFT:
                        key = Event.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                        key = Event.RIGHT;
                        break;
                    case KeyEvent.VK_PRINTSCREEN:
                        key = Event.PRINT_SCREEN;
                        break;
                    case KeyEvent.VK_SCROLL_LOCK:
                        key = Event.SCROLL_LOCK;
                        break;
                    case KeyEvent.VK_CAPS_LOCK:
                        key = Event.CAPS_LOCK;
                        break;
                    case KeyEvent.VK_NUM_LOCK:
                        key = Event.NUM_LOCK;
                        break;
                    case KeyEvent.VK_PAUSE:
                        key = Event.PAUSE;
                        break;
                    case KeyEvent.VK_INSERT:
                        key = Event.INSERT;
                        break;
                }
            }
            return key;
        }
    }
}
