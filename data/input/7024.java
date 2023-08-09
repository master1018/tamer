public class ModificationWatchpointEventSet extends WatchpointEventSet {
    private static final long serialVersionUID = -680889300856154719L;
    ModificationWatchpointEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public Value getValueToBe() {
        return ((ModificationWatchpointEvent)oneEvent).valueToBe();
    }
    @Override
    public void notify(JDIListener listener) {
        listener.modificationWatchpoint(this);
    }
}
