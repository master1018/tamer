class ConditionLock extends Lock {
    private int state = 0;
    public ConditionLock () {
    }
    public ConditionLock (int initialState) {
        state = initialState;
    }
    public synchronized void lockWhen(int desiredState)
        throws InterruptedException
    {
        while (state != desiredState) {
            wait();
        }
        lock();
    }
    public synchronized void unlockWith(int newState) {
        state = newState;
        unlock();
    }
}
