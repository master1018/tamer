public class SharedMemoryAttachingConnector extends GenericAttachingConnector {
    static final String ARG_NAME = "name";
    public SharedMemoryAttachingConnector() {
        super(new SharedMemoryTransportService());
        addStringArgument(
            ARG_NAME,
            getString("memory_attaching.name.label"),
            getString("memory_attaching.name"),
            "",
            true);
        transport = new Transport() {
            public String name() {
                return "dt_shmem";              
            }
        };
    }
    public VirtualMachine
        attach(Map<String, ? extends Connector.Argument> arguments)
        throws IOException, IllegalConnectorArgumentsException
    {
        String name = argument(ARG_NAME, arguments).value();
        return super.attach(name, arguments);
    }
    public String name() {
        return "com.sun.jdi.SharedMemoryAttach";
    }
    public String description() {
       return getString("memory_attaching.description");
    }
}
