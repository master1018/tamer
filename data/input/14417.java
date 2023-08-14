public class B6558853 implements Runnable {
    private InetAddress addr = null;
    private int port = 0;
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        Enumeration<NetworkInterface> l = NetworkInterface.getNetworkInterfaces();
        InetAddress dest = null;
        while (l.hasMoreElements() && dest == null) {
            NetworkInterface nif = l.nextElement();
            if (!nif.isUp())
                continue;
            for (InterfaceAddress a : nif.getInterfaceAddresses()) {
                if (a.getAddress() instanceof Inet6Address) {
                    Inet6Address a6 = (Inet6Address) a.getAddress();
                    if (a6.isLinkLocalAddress()) {
                        dest = a6;
                    }
                    break;
                }
            }
        }
        System.out.println("Using " + dest);
        if (dest != null) {
            B6558853 test = new B6558853(dest, port);
            Thread thread = new Thread(test);
            thread.start();
            Socket s = ss.accept();
            InetAddress a = s.getInetAddress();
            OutputStream out = s.getOutputStream();
            out.write(1);
            out.close();
            if (!(a instanceof Inet6Address) || a.getHostAddress().indexOf("%") == -1) {
                throw new RuntimeException("Wrong address: " + a.getHostAddress());
            }
        }
    }
    public B6558853(InetAddress a, int port) {
        addr = a;
        this.port = port;
    }
    public void run() {
        try {
            Socket s = new Socket(addr, port);
            InputStream in = s.getInputStream();
            int i = in.read();
            in.close();
        } catch (IOException iOException) {
        }
    }
}
