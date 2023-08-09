class PollArrayWrapper extends AbstractPollArrayWrapper {
    static final short POLLCONN = POLLOUT;
    int interruptFD;
    PollArrayWrapper(int newSize) {
        newSize = (newSize + 1) * SIZE_POLLFD;
        pollArray = new AllocatedNativeObject(newSize, false);
        pollArrayAddress = pollArray.address();
        totalChannels = 1;
    }
    void initInterrupt(int fd0, int fd1) {
        interruptFD = fd1;
        putDescriptor(0, fd0);
        putEventOps(0, POLLIN);
        putReventOps(0, 0);
    }
    void release(int i) {
        return;
    }
    void free() {
        pollArray.free();
    }
    void addEntry(SelChImpl sc) {
        putDescriptor(totalChannels, IOUtil.fdVal(sc.getFD()));
        putEventOps(totalChannels, 0);
        putReventOps(totalChannels, 0);
        totalChannels++;
    }
    static void replaceEntry(PollArrayWrapper source, int sindex,
                      PollArrayWrapper target, int tindex) {
        target.putDescriptor(tindex, source.getDescriptor(sindex));
        target.putEventOps(tindex, source.getEventOps(sindex));
        target.putReventOps(tindex, source.getReventOps(sindex));
    }
    void grow(int newSize) {
        PollArrayWrapper temp = new PollArrayWrapper(newSize);
        for (int i=0; i<totalChannels; i++)
            replaceEntry(this, i, temp, i);
        pollArray.free();
        pollArray = temp.pollArray;
        pollArrayAddress = pollArray.address();
    }
    int poll(int numfds, int offset, long timeout) {
        return poll0(pollArrayAddress + (offset * SIZE_POLLFD),
                     numfds, timeout);
    }
    public void interrupt() {
        interrupt(interruptFD);
    }
    private native int poll0(long pollAddress, int numfds, long timeout);
    private static native void interrupt(int fd);
}
