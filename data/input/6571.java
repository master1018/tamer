public class ThreadDeathEventSet extends AbstractEventSet {
    private static final long serialVersionUID = -8801604712308151331L;
    ThreadDeathEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
    public ThreadReference getThread() {
        return ((ThreadDeathEvent)oneEvent).thread();
    }
    @Override
    public void notify(JDIListener listener) {
        listener.threadDeath(this);
    }
}
