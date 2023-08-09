public class DoubleAgentTest {
    static Object locker = new Object();
    static String outputText = "";
    static class IOHandler implements Runnable {
        InputStream in;
        IOHandler(InputStream in) {
            this.in = in;
        }
        static Thread handle(InputStream in) {
            IOHandler handler = new IOHandler(in);
            Thread thr = new Thread(handler);
            thr.setDaemon(true);
            thr.start();
            return thr;
        }
        public void run() {
            try {
                byte b[] = new byte[100];
                for (;;) {
                    int n = in.read(b, 0, 100);
                    synchronized(locker) {
                        locker.notify();
                    }
                    if (n < 0) {
                        break;
                    }
                    String s = new String(b, 0, n, "UTF-8");
                    System.out.print(s);
                    synchronized(outputText) {
                        outputText += s;
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    private static Process launch(String address, String class_name) throws IOException {
        String exe =   System.getProperty("java.home")
                     + File.separator + "bin" + File.separator;
        String arch = System.getProperty("os.arch");
        String osname = System.getProperty("os.name");
        if (osname.equals("SunOS") && arch.equals("sparcv9")) {
            exe += "sparcv9/java";
        } else if (osname.equals("SunOS") && arch.equals("amd64")) {
            exe += "amd64/java";
        } else {
            exe += "java";
        }
        String jdwpOption = "-agentlib:jdwp=transport=dt_socket"
                         + ",server=y" + ",suspend=y" + ",address=" + address;
        String cmd = exe + " " + VMConnection.getDebuggeeVMOptions()
                         + " " + jdwpOption
                         + " " + jdwpOption
                         + " " + class_name;
        System.out.println("Starting: " + cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        return p;
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();
        String address = String.valueOf(port);
        Process process = launch(address, "Exit0");
        Thread t1 = IOHandler.handle(process.getInputStream());
        Thread t2 = IOHandler.handle(process.getErrorStream());
        synchronized(locker) {
            locker.wait();
        }
        int exitCode = process.waitFor();
        try {
            t1.join();
            t2.join();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
            throw new Exception("Debuggee failed InterruptedException");
        }
        if ( outputText.contains("capabilities") ) {
            throw new Exception(
                "Debuggee failed with ERROR about capabilities: " + outputText);
        }
        if ( !outputText.contains("ERROR") ) {
            throw new Exception(
                "Debuggee does not have ERROR in the output: " + outputText);
        }
        if ( exitCode == 0 ) {
            throw new Exception(
                "Debuggee should have failed with an non-zero exit code");
        }
    }
}
