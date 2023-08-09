public class InheritHandle
{
    static String java = System.getProperty("java.home") + File.separator +
                         "bin" + File.separator + "java";
    public static void main(String[] args) {
        if (args.length == 1) {
            doWait();
        } else {
            startTest();
        }
    }
    static void startTest() {
        ServerSocket ss;
        int port;
        Process process;
        try {
            ss = new ServerSocket(0);
            port = ss.getLocalPort();
            System.out.println("First ServerSocket listening on port " + port);
        } catch (IOException e) {
            System.out.println("Cannot create ServerSocket");
            e.printStackTrace();
            return;
        }
        try {
            process = Runtime.getRuntime().exec(java + " InheritHandle -doWait");
        } catch (IOException ioe) {
            System.out.println("Cannot create process");
            ioe.printStackTrace();
            return;
        }
        try {
            System.out.println("waiting...");
            Thread.sleep(2 * 1000);
            System.out.println("Now close the socket and try to create another" +
                               " one listening on the same port");
            ss.close();
            ss = new ServerSocket(port);
            System.out.println("Second ServerSocket created successfully");
            ss.close();
        } catch (InterruptedException ie) {
        } catch (IOException ioe) {
            throw new RuntimeException("Failed: " + ioe);
        } finally {
            process.destroy();
        }
        System.out.println("OK");
    }
    static void doWait() {
        try {
            Thread.sleep(200 * 1000);
        } catch (InterruptedException ie) {
        }
    }
}
