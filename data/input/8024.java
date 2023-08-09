public class LotsOfChannels {
    private final static int PIPES_COUNT = 256;
    private final static int BUF_SIZE = 8192;
    private final static int LOOPS = 10;
    public static void main(String[] argv) throws Exception {
        Pipe[] pipes = new Pipe[PIPES_COUNT];
        Pipe pipe = Pipe.open();
        Pipe.SinkChannel sink = pipe.sink();
        Pipe.SourceChannel source = pipe.source();
        Selector sel = Selector.open();
        source.configureBlocking(false);
        source.register(sel, SelectionKey.OP_READ);
        for (int i = 0; i< PIPES_COUNT; i++ ) {
            pipes[i]= Pipe.open();
            Pipe.SourceChannel sc = pipes[i].source();
            sc.configureBlocking(false);
            sc.register(sel, SelectionKey.OP_READ);
            Pipe.SinkChannel sc2 = pipes[i].sink();
            sc2.configureBlocking(false);
            sc2.register(sel, SelectionKey.OP_WRITE);
        }
        for (int i = 0 ; i < LOOPS; i++ ) {
            sink.write(ByteBuffer.allocate(BUF_SIZE));
            int x = sel.selectNow();
            sel.selectedKeys().clear();
            source.read(ByteBuffer.allocate(BUF_SIZE));
        }
        for (int i = 0; i < PIPES_COUNT; i++ ) {
            pipes[i].sink().close();
            pipes[i].source().close();
        }
        pipe.sink().close();
        pipe.source().close();
        sel.close();
    }
}
