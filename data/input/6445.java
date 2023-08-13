public class AcceptTimeout {
    public static void main(String args[]) throws Exception {
        List<ListeningConnector> connectors = Bootstrap.virtualMachineManager().listeningConnectors();
        for (ListeningConnector lc: connectors) {
            Map<String,Connector.Argument> cargs = lc.defaultArguments();
            Connector.IntegerArgument timeout = (Connector.IntegerArgument)cargs.get("timeout");
            if (timeout != null) {
                System.out.println("Testing " + lc.name());
                timeout.setValue(1000);
                System.out.println("Listening on: " + lc.startListening(cargs));
                try {
                    lc.accept(cargs);
                    throw new RuntimeException("Connection accepted from some debuggee - unexpected!");
                } catch (TransportTimeoutException e) {
                    System.out.println("Timed out as expected.\n");
                }
                lc.stopListening(cargs);
            }
        }
    }
}
