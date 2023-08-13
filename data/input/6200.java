public class ExceptionEventSet extends LocatableEventSet {
    private static final long serialVersionUID = 5328140167954640711L;
    ExceptionEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public ObjectReference getException() {
        return ((ExceptionEvent)oneEvent).exception();
    }
    public Location getCatchLocation() {
        return ((ExceptionEvent)oneEvent).catchLocation();
    }
    @Override
    public void notify(JDIListener listener) {
        listener.exception(this);
    }
}
