public class SocketListeningConnector extends GenericListeningConnector {
    static final String ARG_PORT = "port";
    static final String ARG_LOCALADDR = "localAddress";
    public SocketListeningConnector() {
        super(new SocketTransportService());
        addIntegerArgument(
            ARG_PORT,
            getString("socket_listening.port.label"),
            getString("socket_listening.port"),
            "",
            false,
            0, Integer.MAX_VALUE);
        addStringArgument(
            ARG_LOCALADDR,
            getString("socket_listening.localaddr.label"),
            getString("socket_listening.localaddr"),
            "",                                         
            false);
        transport = new Transport() {
            public String name() {
                return "dt_socket";     
            }
        };
    }
    public String
        startListening(Map<String,? extends Connector.Argument> args)
        throws IOException, IllegalConnectorArgumentsException
    {
        String port = argument(ARG_PORT, args).value();
        String localaddr = argument(ARG_LOCALADDR, args).value();
        if (port.length() == 0) {
            port = "0";
        }
        if (localaddr.length() > 0) {
           localaddr = localaddr + ":" + port;
        } else {
           localaddr = port;
        }
        return super.startListening(localaddr, args);
    }
    public String name() {
        return "com.sun.jdi.SocketListen";
    }
    public String description() {
        return getString("socket_listening.description");
    }
}
