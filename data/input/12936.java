public class ConnectAfterReaderClose {
    public static void main(String argv[]) throws Exception {
        PipedReader pr = new PipedReader();
        pr.close();
        try {
            PipedWriter pw = new PipedWriter(pr);
            throw new Exception("Should not allow connect after close");
        } catch (IOException e) {
        }
    }
}
