public class AcceptCauseFileDescriptorLeak {
    private static final int REPS = 2048;
    public static void main(String[] args) throws Exception {
        final ServerSocket ss = new ServerSocket(0) {
            public Socket accept() throws IOException {
                Socket s = new Socket() { };
                s.setSoTimeout(10000);
                implAccept(s);
                return s;
            }
        };
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < REPS; i++) {
                        (new Socket("localhost", ss.getLocalPort())).close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        for (int i = 0; i < REPS; i++) {
            ss.accept().close();
        }
        ss.close();
        t.join();
    }
}
