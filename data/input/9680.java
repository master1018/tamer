public class JITDebug {
    public static void main(String[] args) {
        if (!new JITDebug().parseArgs(args)) {
            throw new RuntimeException("Unexpected command line arguments: "
                                       + args);
        }
    }
    boolean parseArgs(String[] args) {
        switch (args.length) {
        case 0:
            testLaunch();
            return true;
        case 1:
            if (args[0].equals("TARGET")) {
                debugTarget();
                return true;
            } else {
                return false;
            }
        case 3:
            if (args[0].equals("DEBUGGER")) {
                trivialDebugger(args[2]);
                return true;
            } else {
                return false;
            }
        default:
            return false;
        }
    }
    void testLaunch() {
        class DisplayOutput extends Thread {
            InputStream in;
            DisplayOutput(InputStream in) {
                this.in = in;
            }
            public void run() {
                try {
                    transfer();
                } catch (IOException exc) {
                    new RuntimeException("Unexpected exception: " + exc);
                }
            }
            void transfer() throws IOException {
                int ch;
                while ((ch = in.read()) != -1) {
                    System.out.print((char)ch);
                }
                in.close();
            }
        }
        String transportMethod = System.getProperty("TRANSPORT_METHOD");
        if (transportMethod == null) {
            transportMethod = "dt_socket"; 
        }
        String javaExe = System.getProperty("java.home") +
                         File.separator + "bin" + File.separator +"java";
        List largs = new ArrayList();
        largs.add(javaExe);
        largs.add("-agentlib:jdwp=transport=" + transportMethod + ",server=y,onuncaught=y," +
                  "launch=" +
                  javaExe + " -DTRANSPORT_METHOD=" + transportMethod + " " +
                  this.getClass().getName() + " DEBUGGER ");
        largs.add("JITDebug");
        largs.add("TARGET");
        System.out.println("Launching: " + largs);
        String[] sargs = (String[])largs.toArray(new String[largs.size()]);
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(VMConnection.insertDebuggeeVMOptions(sargs));
            DisplayOutput inThread = new DisplayOutput(proc.getInputStream());
            DisplayOutput erThread = new DisplayOutput(proc.getErrorStream());
            inThread.start();  
            erThread.start();
            inThread.join();  
            erThread.join();
            int exitValue = proc.waitFor();
            if (exitValue != 0) {
                throw new RuntimeException("Failure exit status: " +
                                           exitValue);
            }
        } catch (Exception exc) {
            throw new RuntimeException("Unexpected exception: " + exc);
        }
        System.out.println("JIT Debugging test PASSED");
    }
    void displayOutput(InputStream in) throws IOException {
    }
    void debugTarget() {
        System.out.flush();
        System.out.println("trigger onuncaught launch");
        System.out.flush();
        throw new RuntimeException("Start-up onuncaught handling");
    }
    void trivialDebugger(String transportAddress) {
        System.out.println("trivial debugger started");
        String transportMethod = System.getProperty("TRANSPORT_METHOD");
        String connectorName = null;
        if ("dt_shmem".equals(transportMethod)) {
            connectorName = "com.sun.jdi.SharedMemoryAttach";
        } else if ("dt_socket".equals(transportMethod)) {
            connectorName = "com.sun.jdi.SocketAttach";
        } else {
            System.err.flush();
            System.err.println("Unknown transportMethod: " + transportMethod + " - hanging");
            System.err.flush();
            hang();
        }
        List conns = Bootstrap.virtualMachineManager().attachingConnectors();
        for (Iterator it = conns.iterator(); it.hasNext(); ) {
            AttachingConnector conn = (AttachingConnector)it.next();
            if (conn.name().equals(connectorName)) {
                doAttach(connectorName, conn, transportAddress);
                return;
            }
        }
        System.err.flush();
        System.err.println("No attaching connector matching: " + connectorName + " - hanging");
        System.err.flush();
        hang();
    }
    void doAttach(String connectorName, AttachingConnector conn, String transportAddress) {
        Map connArgs = conn.defaultArguments();
        if ("com.sun.jdi.SharedMemoryAttach".equals(connectorName)) {
            Connector.Argument portArg = (Connector.Argument)connArgs.get("name");
            portArg.setValue(transportAddress);
        } else {
            Connector.Argument portArg = (Connector.Argument)connArgs.get("port");
            portArg.setValue(transportAddress);
        }
        try {
            VirtualMachine vm = conn.attach(connArgs);
            System.out.println("attached to: " + transportAddress);
            vm.exit(0); 
            System.out.println("we are happy - terminated VM with no error");
        } catch (Exception exc) {
            System.err.flush();
            System.err.println("Exception: " + exc + " - hanging");
            System.err.flush();
            hang();
        }
    }
    void hang() {
        try {
            Thread.currentThread().sleep(10 * 60 * 1000);
        } catch (InterruptedException exc) {
        }
    }
}
