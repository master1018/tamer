public class EmptyRead {
    public static void main(String[] args) throws Exception {
        SelectorProvider sp = SelectorProvider.provider();
        Pipe p = sp.openPipe();
        Pipe.SinkChannel sink = p.sink();
        Pipe.SourceChannel source = p.source();
        byte[] someBytes = new byte[0];
        ByteBuffer outgoingdata = ByteBuffer.wrap(someBytes);
        int totalWritten = 0;
        int written = sink.write(outgoingdata);
        if (written < 0)
            throw new Exception("Write failed");
        ByteBuffer incomingdata = ByteBuffer.allocateDirect(0);
        int read = source.read(incomingdata);
        if (read < 0)
            throw new Exception("Read EOF");
        sink.close();
        source.close();
    }
}
