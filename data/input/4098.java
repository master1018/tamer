public class ThreadInfo {
    private ThreadReference thread;
    private int status;
    private int frameCount;
    Object userObject;  
    private boolean interrupted = false;
    private void assureInterrupted() throws VMNotInterruptedException {
        if (!interrupted) {
            throw new VMNotInterruptedException();
        }
    }
    ThreadInfo (ThreadReference thread) {
        this.thread = thread;
        this.frameCount = -1;
    }
    public ThreadReference thread() {
        return thread;
    }
    public int getStatus() throws VMNotInterruptedException {
        assureInterrupted();
        update();
        return status;
    }
    public int getFrameCount() throws VMNotInterruptedException {
        assureInterrupted();
        update();
        return frameCount;
    }
    public StackFrame getFrame(int index) throws VMNotInterruptedException {
        assureInterrupted();
        update();
        try {
            return thread.frame(index);
        } catch (IncompatibleThreadStateException e) {
            interrupted = false;
            throw new VMNotInterruptedException();
        }
    }
    public Object getUserObject() {
        return userObject;
    }
    public void setUserObject(Object obj) {
        userObject = obj;
    }
    void update() throws VMNotInterruptedException {
        if (frameCount == -1) {
            try {
                status = thread.status();
                frameCount = thread.frameCount();
            } catch (IncompatibleThreadStateException e) {
                interrupted = false;
                throw new VMNotInterruptedException();
            }
        }
    }
    void validate() {
        interrupted = true;
    }
    void invalidate() {
        interrupted = false;
        frameCount = -1;
        status = ThreadReference.THREAD_STATUS_UNKNOWN;
    }
}
