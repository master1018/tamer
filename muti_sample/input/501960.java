public abstract class AbstractSelectionKey extends SelectionKey {
    boolean isValid = true;
    protected AbstractSelectionKey() {
        super();
    }
    @Override
    public final boolean isValid() {
        return isValid;
    }
    @Override
    public final void cancel() {
        if (isValid) {
            isValid = false;
            ((AbstractSelector) selector()).cancel(this);
        }
    }
}
