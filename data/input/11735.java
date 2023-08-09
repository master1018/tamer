public abstract class JConsolePlugin {
    private volatile JConsoleContext context = null;
    private List<PropertyChangeListener> listeners = null;
    protected JConsolePlugin() {
    }
    public final synchronized void setContext(JConsoleContext context) {
        this.context = context;
        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                context.addPropertyChangeListener(l);
            }
            listeners = null;
        }
    }
    public final JConsoleContext getContext() {
        return context;
    }
    public abstract java.util.Map<String, JPanel> getTabs();
    public abstract SwingWorker<?,?> newSwingWorker();
    public void dispose() {
    }
    public final void addContextPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        if (context == null) {
            synchronized (this) {
                if (context == null) {
                    if (listeners == null) {
                        listeners = new ArrayList<PropertyChangeListener>();
                    }
                    listeners.add(listener);
                    return;
                }
            }
        }
        context.addPropertyChangeListener(listener);
    }
    public final void removeContextPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        if (context == null) {
            synchronized (this) {
                if (context == null) {
                    if (listeners != null) {
                        listeners.remove(listener);
                    }
                    return;
                }
            }
        }
        context.removePropertyChangeListener(listener);
    }
}
