public abstract class SelectionKey {
    public static final int OP_ACCEPT = 16;
    public static final int OP_CONNECT = 8;
    public static final int OP_READ = 1;
    public static final int OP_WRITE = 4;
    private volatile Object attachment = null;
    protected SelectionKey() {
        super();
    }
    public final Object attach(Object anObject) {
        Object oldAttachment = attachment;
        attachment = anObject;
        return oldAttachment;
    }
    public final Object attachment() {
        return attachment;
    }
    public abstract void cancel();
    public abstract SelectableChannel channel();
    public abstract int interestOps();
    public abstract SelectionKey interestOps(int operations);
    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) == OP_ACCEPT;
    }
    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) == OP_CONNECT;
    }
    public final boolean isReadable() {
        return (readyOps() & OP_READ) == OP_READ;
    }
    public abstract boolean isValid();
    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) == OP_WRITE;
    }
    public abstract int readyOps();
    public abstract Selector selector();
}
