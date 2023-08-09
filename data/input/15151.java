class PropertyVetoException extends Exception {
    private static final long serialVersionUID = 129596057694162164L;
    public PropertyVetoException(String mess, PropertyChangeEvent evt) {
        super(mess);
        this.evt = evt;
    }
    public PropertyChangeEvent getPropertyChangeEvent() {
        return evt;
    }
    private PropertyChangeEvent evt;
}
