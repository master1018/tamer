public abstract class LocatableEventSet extends AbstractEventSet {
    private static final long serialVersionUID = 1027131209997915620L;
    LocatableEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public Location getLocation() {
        return ((LocatableEvent)oneEvent).location();
    }
    public ThreadReference getThread() {
        return ((LocatableEvent)oneEvent).thread();
    }
}
