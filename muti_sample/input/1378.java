public class ExclusiveBind {
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
                    for (int i=0; i<n; i++) {
                        System.out.print((char)b[i]);
                    }
                }
            } catch (IOException ioe) { }
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
    private static Process launch(String address, boolean suspend, String class_name) throws IOException {
        String exe = System.getProperty("java.home") + File.separator + "bin" +
            File.separator;
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
            " -agentlib:jdwp=transport=dt_socket,server=y,suspend=";
        if (suspend) {
            cmd += "y";
        } else {
            cmd += "n";
        }
        cmd += ",address=" + address + " " + class_name;
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
        Process process1 = launch(address, true, "HelloWorld");
        Thread.currentThread().sleep(5000);
        Process process2 = launch(address, false, "HelloWorld");
        int exitCode = process2.waitFor();
        AttachingConnector conn = (AttachingConnector)findConnector("com.sun.jdi.SocketAttach");
        Map conn_args = conn.defaultArguments();
        Connector.IntegerArgument port_arg =
            (Connector.IntegerArgument)conn_args.get("port");
        port_arg.setValue(port);
        VirtualMachine vm = conn.attach(conn_args);
        vm.resume();
        if (exitCode == 0) {
            throw new RuntimeException("Test failed - second debuggee didn't fail to bind");
        } else {
            System.out.println("Test passed - second debuggee correctly failed to bind");
        }
    }
}
