class SentEvent extends AWTEvent implements ActiveEvent {
    private static final long serialVersionUID = -383615247028828931L;
    static final int ID =
        java.awt.event.FocusEvent.FOCUS_LAST + 2;
    boolean dispatched;
    private AWTEvent nested;
    private AppContext toNotify;
    SentEvent() {
        this(null);
    }
    SentEvent(AWTEvent nested) {
        this(nested, null);
    }
    SentEvent(AWTEvent nested, AppContext toNotify) {
        super((nested != null)
                  ? nested.getSource()
                  : Toolkit.getDefaultToolkit(),
              ID);
        this.nested = nested;
        this.toNotify = toNotify;
    }
    public void dispatch() {
        try {
            if (nested != null) {
                Toolkit.getEventQueue().dispatchEvent(nested);
            }
        } finally {
            dispatched = true;
            if (toNotify != null) {
                SunToolkit.postEvent(toNotify, new SentEvent());
            }
            synchronized (this) {
                notifyAll();
            }
        }
    }
    final void dispose() {
        dispatched = true;
        if (toNotify != null) {
            SunToolkit.postEvent(toNotify, new SentEvent());
        }
        synchronized (this) {
            notifyAll();
        }
    }
}
