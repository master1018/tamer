public class SelfTerminator {
    public static void main(String[] args) {
        try {
            Registry registry =
                LocateRegistry.getRegistry("", TestLibrary.REGISTRY_PORT);
            Remote stub = registry.lookup(LeaseCheckInterval.BINDING);
            Runtime.getRuntime().halt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
