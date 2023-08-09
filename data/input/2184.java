public class GeneratedConnectors {
    static Connector find(List l, String name) {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Connector c = (Connector)i.next();
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }
    public static void main(String args[]) throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        if (vmm.getClass().getClassLoader() == null) {
            System.out.println("JDI on bootclasspath - test skipped");
            return;
        }
        List connectors = vmm.allConnectors();
        AttachingConnector attacher =
            (AttachingConnector)find(connectors, "NullAttach");
        ListeningConnector listener =
            (ListeningConnector)find(connectors, "NullListen");
        if (attacher == null || listener == null) {
            throw new RuntimeException("One, or both, generated connectors are missing");
        }
        Connector.StringArgument arg;
        arg = (Connector.StringArgument)attacher.defaultArguments().get("address");
        arg = (Connector.StringArgument)listener.defaultArguments().get("address");
    }
}
