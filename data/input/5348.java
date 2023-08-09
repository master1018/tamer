public class Test4822050 implements ExceptionListener, Runnable {
    private static final int THREADS = 40;
    private static final int ATTEMPTS = 100;
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(baos);
        encoder.writeObject(new JLabel("hello")); 
        encoder.close();
        byte[] buffer = baos.toByteArray();
        for (int i = 0; i < THREADS; i++)
            start(buffer);
    }
    private static void start(byte[] buffer) {
        Thread thread = new Thread(new Test4822050(buffer));
        thread.start();
    }
    private byte[] buffer;
    public Test4822050(byte[] buffer) {
        this.buffer = buffer;
    }
    public void exceptionThrown(Exception exception) {
        throw new Error(exception);
    }
    public void run() {
        for (int i = 0; i < ATTEMPTS; i++)
            parse();
    }
    private void parse() {
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(this.buffer));
        decoder.readObject();
        decoder.close();
    }
}
