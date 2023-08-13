public class WriterLoop extends Thread {
    static PipedOutputStream out;
    static PipedInputStream  in;
    public void run() {
        try {
            System.out.println("Writer started.");
            out.write(new byte[64*1024]);
        } catch (Throwable e) {
            System.out.println("Writer exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Writer done.");
        }
    }
    public static void main(String args[]) throws Exception {
        in = new PipedInputStream();
        out = new PipedOutputStream(in);
        WriterLoop writer = new WriterLoop();
        writer.start();
        try {
            System.out.println("Reader reading...");
            in.read(new byte[2048]);
            System.out.println("Reader closing stream...");
            in.close();
            System.out.println("Reader sleeping 3 seconds...");
            Thread.sleep(3000);
        } catch (Throwable e) {
            System.out.println("Reader exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Active threads:");
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            for (int i = 0; i < threads.length; i++) {
                System.out.println("  " + threads[i]);
            }
            System.out.println("Waiting for writer...");
            writer.join();
            System.out.println("Done.");
        }
    }
}
