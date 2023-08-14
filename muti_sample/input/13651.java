public class Constructors extends Thread {
    static PipedOutputStream out;
    static PipedInputStream  in;
    static int totalToWrite = (8 * 1024);
    static int pipeSize = totalToWrite;
    public void run() {
        try {
            for (int times = (totalToWrite / pipeSize); times > 0; times--) {
                System.out.println("Pipe Input stream reading...");
                int read = in.read(new byte[pipeSize]);
                System.out.println("read: " + read);
                if (read < pipeSize) {
                    throw new Exception("Pipe Size is not set to:" + pipeSize);
                }
            }
        } catch (Throwable e) {
            System.out.println("Pipe Input stream exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Pipe Input stream done.");
        }
    }
    public static void main(String args[]) throws Exception {
        in = new PipedInputStream(pipeSize);
        out = new PipedOutputStream(in);
        testPipe();
        out = new PipedOutputStream();
        in = new PipedInputStream(out, pipeSize);
        testPipe();
   }
   private static void testPipe() throws Exception {
        Constructors reader = new Constructors();
        reader.start();
        try {
            System.out.println("Pipe Output stream started.");
            out.write(new byte[totalToWrite]);
        } catch (Throwable e) {
            System.out.println("Pipe Output stream exception:");
            e.printStackTrace();
        } finally {
            out.close();
            System.out.println("Waiting for Pipe Input stream...");
            reader.join();
            in.close();
            System.out.println("Done.");
        }
    }
}
