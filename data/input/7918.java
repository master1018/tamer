public class ClassPrepareEventSet extends AbstractEventSet {
    private static final long serialVersionUID = 5958493423581010491L;
    ClassPrepareEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public ThreadReference getThread() {
        return ((ClassPrepareEvent)oneEvent).thread();
    }
    public ReferenceType getReferenceType() {
        return ((ClassPrepareEvent)oneEvent).referenceType();
    }
    @Override
    public void notify(JDIListener listener) {
        listener.classPrepare(this);
    }
}
