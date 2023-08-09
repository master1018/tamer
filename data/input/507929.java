class ThreadData implements TimeLineView.Row {
    private int mId;
    private String mName;
    private long mGlobalStartTime = -1;
    private long mGlobalEndTime = -1;
    private long mLastEventTime;
    private long mCpuTime;
    private Call mRoot;
    private Call mCurrent;
    private Call mLastContextSwitch;
    private ArrayList<Call> mStack = new ArrayList<Call>();
    private HashMap<MethodData, Integer> mStackMethods = new HashMap<MethodData, Integer>();
    private boolean mIsEmpty;
    ThreadData(int id, String name, MethodData topLevel) {
        mId = id;
        mName = String.format("[%d] %s", id, name);
        mRoot = new Call(mName, topLevel);
        mCurrent = mRoot;
        mIsEmpty = true;
    }
    public boolean isEmpty() {
        return mIsEmpty;
    }
    public String getName() {
        return mName;
    }
    public Call getCalltreeRoot() {
        return mRoot;
    }
    void handleCall(Call call, long globalTime) {
        mIsEmpty = false;
        long currentTime = call.mThreadStartTime;
        if (currentTime < mLastEventTime) {
            System.err
            .printf(
                    "ThreadData: '%1$s' call time (%2$d) is less than previous time (%3$d) for thread '%4$s'\n",
                    call.getName(), currentTime, mLastEventTime, mName);
            System.exit(1);
        }
        long elapsed = currentTime - mLastEventTime;
        mCpuTime += elapsed;
        if (call.getMethodAction() == 0) {
            enter(call, elapsed);
        } else {
            exit(call, elapsed, globalTime);
        }
        mLastEventTime = currentTime;
        mGlobalEndTime = globalTime;
    }
    private void enter(Call c, long elapsed) {
        Call caller = mCurrent;
        push(c);
        MethodData md = c.mMethodData;
        Integer num = mStackMethods.get(md);
        if (num == null) {
            num = 0;
        } else if (num > 0) {
            c.setRecursive(true);
        }
        num += 1;
        mStackMethods.put(md, num);
        mCurrent = c;
        caller.addExclusiveTime(elapsed);
    }
    private void exit(Call c, long elapsed, long globalTime) {
        mCurrent.mGlobalEndTime = globalTime;
        Call top = pop();
        if (top == null) {
            return;
        }
        if (mCurrent.mMethodData != c.mMethodData) {
            String error = "Method exit (" + c.getName()
                    + ") does not match current method (" + mCurrent.getName()
                    + ")";
            throw new RuntimeException(error);
        } else {
            long duration = c.mThreadStartTime - mCurrent.mThreadStartTime;
            Call caller = top();
            mCurrent.addExclusiveTime(elapsed);
            mCurrent.addInclusiveTime(duration, caller);
            if (caller == null) {
                caller = mRoot;
            }
            mCurrent = caller;
        }
    }
    public void push(Call c) {
        mStack.add(c);
    }
    public Call pop() {
        ArrayList<Call> stack = mStack;
        if (stack.size() == 0)
            return null;
        Call top = stack.get(stack.size() - 1);
        stack.remove(stack.size() - 1);
        MethodData md = top.mMethodData;
        Integer num = mStackMethods.get(md);
        if (num != null) {
            num -= 1;
            if (num <= 0) {
                mStackMethods.remove(md);
            } else {
                mStackMethods.put(md, num);
            }
        }
        return top;
    }
    public Call top() {
        ArrayList<Call> stack = mStack;
        if (stack.size() == 0)
            return null;
        return stack.get(stack.size() - 1);
    }
    public long endTrace() {
        while (mCurrent != mRoot) {
            long duration = mLastEventTime - mCurrent.mThreadStartTime;
            pop();
            Call caller = top();
            mCurrent.addInclusiveTime(duration, caller);
            mCurrent.mGlobalEndTime = mGlobalEndTime;
            if (caller == null) {
                caller = mRoot;
            }
            mCurrent = caller;
        }
        return mLastEventTime;
    }
    @Override
    public String toString() {
        return mName;
    }
    public int getId() {
        return mId;
    }
    public void setCpuTime(long cpuTime) {
        mCpuTime = cpuTime;
    }
    public long getCpuTime() {
        return mCpuTime;
    }
    public void setGlobalStartTime(long globalStartTime) {
        mGlobalStartTime = globalStartTime;
    }
    public long getGlobalStartTime() {
        return mGlobalStartTime;
    }
    public void setLastEventTime(long lastEventTime) {
        mLastEventTime = lastEventTime;
    }
    public long getLastEventTime() {
        return mLastEventTime;
    }
    public void setGlobalEndTime(long globalEndTime) {
        mGlobalEndTime = globalEndTime;
    }
    public long getGlobalEndTime() {
        return mGlobalEndTime;
    }
    public void setLastContextSwitch(Call lastContextSwitch) {
        mLastContextSwitch = lastContextSwitch;
    }
    public Call getLastContextSwitch() {
        return mLastContextSwitch;
    }
}
