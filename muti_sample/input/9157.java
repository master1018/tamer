public class ClassUnloadEventSet extends AbstractEventSet {
    private static final long serialVersionUID = 8370341450345835866L;
    ClassUnloadEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public String getClassName() {
        return ((ClassUnloadEvent)oneEvent).className();
    }
    public String getClassSignature() {
        return ((ClassUnloadEvent)oneEvent).classSignature();
    }
    @Override
    public void notify(JDIListener listener) {
        listener.classUnload(this);
    }
}
