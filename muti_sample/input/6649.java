public abstract class AWTEvent extends EventObject {
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.AWTEvent");
    private byte bdata[];
    protected int id;
    protected boolean consumed = false;
    private transient volatile AccessControlContext acc =
        AccessController.getContext();
    final AccessControlContext getAccessControlContext() {
        if (acc == null) {
            throw new SecurityException("AWTEvent is missing AccessControlContext");
        }
        return acc;
    }
    transient boolean focusManagerIsDispatching = false;
    transient boolean isPosted;
    private transient boolean isSystemGenerated;
    public final static long COMPONENT_EVENT_MASK = 0x01;
    public final static long CONTAINER_EVENT_MASK = 0x02;
    public final static long FOCUS_EVENT_MASK = 0x04;
    public final static long KEY_EVENT_MASK = 0x08;
    public final static long MOUSE_EVENT_MASK = 0x10;
    public final static long MOUSE_MOTION_EVENT_MASK = 0x20;
    public final static long WINDOW_EVENT_MASK = 0x40;
    public final static long ACTION_EVENT_MASK = 0x80;
    public final static long ADJUSTMENT_EVENT_MASK = 0x100;
    public final static long ITEM_EVENT_MASK = 0x200;
    public final static long TEXT_EVENT_MASK = 0x400;
    public final static long INPUT_METHOD_EVENT_MASK = 0x800;
    final static long INPUT_METHODS_ENABLED_MASK = 0x1000;
    public final static long PAINT_EVENT_MASK = 0x2000;
    public final static long INVOCATION_EVENT_MASK = 0x4000;
    public final static long HIERARCHY_EVENT_MASK = 0x8000;
    public final static long HIERARCHY_BOUNDS_EVENT_MASK = 0x10000;
    public final static long MOUSE_WHEEL_EVENT_MASK = 0x20000;
    public final static long WINDOW_STATE_EVENT_MASK = 0x40000;
    public final static long WINDOW_FOCUS_EVENT_MASK = 0x80000;
    public final static int RESERVED_ID_MAX = 1999;
    private static Field inputEvent_CanAccessSystemClipboard_Field = null;
    private static final long serialVersionUID = -1825314779160409405L;
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setAWTEventAccessor(
            new AWTAccessor.AWTEventAccessor() {
                public void setPosted(AWTEvent ev) {
                    ev.isPosted = true;
                }
                public void setSystemGenerated(AWTEvent ev) {
                    ev.isSystemGenerated = true;
                }
                public boolean isSystemGenerated(AWTEvent ev) {
                    return ev.isSystemGenerated;
                }
                public AccessControlContext getAccessControlContext(AWTEvent ev) {
                    return ev.getAccessControlContext();
                }
            });
    }
    private static synchronized Field get_InputEvent_CanAccessSystemClipboard() {
        if (inputEvent_CanAccessSystemClipboard_Field == null) {
            inputEvent_CanAccessSystemClipboard_Field =
                (Field)java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                            public Object run() {
                                Field field = null;
                                try {
                                    field = InputEvent.class.
                                        getDeclaredField("canAccessSystemClipboard");
                                    field.setAccessible(true);
                                    return field;
                                } catch (SecurityException e) {
                                    if (log.isLoggable(PlatformLogger.FINE)) {
                                        log.fine("AWTEvent.get_InputEvent_CanAccessSystemClipboard() got SecurityException ", e);
                                    }
                                } catch (NoSuchFieldException e) {
                                    if (log.isLoggable(PlatformLogger.FINE)) {
                                        log.fine("AWTEvent.get_InputEvent_CanAccessSystemClipboard() got NoSuchFieldException ", e);
                                    }
                                }
                                return null;
                            }
                        });
        }
        return inputEvent_CanAccessSystemClipboard_Field;
    }
    private static native void initIDs();
    public AWTEvent(Event event) {
        this(event.target, event.id);
    }
    public AWTEvent(Object source, int id) {
        super(source);
        this.id = id;
        switch(id) {
          case ActionEvent.ACTION_PERFORMED:
          case ItemEvent.ITEM_STATE_CHANGED:
          case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
          case TextEvent.TEXT_VALUE_CHANGED:
            consumed = true;
            break;
          default:
        }
    }
    public void setSource(Object newSource) {
        if (source == newSource) {
            return;
        }
        Component comp = null;
        if (newSource instanceof Component) {
            comp = (Component)newSource;
            while (comp != null && comp.peer != null &&
                   (comp.peer instanceof LightweightPeer)) {
                comp = comp.parent;
            }
        }
        synchronized (this) {
            source = newSource;
            if (comp != null) {
                ComponentPeer peer = comp.peer;
                if (peer != null) {
                    nativeSetSource(peer);
                }
            }
        }
    }
    private native void nativeSetSource(ComponentPeer peer);
    public int getID() {
        return id;
    }
    public String toString() {
        String srcName = null;
        if (source instanceof Component) {
            srcName = ((Component)source).getName();
        } else if (source instanceof MenuComponent) {
            srcName = ((MenuComponent)source).getName();
        }
        return getClass().getName() + "[" + paramString() + "] on " +
            (srcName != null? srcName : source);
    }
    public String paramString() {
        return "";
    }
    protected void consume() {
        switch(id) {
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
          case MouseEvent.MOUSE_PRESSED:
          case MouseEvent.MOUSE_RELEASED:
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
          case MouseEvent.MOUSE_ENTERED:
          case MouseEvent.MOUSE_EXITED:
          case MouseEvent.MOUSE_WHEEL:
          case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
          case InputMethodEvent.CARET_POSITION_CHANGED:
              consumed = true;
              break;
          default:
        }
    }
    protected boolean isConsumed() {
        return consumed;
    }
    Event convertToOld() {
        Object src = getSource();
        int newid = id;
        switch(id) {
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
              KeyEvent ke = (KeyEvent)this;
              if (ke.isActionKey()) {
                  newid = (id == KeyEvent.KEY_PRESSED?
                           Event.KEY_ACTION : Event.KEY_ACTION_RELEASE);
              }
              int keyCode = ke.getKeyCode();
              if (keyCode == KeyEvent.VK_SHIFT ||
                  keyCode == KeyEvent.VK_CONTROL ||
                  keyCode == KeyEvent.VK_ALT) {
                  return null;  
              }
              return new Event(src, ke.getWhen(), newid, 0, 0,
                               Event.getOldEventKey(ke),
                               (ke.getModifiers() & ~InputEvent.BUTTON1_MASK));
          case MouseEvent.MOUSE_PRESSED:
          case MouseEvent.MOUSE_RELEASED:
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
          case MouseEvent.MOUSE_ENTERED:
          case MouseEvent.MOUSE_EXITED:
              MouseEvent me = (MouseEvent)this;
              Event olde = new Event(src, me.getWhen(), newid,
                               me.getX(), me.getY(), 0,
                               (me.getModifiers() & ~InputEvent.BUTTON1_MASK));
              olde.clickCount = me.getClickCount();
              return olde;
          case FocusEvent.FOCUS_GAINED:
              return new Event(src, Event.GOT_FOCUS, null);
          case FocusEvent.FOCUS_LOST:
              return new Event(src, Event.LOST_FOCUS, null);
          case WindowEvent.WINDOW_CLOSING:
          case WindowEvent.WINDOW_ICONIFIED:
          case WindowEvent.WINDOW_DEICONIFIED:
              return new Event(src, newid, null);
          case ComponentEvent.COMPONENT_MOVED:
              if (src instanceof Frame || src instanceof Dialog) {
                  Point p = ((Component)src).getLocation();
                  return new Event(src, 0, Event.WINDOW_MOVED, p.x, p.y, 0, 0);
              }
              break;
          case ActionEvent.ACTION_PERFORMED:
              ActionEvent ae = (ActionEvent)this;
              String cmd;
              if (src instanceof Button) {
                  cmd = ((Button)src).getLabel();
              } else if (src instanceof MenuItem) {
                  cmd = ((MenuItem)src).getLabel();
              } else {
                  cmd = ae.getActionCommand();
              }
              return new Event(src, 0, newid, 0, 0, 0, ae.getModifiers(), cmd);
          case ItemEvent.ITEM_STATE_CHANGED:
              ItemEvent ie = (ItemEvent)this;
              Object arg;
              if (src instanceof List) {
                  newid = (ie.getStateChange() == ItemEvent.SELECTED?
                           Event.LIST_SELECT : Event.LIST_DESELECT);
                  arg = ie.getItem();
              } else {
                  newid = Event.ACTION_EVENT;
                  if (src instanceof Choice) {
                      arg = ie.getItem();
                  } else { 
                      arg = Boolean.valueOf(ie.getStateChange() == ItemEvent.SELECTED);
                  }
              }
              return new Event(src, newid, arg);
          case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
              AdjustmentEvent aje = (AdjustmentEvent)this;
              switch(aje.getAdjustmentType()) {
                case AdjustmentEvent.UNIT_INCREMENT:
                  newid = Event.SCROLL_LINE_DOWN;
                  break;
                case AdjustmentEvent.UNIT_DECREMENT:
                  newid = Event.SCROLL_LINE_UP;
                  break;
                case AdjustmentEvent.BLOCK_INCREMENT:
                  newid = Event.SCROLL_PAGE_DOWN;
                  break;
                case AdjustmentEvent.BLOCK_DECREMENT:
                  newid = Event.SCROLL_PAGE_UP;
                  break;
                case AdjustmentEvent.TRACK:
                  if (aje.getValueIsAdjusting()) {
                      newid = Event.SCROLL_ABSOLUTE;
                  }
                  else {
                      newid = Event.SCROLL_END;
                  }
                  break;
                default:
                  return null;
              }
              return new Event(src, newid, Integer.valueOf(aje.getValue()));
          default:
        }
        return null;
    }
    void copyPrivateDataInto(AWTEvent that) {
        that.bdata = this.bdata;
        if (this instanceof InputEvent && that instanceof InputEvent) {
            Field field = get_InputEvent_CanAccessSystemClipboard();
            if (field != null) {
                try {
                    boolean b = field.getBoolean(this);
                    field.setBoolean(that, b);
                } catch(IllegalAccessException e) {
                    if (log.isLoggable(PlatformLogger.FINE)) {
                        log.fine("AWTEvent.copyPrivateDataInto() got IllegalAccessException ", e);
                    }
                }
            }
        }
        that.isSystemGenerated = this.isSystemGenerated;
    }
    void dispatched() {
        if (this instanceof InputEvent) {
            Field field = get_InputEvent_CanAccessSystemClipboard();
            if (field != null) {
                try {
                    field.setBoolean(this, false);
                } catch(IllegalAccessException e) {
                    if (log.isLoggable(PlatformLogger.FINE)) {
                        log.fine("AWTEvent.dispatched() got IllegalAccessException ", e);
                    }
                }
            }
        }
    }
} 
