public class ProcessAttachingConnector
        extends ConnectorImpl implements AttachingConnector
{
    static final String ARG_PID = "pid";
    static final String ARG_TIMEOUT = "timeout";
    com.sun.tools.attach.VirtualMachine vm;
    Transport transport;
    public ProcessAttachingConnector() {
        addStringArgument(
            ARG_PID,
            getString("process_attaching.pid.label"),
            getString("process_attaching.pid"),
            "",
            true);
        addIntegerArgument(
            ARG_TIMEOUT,
            getString("generic_attaching.timeout.label"),       
            getString("generic_attaching.timeout"),             
            "",
            false,
            0, Integer.MAX_VALUE);
        transport = new Transport() {
            public String name() {
                return "local";
            }
        };
    }
    public VirtualMachine attach(Map<String,? extends Connector.Argument> args)
                throws IOException, IllegalConnectorArgumentsException
    {
        String pid = argument(ARG_PID, args).value();
        String t = argument(ARG_TIMEOUT, args).value();
        int timeout = 0;
        if (t.length() > 0) {
            timeout = Integer.decode(t).intValue();
        }
        String address = null;
        com.sun.tools.attach.VirtualMachine vm = null;
        try {
            vm = com.sun.tools.attach.VirtualMachine.attach(pid);
            Properties props = vm.getAgentProperties();
            address = props.getProperty("sun.jdwp.listenerAddress");
        } catch (Exception x) {
            throw new IOException(x.getMessage());
        } finally {
            if (vm != null) vm.detach();
        }
        if (address == null) {
            throw new IOException("Not a debuggee, or not listening for debugger to attach");
        }
        int pos = address.indexOf(':');
        if (pos < 1) {
            throw new IOException("Unable to determine transport endpoint");
        }
        final String lib = address.substring(0, pos);
        address = address.substring(pos+1, address.length());
        TransportService ts = null;
        if (lib.equals("dt_socket")) {
            ts = new SocketTransportService();
        } else {
            if (lib.equals("dt_shmem")) {
                try {
                    Class c = Class.forName("com.sun.tools.jdi.SharedMemoryTransportService");
                    ts = (TransportService)c.newInstance();
                } catch (Exception x) { }
            }
        }
        if (ts == null) {
            throw new IOException("Transport " + lib + " not recognized");
        }
        Connection connection = ts.attach(address, timeout, 0);
        return Bootstrap.virtualMachineManager().createVirtualMachine(connection);
    }
    public String name() {
        return "com.sun.jdi.ProcessAttach";
    }
    public String description() {
        return getString("process_attaching.description");
    }
    public Transport transport() {
        if (transport == null) {
            return new Transport() {
                public String name() {
                    return "local";
                }
            };
        }
        return transport;
    }
}
