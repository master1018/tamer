public abstract class SelectionKey {
    protected SelectionKey() { }
    public abstract SelectableChannel channel();
    public abstract Selector selector();
    public abstract boolean isValid();
    public abstract void cancel();
    public abstract int interestOps();
    public abstract SelectionKey interestOps(int ops);
    public abstract int readyOps();
    public static final int OP_READ = 1 << 0;
    public static final int OP_WRITE = 1 << 2;
    public static final int OP_CONNECT = 1 << 3;
    public static final int OP_ACCEPT = 1 << 4;
    public final boolean isReadable() {
        return (readyOps() & OP_READ) != 0;
    }
    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) != 0;
    }
    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) != 0;
    }
    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) != 0;
    }
    private volatile Object attachment = null;
    private static final AtomicReferenceFieldUpdater<SelectionKey,Object>
        attachmentUpdater = AtomicReferenceFieldUpdater.newUpdater(
            SelectionKey.class, Object.class, "attachment"
        );
    public final Object attach(Object ob) {
        return attachmentUpdater.getAndSet(this, ob);
    }
    public final Object attachment() {
        return attachment;
    }
}
