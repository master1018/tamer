public class ShutdownDebuggee {
    public static void main(String args[]) throws Exception {
        File f = new File(args[0]);
        FileInputStream fis = new FileInputStream(f);
        byte b[] = new byte[8];
        int n = fis.read(b);
        if (n < 1) {
            throw new RuntimeException("Empty file");
        }
        fis.close();
        String str = new String(b, 0, n, "UTF-8");
        System.out.println("Port number of debuggee is: " + str);
        int port = Integer.parseInt(str);
        System.out.println("Connecting to port " + port +
            " to shutdown Debuggee ...");
        Socket s = new Socket();
        s.connect( new InetSocketAddress(port) );
        s.close();
    }
}
