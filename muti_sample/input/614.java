public class SetSoLinger {
    static final int LINGER = 65546;
    public static void main(String args[]) throws Exception {
        int value;
        InetAddress addr = InetAddress.getLocalHost();
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        Socket s = new Socket(addr, port);
        Socket soc = ss.accept();
        soc.setSoLinger(true, LINGER);
        value = soc.getSoLinger();
        soc.close();
        s.close();
        ss.close();
        if(value != 65535)
            throw new RuntimeException("Failed. Value not properly reduced.");
    }
}
