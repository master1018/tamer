public class SocketInheritance {
    static class IOHandler implements Runnable {
        InputStream in;
        IOHandler(InputStream in) {
            this.in = in;
        }
        static void handle(InputStream in) {
            IOHandler handler = new IOHandler(in);
            Thread thr = new Thread(handler);
            thr.setDaemon(true);
            thr.start();
        }
        public void run() {
            try {
                byte b[] = new byte[100];
                for (;;) {
                    int n = in.read(b);
                    if (n < 0) return;
                    System.out.write(b, 0, n);
                }
            } catch (IOException ioe) { }
        }
    }
    static SocketChannel connect(int port) throws IOException {
        InetAddress lh = InetAddress.getByName("127.0.0.1");
        InetSocketAddress isa = new InetSocketAddress(lh, port);
        return SocketChannel.open(isa);
    }
    static void child(int port) {
        try {
            connect(port).close();
        } catch (IOException x) {
            x.printStackTrace();
            return;
        }
        for (;;) {
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException x) { }
        }
    }
    static void start() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind( new InetSocketAddress(0) );
        int port = ssc.socket().getLocalPort();
        SocketChannel sc1 = connect(port);
        SocketChannel sc2 = ssc.accept();
        String cmd = System.getProperty("java.home") + File.separator + "bin" +
            File.separator + "java";
        String testClasses = System.getProperty("test.classes");
        if (testClasses != null)
            cmd += " -cp " + testClasses;
        cmd += " SocketInheritance -child " + port;
        Process p = Runtime.getRuntime().exec(cmd);
        IOHandler.handle(p.getInputStream());
        IOHandler.handle(p.getErrorStream());
        SocketChannel sc3 = ssc.accept();
        sc1.close();
        sc2.close();
        sc3.close();
        ssc.close();
        try {
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.close();
        } finally {
            p.destroy();
        }
    }
    public static void main(String[] args) throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows"))
            return;
        if (args.length == 0) {
            start();
        } else {
            if (args[0].equals("-child")) {
                child(Integer.parseInt(args[1]));
            }
        }
    }
}
