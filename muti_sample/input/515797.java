public abstract class EventListenerProxy implements EventListener {
    private final EventListener listener;
    public EventListenerProxy(EventListener listener) {
        super();
        this.listener = listener;
    }
    public EventListener getListener() {
        return listener;
    }
}
