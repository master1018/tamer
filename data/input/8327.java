public class LocationTriggerEventSet extends LocatableEventSet {
    private static final long serialVersionUID = -3674631710485872487L;
    LocationTriggerEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    @Override
    public void notify(JDIListener listener) {
        listener.locationTrigger(this);
    }
}
