public class CurrentFrameChangedEvent extends EventObject {
    private static final long serialVersionUID = 4214479486546762179L;
    private ThreadInfo tinfo;
    private int index;
    private boolean invalidate;
    public CurrentFrameChangedEvent(Object source, ThreadInfo tinfo,
                                    int index, boolean invalidate) {
        super(source);
        this.tinfo = tinfo;
        this.index = index;
        this.invalidate = invalidate;
    }
    public ThreadReference getThread() {
        return tinfo == null? null : tinfo.thread();
    }
    public ThreadInfo getThreadInfo() {
        return tinfo;
    }
    public int getIndex() {
        return index;
    }
    public boolean getInvalidate() {
        return invalidate;
    }
}
