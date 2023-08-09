public class DeadReader {
    public static void main(String[] argv) throws Exception {
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream();
        is.connect(os);
        LazyReader lr = new LazyReader(is);
        os.write(new byte[1000]);
        lr.start();
        while (lr.isAlive()) {
            Thread.sleep(100);
        }
        try{
            os.write(27);
            throw new Exception
                ("Test failed: shouldn't be able to write");
        } catch (IOException e) {
        }
    }
}
class LazyReader extends Thread {
    private PipedInputStream snk;
    private int delay;
    public LazyReader(PipedInputStream snk) {
        this.snk = snk;
    }
    public void run() {
        try {
            snk.read();
        } catch (Exception e) {
            System.err.println("Test failed: unexpected exception");
        }
        return;
    }
}
