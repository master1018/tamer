public class BadHandshakeTest {
    static volatile boolean ready = false;
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
                    int n = in.read(b, 0, 100);
                    ready = true;
                    if (n < 0) {
                        break;
                    }
                    String s = new String(b, 0, n, "UTF-8");
                    System.out.print(s);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    private static Connector findConnector(String name) {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
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
        String cmd = exe + " " + VMConnection.getDebuggeeVMOptions() +
            " -agentlib:jdwp=transport=dt_socket" +
            ",server=y" + ",suspend=y" + ",address=" + address +
            " " + class_name;
        System.out.println("Starting: " + cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        IOHandler.handle(p.getInputStream());
        IOHandler.handle(p.getErrorStream());
        return p;
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();
        String address = String.valueOf(port);
        Process process = launch(address, "Exit0");
        while (!ready) {
            try {
                Thread.sleep(1000);
            } catch(Exception ee) {
                throw ee;
            }
        }
        Socket s = new Socket(InetAddress.getLocalHost(), port);
        s.getOutputStream().write("Here's a poke in the eye".getBytes("UTF-8"));
        s.close();
        s = new Socket(InetAddress.getLocalHost(), port);
        s.getOutputStream().write("JDWP-".getBytes("UTF-8"));
        AttachingConnector conn = (AttachingConnector)findConnector("com.sun.jdi.SocketAttach");
        Map conn_args = conn.defaultArguments();
        Connector.IntegerArgument port_arg =
            (Connector.IntegerArgument)conn_args.get("port");
        port_arg.setValue(port);
        VirtualMachine vm = conn.attach(conn_args);
        EventSet evtSet = vm.eventQueue().remove();
        for (Event event: evtSet) {
            if (event instanceof VMStartEvent) {
                break;
            }
            throw new RuntimeException("Test failed - debuggee did not start properly");
        }
        vm.eventRequestManager().deleteAllBreakpoints();
        vm.resume();
        process.waitFor();
    }
}
