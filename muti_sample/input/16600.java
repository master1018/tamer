class DevPollArrayWrapper {
    static final short POLLIN       = 0x0001;
    static final short POLLPRI      = 0x0002;
    static final short POLLOUT      = 0x0004;
    static final short POLLRDNORM   = 0x0040;
    static final short POLLWRNORM   = POLLOUT;
    static final short POLLRDBAND   = 0x0080;
    static final short POLLWRBAND   = 0x0100;
    static final short POLLNORM     = POLLRDNORM;
    static final short POLLERR      = 0x0008;
    static final short POLLHUP      = 0x0010;
    static final short POLLNVAL     = 0x0020;
    static final short POLLREMOVE   = 0x0800;
    static final short POLLCONN     = POLLOUT;
    static final short SIZE_POLLFD   = 8;
    static final short FD_OFFSET     = 0;
    static final short EVENT_OFFSET  = 4;
    static final short REVENT_OFFSET = 6;
    static final int   OPEN_MAX      = fdLimit();
    static final int   NUM_POLLFDS   = Math.min(OPEN_MAX-1, 8192);
    private long pollArrayAddress;
    private AllocatedNativeObject updatePollArray;
    private int MAX_UPDATE_SIZE = Math.min(OPEN_MAX, 10000);
    DevPollArrayWrapper() {
        int allocationSize = NUM_POLLFDS * SIZE_POLLFD;
        pollArray = new AllocatedNativeObject(allocationSize, true);
        pollArrayAddress = pollArray.address();
        allocationSize = MAX_UPDATE_SIZE * SIZE_POLLFD;
        updatePollArray = new AllocatedNativeObject(allocationSize, true);
        wfd = init();
    }
    private static class Updator {
        int fd;
        int mask;
        Updator(int fd, int mask) {
            this.fd = fd;
            this.mask = mask;
        }
    }
    private LinkedList<Updator> updateList = new LinkedList<Updator>();
    private AllocatedNativeObject pollArray;
    int wfd;
    int outgoingInterruptFD;
    int incomingInterruptFD;
    int interruptedIndex;
    int updated;
    void initInterrupt(int fd0, int fd1) {
        outgoingInterruptFD = fd1;
        incomingInterruptFD = fd0;
        register(wfd, fd0, POLLIN);
    }
    void putReventOps(int i, int revent) {
        int offset = SIZE_POLLFD * i + REVENT_OFFSET;
        pollArray.putShort(offset, (short)revent);
    }
    int getEventOps(int i) {
        int offset = SIZE_POLLFD * i + EVENT_OFFSET;
        return pollArray.getShort(offset);
    }
    int getReventOps(int i) {
        int offset = SIZE_POLLFD * i + REVENT_OFFSET;
        return pollArray.getShort(offset);
    }
    int getDescriptor(int i) {
        int offset = SIZE_POLLFD * i + FD_OFFSET;
        return pollArray.getInt(offset);
    }
    void setInterest(int fd, int mask) {
        synchronized (updateList) {
            updateList.add(new Updator(fd, mask));
        }
    }
    void release(int fd) {
        synchronized (updateList) {
            updateList.add(new Updator(fd, POLLREMOVE));
        }
    }
    void closeDevPollFD() throws IOException {
        FileDispatcherImpl.closeIntFD(wfd);
        pollArray.free();
        updatePollArray.free();
    }
    int poll(long timeout) throws IOException {
        updateRegistrations();
        updated = poll0(pollArrayAddress, NUM_POLLFDS, timeout, wfd);
        for (int i=0; i<updated; i++) {
            if (getDescriptor(i) == incomingInterruptFD) {
                interruptedIndex = i;
                interrupted = true;
                break;
            }
        }
        return updated;
    }
    void updateRegistrations() throws IOException {
        synchronized (updateList) {
            while (updateList.size() > 0) {
                int index = 0;
                Updator u = null;
                while ((u = updateList.poll()) != null) {
                    putPollFD(updatePollArray, index, u.fd, POLLREMOVE);
                    index++;
                    if (u.mask != POLLREMOVE) {
                        putPollFD(updatePollArray, index, u.fd, (short)u.mask);
                        index++;
                    }
                    if (index >  MAX_UPDATE_SIZE - 2)
                        break;
                }
                registerMultiple(wfd, updatePollArray.address(), index);
             }
        }
    }
    private void putPollFD(AllocatedNativeObject array, int index, int fd,
                           short event)
    {
        int structIndex = SIZE_POLLFD * index;
        array.putInt(structIndex + FD_OFFSET, fd);
        array.putShort(structIndex + EVENT_OFFSET, event);
        array.putShort(structIndex + REVENT_OFFSET, (short)0);
    }
    boolean interrupted = false;
    public void interrupt() {
        interrupt(outgoingInterruptFD);
    }
    public int interruptedIndex() {
        return interruptedIndex;
    }
    boolean interrupted() {
        return interrupted;
    }
    void clearInterrupted() {
        interrupted = false;
    }
    private native int init();
    private native void register(int wfd, int fd, int mask);
    private native void registerMultiple(int wfd, long address, int len)
        throws IOException;
    private native int poll0(long pollAddress, int numfds, long timeout,
                             int wfd);
    private static native void interrupt(int fd);
    private static native int fdLimit();
}
