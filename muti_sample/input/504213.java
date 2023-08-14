final class AWTListenerList<T extends EventListener> extends ListenerList<T> {
    private static final long serialVersionUID = -2622077171532840953L;
    private final Component owner;
    AWTListenerList() {
        super();
        this.owner = null;
    }
    AWTListenerList(Component owner) {
        super();
        this.owner = owner;
    }
    @Override
    public void addUserListener(T listener) {
        super.addUserListener(listener);
        if (owner != null) {
            owner.deprecatedEventHandler = false;
        }
    }
}
