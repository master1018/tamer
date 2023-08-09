public class FlushAfterClose {
    public static void main(String argv[]) throws Exception {
        PipedReader pr = new PipedReader();
        PipedWriter pw = new PipedWriter(pr);
        pw.close();
        try {
            pw.flush();
            throw new Exception("Should not allow flush after close");
        } catch (IOException e) {
        }
    }
}
