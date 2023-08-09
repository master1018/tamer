public class ImplVersionCommand {
    public static void main(String[] args) throws Exception {
        System.out.println("Create RMIJRMPServerImpl");
        RMIServer server = new RMIJRMPServerImpl(0, null, null, null);
        System.out.println("Get JMX Remote implementation version from RMIServer");
        String full_version = server.getVersion();
        System.out.println("RMIServer.getVersion() = "+ full_version);
        String impl_version = full_version.substring(
            full_version.indexOf("java_runtime_")+"java_runtime_".length());
        System.out.println("JMX Remote implementation version   = " +
                           impl_version);
        System.out.println("Java Runtime implementation version = " +
                           args[0]);
        if (!impl_version.equals(args[0])) {
            throw new IllegalArgumentException(
                "***FAILED: JMX Remote and Java Runtime versions do NOT match***");
        }
        System.out.println("JMX Remote and Java Runtime versions match.");
        System.out.println("Bye! Bye!");
    }
}
