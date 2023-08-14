final class PipeImpl extends Pipe {
    private SinkChannelImpl sink;
    private SourceChannelImpl source;
    private int serverPort;
    public PipeImpl() throws IOException {
        super();
        try {
            sink = new SinkChannelImpl(SelectorProvider.provider());
            source = new SourceChannelImpl(SelectorProvider.provider());
            sink.finishConnect();
            source.accept();
            source.closeServer();
        } catch(IOException ioe){
            reset();
            throw ioe;
        } catch(RuntimeException e){
            reset();
            throw e;
        }
    }
    private void reset(){
        if(sink != null){
            try {
                sink.close();
            } catch (Exception e) {
            }
        }
        if(source != null){
            try {
                source.closeServer();
            } catch (Exception e) {
            }
            try {
                source.close();
            } catch (Exception e) {
            }
        }
    }
    public SinkChannel sink() {
        return sink;
    }
    public SourceChannel source() {
        return source;
    }
    private class SourceChannelImpl extends Pipe.SourceChannel implements
            FileDescriptorHandler {
        private SocketChannelImpl sourceSocket;
        private ServerSocketChannel sourceServer;
        protected SourceChannelImpl(SelectorProvider provider)
                throws IOException {
            super(provider);
            sourceServer = provider.openServerSocketChannel();
            sourceServer.socket().bind(
                new InetSocketAddress(InetAddress.getByName(null), 0));
            serverPort = sourceServer.socket().getLocalPort();
        }
        void closeServer() throws IOException {
            sourceServer.close();
        }
        void accept() throws IOException {
            sourceSocket = (SocketChannelImpl) sourceServer.accept();
        }
        protected void implCloseSelectableChannel() throws IOException {
            sourceSocket.close();
        }
        protected void implConfigureBlocking(boolean blockingMode)
                throws IOException {
            sourceSocket.configureBlocking(blockingMode);
        }
        public int read(ByteBuffer buffer) throws IOException {
            return sourceSocket.read(buffer);
        }
        public long read(ByteBuffer[] buffers) throws IOException {
            return read(buffers, 0, buffers.length);
        }
        public long read(ByteBuffer[] buffers, int offset, int length)
                throws IOException {
            return sourceSocket.read(buffers, offset, length);
        }
        public FileDescriptor getFD() {
            return sourceSocket.getFD();
        }
    }
    private class SinkChannelImpl extends Pipe.SinkChannel implements
            FileDescriptorHandler {
        private SocketChannelImpl sinkSocket;
        protected SinkChannelImpl(SelectorProvider provider) throws IOException {
            super(provider);
            sinkSocket = (SocketChannelImpl) provider.openSocketChannel();
        }
        public boolean finishConnect() throws IOException {
            return sinkSocket.connect(
                new InetSocketAddress(InetAddress.getByName(null), serverPort));
        }
        protected void implCloseSelectableChannel() throws IOException {
            sinkSocket.close();
        }
        protected void implConfigureBlocking(boolean blockingMode)
                throws IOException {
            sinkSocket.configureBlocking(blockingMode);
        }
        public int write(ByteBuffer buffer) throws IOException {
            return sinkSocket.write(buffer);
        }
        public long write(ByteBuffer[] buffers) throws IOException {
            return write(buffers, 0, buffers.length);
        }
        public long write(ByteBuffer[] buffers, int offset, int length)
                throws IOException {
            return sinkSocket.write(buffers, offset, length);
        }
        public FileDescriptor getFD() {
            return sinkSocket.getFD();
        }
    }
}
