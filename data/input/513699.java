public class StackTrace {
    int mSerialNumber;
    int mThreadSerialNumber;
    StackFrame[] mFrames;
    StackTrace mParent = null;
    int mOffset = 0;
    private StackTrace() {
    }
    public StackTrace(int serial, int thread, StackFrame[] frames) {
        mSerialNumber = serial;
        mThreadSerialNumber = thread;
        mFrames = frames;
    }
    public final StackTrace fromDepth(int startingDepth) {
        StackTrace result = new StackTrace();
        if (mParent != null) {
            result.mParent = mParent;
        } else {
            result.mParent = this;
        }
        result.mOffset = startingDepth + mOffset;
        return result;
    }
    public final void dump() {
        final int N = mFrames.length;
        for (int i = 0; i < N; i++) {
            System.out.println(mFrames[i].toString());
        }
    }
}
