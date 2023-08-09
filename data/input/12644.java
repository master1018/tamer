public abstract class WatchpointEventSet extends LocatableEventSet {
    private static final long serialVersionUID = 5606285209703845409L;
    WatchpointEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public Field getField() {
        return ((WatchpointEvent)oneEvent).field();
    }
    public ObjectReference getObject() {
        return ((WatchpointEvent)oneEvent).object();
    }
    public Value getValueCurrent() {
        return ((WatchpointEvent)oneEvent).valueCurrent();
    }
}
