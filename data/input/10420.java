public class ClosedWriter implements Runnable {
    static PipedInputStream is;
    static PipedOutputStream os;
    public void run() {
        try {
            os.write(0);
            os.write(0);
            os.write(0);
            os.write(0);
            os.write(0);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        is = new PipedInputStream();
        os = new PipedOutputStream();
        is.connect(os);
        Thread t = new Thread(new ClosedWriter());
        t.start();
        while (is.read() != -1) {
        }
    }
}
