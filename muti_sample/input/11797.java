public class LocalRMIServerSocketFactoryTest {
    private static final SynchronousQueue<Exception> queue =
            new SynchronousQueue<Exception>();
    static final class Result extends Exception {
        private Result() {
            super("SUCCESS: No exception was thrown");
        }
        static final Result SUCCESS = new Result();
    }
    private static void checkError(String message) throws Exception {
        final Exception x = queue.take();
        if (x == Result.SUCCESS) {
            return;
        }
        if (x instanceof NullPointerException) {
            throw new Exception(message + " - " +
                    "Congratulations! it seems you have triggered 6674166. " +
                    "Neither 6674166 nor 6774170 seem to be fixed: " + x, x);
        } else if (x instanceof IOException) {
            throw new Exception(message + " - " +
                    "Unexpected IOException. Maybe you triggered 6674166? " +
                    x, x);
        } else if (x != null) {
            throw new Exception(message + " - " +
                    "Ouch, that's bad. " +
                    "This is a new kind of unexpected exception " +
                    x, x);
        }
    }
    public static void main(String[] args) throws Exception {
        final LocalRMIServerSocketFactory f =
                new LocalRMIServerSocketFactory();
        final ServerSocket s = f.createServerSocket(0);
        final int port = s.getLocalPort();
        Thread t = new Thread() {
            public void run() {
                while (true) {
                    Exception error = Result.SUCCESS;
                    try {
                        System.err.println("Accepting: ");
                        final Socket ss = s.accept();
                        System.err.println(ss.getInetAddress() + " accepted");
                    } catch (Exception x) {
                        x.printStackTrace();
                        error = x;
                    } finally {
                        try {
                            queue.put(error);
                        } catch (Exception x) {
                            System.err.println("Could't send result to client!");
                            x.printStackTrace();
                            return;
                        }
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
        System.err.println("new Socket((String)null, port)");
        final Socket s1 = new Socket((String) null, port);
        checkError("new Socket((String)null, port)");
        s1.close();
        System.err.println("new Socket((String)null, port): PASSED");
        System.err.println("new Socket(InetAddress.getByName(null), port)");
        final Socket s2 = new Socket(InetAddress.getByName(null), port);
        checkError("new Socket(InetAddress.getByName(null), port)");
        s2.close();
        System.err.println("new Socket(InetAddress.getByName(null), port): PASSED");
        System.err.println("new Socket(localhost, port)");
        final Socket s3 = new Socket("localhost", port);
        checkError("new Socket(localhost, port)");
        s3.close();
        System.err.println("new Socket(localhost, port): PASSED");
        System.err.println("new Socket(127.0.0.1, port)");
        final Socket s4 = new Socket("127.0.0.1", port);
        checkError("new Socket(127.0.0.1, port)");
        s4.close();
        System.err.println("new Socket(127.0.0.1, port): PASSED");
    }
}
