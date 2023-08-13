public class ShutdownSimpleApplication {
    public static void main(String args[]) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("Usage: ShutdownSimpleApplication" +
                " port-file");
        }
        File f = new File(args[0]);
        FileInputStream fis = new FileInputStream(f);
        byte b[] = new byte[8];
        int n = fis.read(b);
        if (n < 1) {
            throw new RuntimeException("Empty port-file");
        }
        fis.close();
        String str = new String(b, 0, n, "UTF-8");
        System.out.println("INFO: Port number of SimpleApplication: " + str);
        int port = Integer.parseInt(str);
        System.out.println("INFO: Connecting to port " + port +
            " to shutdown SimpleApplication ...");
        System.out.flush();
        Socket s = new Socket();
        s.connect( new InetSocketAddress(port) );
        s.close();
        System.out.println("INFO: done connecting to SimpleApplication.");
        System.out.flush();
        System.exit(0);
    }
}
