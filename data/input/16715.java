public class ChangedEvent<T> extends Event<ChangedListener<T>> {
    private T object;
    public ChangedEvent() {
    }
    public ChangedEvent(T object) {
        this.object = object;
    }
    protected void fire(ChangedListener<T> l) {
        l.changed(object);
    }
}
