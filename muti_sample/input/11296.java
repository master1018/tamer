public class BrokenPipe {
    private static class Closer implements Runnable {
        private final Socket s;
        Closer(Socket s) {
            this.s = s;
        }
        public void run() {
            try {
                Thread.sleep(5000);
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Socket client = new Socket(InetAddress.getLocalHost(),
                                   ss.getLocalPort());
        Socket server = ss.accept();
        ss.close();
        new Thread(new Closer(server)).start();
        try {
            client.getOutputStream().write(new byte[1000000]);
        } catch (IOException ioe) {
            String text = ioe.getMessage();
            if (text.toLowerCase().indexOf("closed") >= 0) {
                throw ioe;
            }
        } finally {
            server.close();
        }
    }
}
