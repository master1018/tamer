public class GenericAttachingConnector
        extends ConnectorImpl implements AttachingConnector
{
    static final String ARG_ADDRESS = "address";
    static final String ARG_TIMEOUT = "timeout";
    TransportService transportService;
    Transport transport;
    private GenericAttachingConnector(TransportService ts,
                                      boolean addAddressArgument)
    {
        transportService = ts;
        transport = new Transport() {
                public String name() {
                    return transportService.name();
                }
            };
        if (addAddressArgument) {
            addStringArgument(
                ARG_ADDRESS,
                getString("generic_attaching.address.label"),
                getString("generic_attaching.address"),
                "",
                true);
        }
        addIntegerArgument(
                ARG_TIMEOUT,
                getString("generic_attaching.timeout.label"),
                getString("generic_attaching.timeout"),
                "",
                false,
                0, Integer.MAX_VALUE);
    }
    protected GenericAttachingConnector(TransportService ts) {
        this(ts, false);
    }
    public static GenericAttachingConnector create(TransportService ts) {
        return new GenericAttachingConnector(ts, true);
    }
    public VirtualMachine attach(String address, Map args)
        throws IOException, IllegalConnectorArgumentsException
    {
        String ts  = argument(ARG_TIMEOUT, args).value();
        int timeout = 0;
        if (ts.length() > 0) {
            timeout = Integer.decode(ts).intValue();
        }
        Connection connection = transportService.attach(address, timeout, 0);
        return Bootstrap.virtualMachineManager().createVirtualMachine(connection);
    }
    public VirtualMachine
        attach(Map<String,? extends Connector.Argument> args)
        throws IOException, IllegalConnectorArgumentsException
    {
        String address = argument(ARG_ADDRESS, args).value();
        return attach(address, args);
    }
    public String name() {
        return transport.name() + "Attach";
    }
    public String description() {
        return transportService.description();
    }
    public Transport transport() {
        return transport;
    }
}
