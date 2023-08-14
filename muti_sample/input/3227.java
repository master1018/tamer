public class CloseAndAvailableRC {
    public static void main(final String[] args) throws Exception {
        CloseAndAvailableRC rc = new CloseAndAvailableRC();
        rc.go();
    }
    private PipedInputStream inPipe = null;
    private PipedOutputStream outPipe = null;
    private Thread sink = null;
    private volatile boolean stop = false;
    private volatile boolean stopTest = false;
    private void go() throws Exception {
        for (long i=0; i<2000; i++) {
            if (stopTest) {
                cleanup();
                throw new RuntimeException("Test failed");
            }
            resetPipes();
            System.err.println("Closing...");
            inPipe.close();
        }
        cleanup();
    }
    private void cleanup() throws Exception {
        if (sink != null) {
            stop = true;
            sink.interrupt();
            try {
                sink.join();
            } catch (InterruptedException e) {
            }
            stop = false;
            outPipe.close();
        }
    }
    private void resetPipes() throws Exception {
        cleanup();
        inPipe = new PipedInputStream();
        outPipe = new PipedOutputStream(inPipe);
        for (byte b = 0; b < 10; b++)
            outPipe.write(b);
        sink = new Sink();
        sink.start();
    }
    private class Sink extends Thread {
        public void run() {
            while (!stop) {
                try {
                    final int num = inPipe.available();
                    if (num < 0) {
                        stopTest = true;
                    }
                } catch (final IOException e) {
                    System.err.println("Error on available:" + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        }
    }
}
