final class WindowsSelectorImpl extends SelectorImpl {
    private final int INIT_CAP = 8;
    private final static int MAX_SELECTABLE_FDS = 1024;
    private SelectionKeyImpl[] channelArray = new SelectionKeyImpl[INIT_CAP];
    private PollArrayWrapper pollWrapper;
    private int totalChannels = 1;
    private int threadsCount = 0;
    private final List<SelectThread> threads = new ArrayList<SelectThread>();
    private final Pipe wakeupPipe;
    private final int wakeupSourceFd, wakeupSinkFd;
    private Object closeLock = new Object();
    private final static class FdMap extends HashMap<Integer, MapEntry> {
        static final long serialVersionUID = 0L;
        private MapEntry get(int desc) {
            return get(new Integer(desc));
        }
        private MapEntry put(SelectionKeyImpl ski) {
            return put(new Integer(ski.channel.getFDVal()), new MapEntry(ski));
        }
        private MapEntry remove(SelectionKeyImpl ski) {
            Integer fd = new Integer(ski.channel.getFDVal());
            MapEntry x = get(fd);
            if ((x != null) && (x.ski.channel == ski.channel))
                return remove(fd);
            return null;
        }
    }
    private final static class MapEntry {
        SelectionKeyImpl ski;
        long updateCount = 0;
        long clearedCount = 0;
        MapEntry(SelectionKeyImpl ski) {
            this.ski = ski;
        }
    }
    private final FdMap fdMap = new FdMap();
    private final SubSelector subSelector = new SubSelector();
    private long timeout; 
    private final Object interruptLock = new Object();
    private volatile boolean interruptTriggered = false;
    WindowsSelectorImpl(SelectorProvider sp) throws IOException {
        super(sp);
        pollWrapper = new PollArrayWrapper(INIT_CAP);
        wakeupPipe = Pipe.open();
        wakeupSourceFd = ((SelChImpl)wakeupPipe.source()).getFDVal();
        SinkChannelImpl sink = (SinkChannelImpl)wakeupPipe.sink();
        (sink.sc).socket().setTcpNoDelay(true);
        wakeupSinkFd = ((SelChImpl)sink).getFDVal();
        pollWrapper.addWakeupSocket(wakeupSourceFd, 0);
    }
    protected int doSelect(long timeout) throws IOException {
        if (channelArray == null)
            throw new ClosedSelectorException();
        this.timeout = timeout; 
        processDeregisterQueue();
        if (interruptTriggered) {
            resetWakeupSocket();
            return 0;
        }
        adjustThreadsCount();
        finishLock.reset(); 
        startLock.startThreads();
        try {
            begin();
            try {
                subSelector.poll();
            } catch (IOException e) {
                finishLock.setException(e); 
            }
            if (threads.size() > 0)
                finishLock.waitForHelperThreads();
          } finally {
              end();
          }
        finishLock.checkForException();
        processDeregisterQueue();
        int updated = updateSelectedKeys();
        resetWakeupSocket();
        return updated;
    }
    private final StartLock startLock = new StartLock();
    private final class StartLock {
        private long runsCounter;
        private synchronized void startThreads() {
            runsCounter++; 
            notifyAll(); 
        }
        private synchronized boolean waitForStart(SelectThread thread) {
            while (true) {
                while (runsCounter == thread.lastRun) {
                    try {
                        startLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (thread.isZombie()) { 
                    return true; 
                } else {
                    thread.lastRun = runsCounter; 
                    return false; 
                }
            }
        }
    }
    private final FinishLock finishLock = new FinishLock();
    private final class FinishLock  {
        private int threadsToFinish;
        IOException exception = null;
        private void reset() {
            threadsToFinish = threads.size(); 
        }
        private synchronized void threadFinished() {
            if (threadsToFinish == threads.size()) { 
                wakeup();
            }
            threadsToFinish--;
            if (threadsToFinish == 0) 
                notify();             
        }
        private synchronized void waitForHelperThreads() {
            if (threadsToFinish == threads.size()) {
                wakeup();
            }
            while (threadsToFinish != 0) {
                try {
                    finishLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        private synchronized void setException(IOException e) {
            exception = e;
        }
        private void checkForException() throws IOException {
            if (exception == null)
                return;
            StringBuffer message =  new StringBuffer("An exception occured" +
                                       " during the execution of select(): \n");
            message.append(exception);
            message.append('\n');
            exception = null;
            throw new IOException(message.toString());
        }
    }
    private final class SubSelector {
        private final int pollArrayIndex; 
        private final int[] readFds = new int [MAX_SELECTABLE_FDS + 1];
        private final int[] writeFds = new int [MAX_SELECTABLE_FDS + 1];
        private final int[] exceptFds = new int [MAX_SELECTABLE_FDS + 1];
        private SubSelector() {
            this.pollArrayIndex = 0; 
        }
        private SubSelector(int threadIndex) { 
            this.pollArrayIndex = (threadIndex + 1) * MAX_SELECTABLE_FDS;
        }
        private int poll() throws IOException{ 
            return poll0(pollWrapper.pollArrayAddress,
                         Math.min(totalChannels, MAX_SELECTABLE_FDS),
                         readFds, writeFds, exceptFds, timeout);
        }
        private int poll(int index) throws IOException {
            return  poll0(pollWrapper.pollArrayAddress +
                     (pollArrayIndex * PollArrayWrapper.SIZE_POLLFD),
                     Math.min(MAX_SELECTABLE_FDS,
                             totalChannels - (index + 1) * MAX_SELECTABLE_FDS),
                     readFds, writeFds, exceptFds, timeout);
        }
        private native int poll0(long pollAddress, int numfds,
             int[] readFds, int[] writeFds, int[] exceptFds, long timeout);
        private int processSelectedKeys(long updateCount) {
            int numKeysUpdated = 0;
            numKeysUpdated += processFDSet(updateCount, readFds,
                                           PollArrayWrapper.POLLIN,
                                           false);
            numKeysUpdated += processFDSet(updateCount, writeFds,
                                           PollArrayWrapper.POLLCONN |
                                           PollArrayWrapper.POLLOUT,
                                           false);
            numKeysUpdated += processFDSet(updateCount, exceptFds,
                                           PollArrayWrapper.POLLIN |
                                           PollArrayWrapper.POLLCONN |
                                           PollArrayWrapper.POLLOUT,
                                           true);
            return numKeysUpdated;
        }
        private int processFDSet(long updateCount, int[] fds, int rOps,
                                 boolean isExceptFds)
        {
            int numKeysUpdated = 0;
            for (int i = 1; i <= fds[0]; i++) {
                int desc = fds[i];
                if (desc == wakeupSourceFd) {
                    synchronized (interruptLock) {
                        interruptTriggered = true;
                    }
                    continue;
                }
                MapEntry me = fdMap.get(desc);
                if (me == null)
                    continue;
                SelectionKeyImpl sk = me.ski;
                if (isExceptFds &&
                    (sk.channel() instanceof SocketChannelImpl) &&
                    discardUrgentData(desc))
                {
                    continue;
                }
                if (selectedKeys.contains(sk)) { 
                    if (me.clearedCount != updateCount) {
                        if (sk.channel.translateAndSetReadyOps(rOps, sk) &&
                            (me.updateCount != updateCount)) {
                            me.updateCount = updateCount;
                            numKeysUpdated++;
                        }
                    } else { 
                        if (sk.channel.translateAndUpdateReadyOps(rOps, sk) &&
                            (me.updateCount != updateCount)) {
                            me.updateCount = updateCount;
                            numKeysUpdated++;
                        }
                    }
                    me.clearedCount = updateCount;
                } else { 
                    if (me.clearedCount != updateCount) {
                        sk.channel.translateAndSetReadyOps(rOps, sk);
                        if ((sk.nioReadyOps() & sk.nioInterestOps()) != 0) {
                            selectedKeys.add(sk);
                            me.updateCount = updateCount;
                            numKeysUpdated++;
                        }
                    } else { 
                        sk.channel.translateAndUpdateReadyOps(rOps, sk);
                        if ((sk.nioReadyOps() & sk.nioInterestOps()) != 0) {
                            selectedKeys.add(sk);
                            me.updateCount = updateCount;
                            numKeysUpdated++;
                        }
                    }
                    me.clearedCount = updateCount;
                }
            }
            return numKeysUpdated;
        }
    }
    private final class SelectThread extends Thread {
        private final int index; 
        final SubSelector subSelector;
        private long lastRun = 0; 
        private volatile boolean zombie;
        private SelectThread(int i) {
            this.index = i;
            this.subSelector = new SubSelector(i);
            this.lastRun = startLock.runsCounter;
        }
        void makeZombie() {
            zombie = true;
        }
        boolean isZombie() {
            return zombie;
        }
        public void run() {
            while (true) { 
                if (startLock.waitForStart(this))
                    return;
                try {
                    subSelector.poll(index);
                } catch (IOException e) {
                    finishLock.setException(e);
                }
                finishLock.threadFinished();
            }
        }
    }
    private void adjustThreadsCount() {
        if (threadsCount > threads.size()) {
            for (int i = threads.size(); i < threadsCount; i++) {
                SelectThread newThread = new SelectThread(i);
                threads.add(newThread);
                newThread.setDaemon(true);
                newThread.start();
            }
        } else if (threadsCount < threads.size()) {
            for (int i = threads.size() - 1 ; i >= threadsCount; i--)
                threads.remove(i).makeZombie();
        }
    }
    private void setWakeupSocket() {
        setWakeupSocket0(wakeupSinkFd);
    }
    private native void setWakeupSocket0(int wakeupSinkFd);
    private void resetWakeupSocket() {
        synchronized (interruptLock) {
            if (interruptTriggered == false)
                return;
            resetWakeupSocket0(wakeupSourceFd);
            interruptTriggered = false;
        }
    }
    private native void resetWakeupSocket0(int wakeupSourceFd);
    private native boolean discardUrgentData(int fd);
    private long updateCount = 0;
    private int updateSelectedKeys() {
        updateCount++;
        int numKeysUpdated = 0;
        numKeysUpdated += subSelector.processSelectedKeys(updateCount);
        for (SelectThread t: threads) {
            numKeysUpdated += t.subSelector.processSelectedKeys(updateCount);
        }
        return numKeysUpdated;
    }
    protected void implClose() throws IOException {
        synchronized (closeLock) {
            if (channelArray != null) {
                if (pollWrapper != null) {
                    synchronized (interruptLock) {
                        interruptTriggered = true;
                    }
                    wakeupPipe.sink().close();
                    wakeupPipe.source().close();
                    for(int i = 1; i < totalChannels; i++) { 
                        if (i % MAX_SELECTABLE_FDS != 0) { 
                            deregister(channelArray[i]);
                            SelectableChannel selch = channelArray[i].channel();
                            if (!selch.isOpen() && !selch.isRegistered())
                                ((SelChImpl)selch).kill();
                        }
                    }
                    pollWrapper.free();
                    pollWrapper = null;
                    selectedKeys = null;
                    channelArray = null;
                    for (SelectThread t: threads)
                         t.makeZombie();
                    startLock.startThreads();
                }
            }
        }
    }
    protected void implRegister(SelectionKeyImpl ski) {
        synchronized (closeLock) {
            if (pollWrapper == null)
                throw new ClosedSelectorException();
            growIfNeeded();
            channelArray[totalChannels] = ski;
            ski.setIndex(totalChannels);
            fdMap.put(ski);
            keys.add(ski);
            pollWrapper.addEntry(totalChannels, ski);
            totalChannels++;
        }
    }
    private void growIfNeeded() {
        if (channelArray.length == totalChannels) {
            int newSize = totalChannels * 2; 
            SelectionKeyImpl temp[] = new SelectionKeyImpl[newSize];
            System.arraycopy(channelArray, 1, temp, 1, totalChannels - 1);
            channelArray = temp;
            pollWrapper.grow(newSize);
        }
        if (totalChannels % MAX_SELECTABLE_FDS == 0) { 
            pollWrapper.addWakeupSocket(wakeupSourceFd, totalChannels);
            totalChannels++;
            threadsCount++;
        }
    }
    protected void implDereg(SelectionKeyImpl ski) throws IOException{
        int i = ski.getIndex();
        assert (i >= 0);
        if (i != totalChannels - 1) {
            SelectionKeyImpl endChannel = channelArray[totalChannels-1];
            channelArray[i] = endChannel;
            endChannel.setIndex(i);
            pollWrapper.replaceEntry(pollWrapper, totalChannels - 1,
                                                                pollWrapper, i);
        }
        channelArray[totalChannels - 1] = null;
        totalChannels--;
        ski.setIndex(-1);
        if ( totalChannels != 1 && totalChannels % MAX_SELECTABLE_FDS == 1) {
            totalChannels--;
            threadsCount--; 
        }
        fdMap.remove(ski); 
        keys.remove(ski);
        selectedKeys.remove(ski);
        deregister(ski);
        SelectableChannel selch = ski.channel();
        if (!selch.isOpen() && !selch.isRegistered())
            ((SelChImpl)selch).kill();
    }
    void putEventOps(SelectionKeyImpl sk, int ops) {
        synchronized (closeLock) {
            if (pollWrapper == null)
                throw new ClosedSelectorException();
            pollWrapper.putEventOps(sk.getIndex(), ops);
        }
    }
    public Selector wakeup() {
        synchronized (interruptLock) {
            if (!interruptTriggered) {
                setWakeupSocket();
                interruptTriggered = true;
            }
        }
        return this;
    }
    static {
        Util.load();
    }
}
