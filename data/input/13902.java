public final class SwingPropertyChangeSupport extends PropertyChangeSupport {
    public SwingPropertyChangeSupport(Object sourceBean) {
        this(sourceBean, false);
    }
    public SwingPropertyChangeSupport(Object sourceBean, boolean notifyOnEDT) {
        super(sourceBean);
        this.notifyOnEDT = notifyOnEDT;
    }
    public void firePropertyChange(final PropertyChangeEvent evt) {
        if (evt == null) {
            throw new NullPointerException();
        }
        if (! isNotifyOnEDT()
            || SwingUtilities.isEventDispatchThread()) {
            super.firePropertyChange(evt);
        } else {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        firePropertyChange(evt);
                    }
                });
        }
    }
    public final boolean isNotifyOnEDT() {
        return notifyOnEDT;
    }
    static final long serialVersionUID = 7162625831330845068L;
    private final boolean notifyOnEDT;
}
