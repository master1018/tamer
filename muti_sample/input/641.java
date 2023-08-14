public class PipeChannel {
    private static Random generator = new Random();
    public static void main(String[] args) throws Exception {
        for (int x=0; x<100; x++) {
            SelectorProvider sp = SelectorProvider.provider();
            Pipe p = sp.openPipe();
            Pipe.SinkChannel sink = p.sink();
            Pipe.SourceChannel source = p.source();
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
            ByteBuffer incomingdata = ByteBuffer.allocateDirect(10);
            int totalRead = 0;
            do {
                int bytesRead = source.read(incomingdata);
                if (bytesRead > 0)
                    totalRead += bytesRead;
            } while(totalRead < 10);
            for(int i=0; i<10; i++)
                if (outgoingdata.get(i) != incomingdata.get(i))
                    throw new Exception("Pipe failed");
            sink.close();
            source.close();
        }
    }
}
