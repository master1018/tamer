public class FasterWriter implements Runnable {
    static PipedInputStream is;
    static PipedOutputStream os;
    public void run() {
        try {
            os.write(0);
            os.write(0);
            os.write(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        is = new PipedInputStream();
        os = new PipedOutputStream(is);
        Thread t = new Thread(new FasterWriter());
        t.start();
        t.join();
        try {
            is.read();
        } catch (IOException e) {
            throw new Exception("Cannot read remaining data in the pipe");
        }
    }
}
