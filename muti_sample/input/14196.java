public class WriteAfterClose {
    public static void main(String argv[]) throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        in.close();
        try {
            out.write('a');
            throw new Exception("Should not allow write after close");
        } catch (IOException e) {
        }
    }
}
