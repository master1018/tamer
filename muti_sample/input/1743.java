public class ThreadStartEventSet extends AbstractEventSet {
    private static final long serialVersionUID = -3802096132294933502L;
    ThreadStartEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public ThreadReference getThread() {
        return ((ThreadStartEvent)oneEvent).thread();
    }
    @Override
    public void notify(JDIListener listener) {
        listener.threadStart(this);
    }
}
