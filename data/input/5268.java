abstract class AbstractPollSelectorImpl
    extends SelectorImpl
{
    PollArrayWrapper pollWrapper;
    protected final int INIT_CAP = 10;
    protected SelectionKeyImpl[] channelArray;
    protected int channelOffset = 0;
    protected int totalChannels;
    private boolean closed = false;
    private Object closeLock = new Object();
    AbstractPollSelectorImpl(SelectorProvider sp, int channels, int offset) {
        super(sp);
        this.totalChannels = channels;
        this.channelOffset = offset;
    }
    void putEventOps(SelectionKeyImpl sk, int ops) {
        synchronized (closeLock) {
            if (closed)
                throw new ClosedSelectorException();
            pollWrapper.putEventOps(sk.getIndex(), ops);
        }
    }
    public Selector wakeup() {
        pollWrapper.interrupt();
        return this;
    }
    protected abstract int doSelect(long timeout) throws IOException;
    protected void implClose() throws IOException {
        synchronized (closeLock) {
            if (closed)
                return;
            closed = true;
            for(int i=channelOffset; i<totalChannels; i++) {
                SelectionKeyImpl ski = channelArray[i];
                assert(ski.getIndex() != -1);
                ski.setIndex(-1);
                deregister(ski);
                SelectableChannel selch = channelArray[i].channel();
                if (!selch.isOpen() && !selch.isRegistered())
                    ((SelChImpl)selch).kill();
            }
            implCloseInterrupt();
            pollWrapper.free();
            pollWrapper = null;
            selectedKeys = null;
            channelArray = null;
            totalChannels = 0;
        }
    }
    protected abstract void implCloseInterrupt() throws IOException;
    protected int updateSelectedKeys() {
        int numKeysUpdated = 0;
        for (int i=channelOffset; i<totalChannels; i++) {
            int rOps = pollWrapper.getReventOps(i);
            if (rOps != 0) {
                SelectionKeyImpl sk = channelArray[i];
                pollWrapper.putReventOps(i, 0);
                if (selectedKeys.contains(sk)) {
                    if (sk.channel.translateAndSetReadyOps(rOps, sk)) {
                        numKeysUpdated++;
                    }
                } else {
                    sk.channel.translateAndSetReadyOps(rOps, sk);
                    if ((sk.nioReadyOps() & sk.nioInterestOps()) != 0) {
                        selectedKeys.add(sk);
                        numKeysUpdated++;
                    }
                }
            }
        }
        return numKeysUpdated;
    }
    protected void implRegister(SelectionKeyImpl ski) {
        synchronized (closeLock) {
            if (closed)
                throw new ClosedSelectorException();
            if (channelArray.length == totalChannels) {
                int newSize = pollWrapper.totalChannels * 2;
                SelectionKeyImpl temp[] = new SelectionKeyImpl[newSize];
                for (int i=channelOffset; i<totalChannels; i++)
                    temp[i] = channelArray[i];
                channelArray = temp;
                pollWrapper.grow(newSize);
            }
            channelArray[totalChannels] = ski;
            ski.setIndex(totalChannels);
            pollWrapper.addEntry(ski.channel);
            totalChannels++;
            keys.add(ski);
        }
    }
    protected void implDereg(SelectionKeyImpl ski) throws IOException {
        int i = ski.getIndex();
        assert (i >= 0);
        if (i != totalChannels - 1) {
            SelectionKeyImpl endChannel = channelArray[totalChannels-1];
            channelArray[i] = endChannel;
            endChannel.setIndex(i);
            pollWrapper.release(i);
            PollArrayWrapper.replaceEntry(pollWrapper, totalChannels - 1,
                                          pollWrapper, i);
        } else {
            pollWrapper.release(i);
        }
        channelArray[totalChannels-1] = null;
        totalChannels--;
        pollWrapper.totalChannels--;
        ski.setIndex(-1);
        keys.remove(ski);
        selectedKeys.remove(ski);
        deregister((AbstractSelectionKey)ski);
        SelectableChannel selch = ski.channel();
        if (!selch.isOpen() && !selch.isRegistered())
            ((SelChImpl)selch).kill();
    }
    static {
        Util.load();
    }
}
