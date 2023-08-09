public abstract class InputMethodAdapter implements InputMethod {
    private Component clientComponent;
    void setClientComponent(Component client) {
        clientComponent = client;
    }
    protected Component getClientComponent() {
        return clientComponent;
    }
    protected boolean haveActiveClient() {
        return clientComponent != null && clientComponent.getInputMethodRequests() != null;
    }
    protected void setAWTFocussedComponent(Component component) {
    }
    protected boolean supportsBelowTheSpot() {
        return false;
    }
    protected void stopListening() {
    }
    public void notifyClientWindowChange(Rectangle location) {
    }
    public void reconvert() {
        throw new UnsupportedOperationException();
    }
    public abstract void disableInputMethod();
    public abstract String getNativeInputMethodInfo();
}
