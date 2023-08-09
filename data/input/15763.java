public class AWTEventListenerProxy
        extends EventListenerProxy<AWTEventListener>
        implements AWTEventListener {
    private final long eventMask;
    public AWTEventListenerProxy (long eventMask, AWTEventListener listener) {
        super(listener);
        this.eventMask = eventMask;
    }
    public void eventDispatched(AWTEvent event) {
        getListener().eventDispatched(event);
    }
    public long getEventMask() {
        return this.eventMask;
    }
}
