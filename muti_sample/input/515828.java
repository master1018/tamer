public class PropertyVetoException extends Exception {
    private static final long serialVersionUID = 129596057694162164L;
    private final PropertyChangeEvent evt;
    public PropertyVetoException(String message, PropertyChangeEvent event) {
        super(message);
        this.evt = event;
    }
    public PropertyChangeEvent getPropertyChangeEvent() {
        return evt;
    }
}
