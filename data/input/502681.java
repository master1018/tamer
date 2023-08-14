public abstract class Pipe {
    public static abstract class SinkChannel extends AbstractSelectableChannel
            implements WritableByteChannel, GatheringByteChannel {
        protected SinkChannel(SelectorProvider provider) {
            super(provider);
        }
        @Override
        public final int validOps() {
            return SelectionKey.OP_WRITE;
        }
    }
    public static abstract class SourceChannel extends
            AbstractSelectableChannel implements ReadableByteChannel,
            ScatteringByteChannel {
        protected SourceChannel(SelectorProvider provider) {
            super(provider);
        }
        @Override
        public final int validOps() {
            return SelectionKey.OP_READ;
        }
    }
    public static Pipe open() throws IOException {
        return SelectorProvider.provider().openPipe();
    }
    protected Pipe() {
        super();
    }
    public abstract SinkChannel sink();
    public abstract SourceChannel source();
}
