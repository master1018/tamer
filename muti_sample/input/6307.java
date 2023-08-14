public class SharedMemoryListeningConnector extends GenericListeningConnector {
    static final String ARG_NAME = "name";
    public SharedMemoryListeningConnector() {
        super(new SharedMemoryTransportService());
        addStringArgument(
            ARG_NAME,
            getString("memory_listening.name.label"),
            getString("memory_listening.name"),
            "",
            false);
        transport = new Transport() {
            public String name() {
                return "dt_shmem";              
            }
        };
    }
    public String
        startListening(Map<String, ? extends Connector.Argument> args)
        throws IOException, IllegalConnectorArgumentsException
    {
        String name = argument(ARG_NAME, args).value();
        if (name.length() == 0) {
            assert transportService instanceof SharedMemoryTransportService;
            SharedMemoryTransportService ts = (SharedMemoryTransportService)transportService;
            name = ts.defaultAddress();
        }
        return super.startListening(name, args);
    }
    public String name() {
        return "com.sun.jdi.SharedMemoryListen";
    }
    public String description() {
       return getString("memory_listening.description");
    }
}
