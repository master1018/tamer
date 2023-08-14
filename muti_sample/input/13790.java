public class MonitorInfoImpl extends MirrorImpl {
    private boolean isValid = true;
    ObjectReference monitor;
    ThreadReference thread;
    int  stack_depth;
    MonitorInfoImpl(VirtualMachine vm, ObjectReference mon,
                    ThreadReference thread, int dpth) {
        super(vm);
        this.monitor = mon;
        this.thread = thread;
        this.stack_depth = dpth;
    }
    private void validateMonitorInfo() {
        if (!isValid) {
            throw new InvalidStackFrameException("Thread has been resumed");
        }
    }
    public ObjectReference monitor() {
        validateMonitorInfo();
        return monitor;
    }
    public int stackDepth() {
        validateMonitorInfo();
        return stack_depth;
    }
    public ThreadReference thread() {
        validateMonitorInfo();
        return thread;
    }
}
