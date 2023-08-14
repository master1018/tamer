class EPollArrayWrapper {
    static final int EPOLLIN      = 0x001;
    static final int EPOLL_CTL_ADD      = 1;
    static final int EPOLL_CTL_DEL      = 2;
    static final int EPOLL_CTL_MOD      = 3;
    static final int SIZE_EPOLLEVENT  = sizeofEPollEvent();
    static final int EVENT_OFFSET     = 0;
    static final int DATA_OFFSET      = offsetofData();
    static final int FD_OFFSET        = DATA_OFFSET;
    static final int NUM_EPOLLEVENTS  = Math.min(fdLimit(), 8192);
    private final long pollArrayAddress;
    private final HashSet<SelChImpl> idleSet;
    EPollArrayWrapper() {
        epfd = epollCreate();
        int allocationSize = NUM_EPOLLEVENTS * SIZE_EPOLLEVENT;
        pollArray = new AllocatedNativeObject(allocationSize, true);
        pollArrayAddress = pollArray.address();
        for (int i=0; i<NUM_EPOLLEVENTS; i++) {
            putEventOps(i, 0);
            putData(i, 0L);
        }
        idleSet = new HashSet<SelChImpl>();
    }
    private static class Updator {
        SelChImpl channel;
        int opcode;
        int events;
        Updator(SelChImpl channel, int opcode, int events) {
            this.channel = channel;
            this.opcode = opcode;
            this.events = events;
        }
        Updator(SelChImpl channel, int opcode) {
            this(channel, opcode, 0);
        }
    }
    private LinkedList<Updator> updateList = new LinkedList<Updator>();
    private AllocatedNativeObject pollArray;
    final int epfd;
    int outgoingInterruptFD;
    int incomingInterruptFD;
    int interruptedIndex;
    int updated;
    void initInterrupt(int fd0, int fd1) {
        outgoingInterruptFD = fd1;
        incomingInterruptFD = fd0;
        epollCtl(epfd, EPOLL_CTL_ADD, fd0, EPOLLIN);
    }
    void putEventOps(int i, int event) {
        int offset = SIZE_EPOLLEVENT * i + EVENT_OFFSET;
        pollArray.putInt(offset, event);
    }
    void putData(int i, long value) {
        int offset = SIZE_EPOLLEVENT * i + DATA_OFFSET;
        pollArray.putLong(offset, value);
    }
    void putDescriptor(int i, int fd) {
        int offset = SIZE_EPOLLEVENT * i + FD_OFFSET;
        pollArray.putInt(offset, fd);
    }
    int getEventOps(int i) {
        int offset = SIZE_EPOLLEVENT * i + EVENT_OFFSET;
        return pollArray.getInt(offset);
    }
    int getDescriptor(int i) {
        int offset = SIZE_EPOLLEVENT * i + FD_OFFSET;
        return pollArray.getInt(offset);
    }
    void setInterest(SelChImpl channel, int mask) {
        synchronized (updateList) {
            if (updateList.size() > 0) {
                Updator last = updateList.getLast();
                if (last.channel == channel && last.opcode == EPOLL_CTL_ADD) {
                    last.events = mask;
                    return;
                }
            }
            updateList.add(new Updator(channel, EPOLL_CTL_MOD, mask));
        }
    }
    void add(SelChImpl channel) {
        synchronized (updateList) {
            updateList.add(new Updator(channel, EPOLL_CTL_ADD));
        }
    }
    void release(SelChImpl channel) {
        synchronized (updateList) {
            for (Iterator<Updator> it = updateList.iterator(); it.hasNext();) {
                if (it.next().channel == channel) {
                    it.remove();
                }
            }
            idleSet.remove(channel);
            epollCtl(epfd, EPOLL_CTL_DEL, channel.getFDVal(), 0);
        }
    }
    void closeEPollFD() throws IOException {
        FileDispatcherImpl.closeIntFD(epfd);
        pollArray.free();
    }
    int poll(long timeout) throws IOException {
        updateRegistrations();
        updated = epollWait(pollArrayAddress, NUM_EPOLLEVENTS, timeout, epfd);
        for (int i=0; i<updated; i++) {
            if (getDescriptor(i) == incomingInterruptFD) {
                interruptedIndex = i;
                interrupted = true;
                break;
            }
        }
        return updated;
    }
    void updateRegistrations() {
        synchronized (updateList) {
            Updator u = null;
            while ((u = updateList.poll()) != null) {
                SelChImpl ch = u.channel;
                if (!ch.isOpen())
                    continue;
                if (u.events == 0) {
                    boolean added = idleSet.add(u.channel);
                    if (added && (u.opcode == EPOLL_CTL_MOD))
                        epollCtl(epfd, EPOLL_CTL_DEL, ch.getFDVal(), 0);
                } else {
                    boolean idle = false;
                    if (!idleSet.isEmpty())
                        idle = idleSet.remove(u.channel);
                    int opcode = (idle) ? EPOLL_CTL_ADD : u.opcode;
                    epollCtl(epfd, opcode, ch.getFDVal(), u.events);
                }
            }
        }
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
    static {
        init();
    }
    private native int epollCreate();
    private native void epollCtl(int epfd, int opcode, int fd, int events);
    private native int epollWait(long pollAddress, int numfds, long timeout,
                                 int epfd) throws IOException;
    private static native int sizeofEPollEvent();
    private static native int offsetofData();
    private static native int fdLimit();
    private static native void interrupt(int fd);
    private static native void init();
}
