public class SelectPipe {
    private static Random generator = new Random();
    public static void main(String[] args) throws Exception {
        SelectorProvider sp = SelectorProvider.provider();
        Selector selector = Selector.open();
        Pipe p = sp.openPipe();
        Pipe.SinkChannel sink = p.sink();
        Pipe.SourceChannel source = p.source();
        source.configureBlocking(false);
        sink.configureBlocking(false);
        SelectionKey readkey = source.register(selector, SelectionKey.OP_READ);
        SelectionKey writekey = sink.register(selector, SelectionKey.OP_WRITE);
        ByteBuffer outgoingdata = ByteBuffer.allocateDirect(10);
        byte[] someBytes = new byte[10];
        generator.nextBytes(someBytes);
        outgoingdata.put(someBytes);
        outgoingdata.flip();
        int totalWritten = 0;
        while (totalWritten < 10) {
            int written = sink.write(outgoingdata);
            if (written < 0)
                throw new Exception("Write failed");
            totalWritten += written;
        }
        if (selector.select(1000) == 0) {
            throw new Exception("test failed");
        }
        ByteBuffer incomingdata = ByteBuffer.allocateDirect(10);
        int totalRead = 0;
        do {
            int bytesRead = source.read(incomingdata);
            if (bytesRead > 0)
                totalRead += bytesRead;
        } while(totalRead < 10);
        sink.close();
        source.close();
        selector.close();
        for(int i=0; i<10; i++)
            if (outgoingdata.get(i) != incomingdata.get(i))
                throw new Exception("Pipe failed");
    }
}
