class SelectionKeyImpl
    extends AbstractSelectionKey
{
    final SelChImpl channel;                            
    final SelectorImpl selector;                        
    private int index;
    private volatile int interestOps;
    private int readyOps;
    SelectionKeyImpl(SelChImpl ch, SelectorImpl sel) {
        channel = ch;
        selector = sel;
    }
    public SelectableChannel channel() {
        return (SelectableChannel)channel;
    }
    public Selector selector() {
        return selector;
    }
    int getIndex() {                                    
        return index;
    }
    void setIndex(int i) {                              
        index = i;
    }
    private void ensureValid() {
        if (!isValid())
            throw new CancelledKeyException();
    }
    public int interestOps() {
        ensureValid();
        return interestOps;
    }
    public SelectionKey interestOps(int ops) {
        ensureValid();
        return nioInterestOps(ops);
    }
    public int readyOps() {
        ensureValid();
        return readyOps;
    }
    void nioReadyOps(int ops) {                 
        readyOps = ops;
    }
    int nioReadyOps() {                         
        return readyOps;
    }
    SelectionKey nioInterestOps(int ops) {      
        if ((ops & ~channel().validOps()) != 0)
            throw new IllegalArgumentException();
        channel.translateAndSetInterestOps(ops, this);
        interestOps = ops;
        return this;
    }
    int nioInterestOps() {                       
        return interestOps;
    }
}
