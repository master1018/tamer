public class VMStartEventSet extends AbstractEventSet {
    private static final long serialVersionUID = -3384957227835478191L;
    VMStartEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public ThreadReference getThread() {
        return ((VMStartEvent)oneEvent).thread();
    }
   @Override
    public void notify(JDIListener listener) {
        listener.vmStart(this);
    }
}
