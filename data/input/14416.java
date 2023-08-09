public class AppletThreadGroup extends ThreadGroup {
    public AppletThreadGroup(String name) {
        this(Thread.currentThread().getThreadGroup(), name);
    }
    public AppletThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
        setMaxPriority(Thread.NORM_PRIORITY - 1);
    }
}
