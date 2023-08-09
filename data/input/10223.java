public class WriteAfterReaderClose {
    public static void main(String argv[]) throws Exception {
        PipedReader pr = new PipedReader();
        PipedWriter pw = new PipedWriter(pr);
        pr.close();
        try {
            pw.write('a');
            throw new Exception("Should not allow write after close");
        } catch (IOException e) {
        }
    }
}
