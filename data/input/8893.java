public class DisableMultiplexing implements Remote {
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4183204\n");
        System.err.println("Setting draconian security manager.");
        System.setSecurityManager(new SecurityManager() {
            public void checkListen(int port) {
                throw new SecurityException("THOU SHALT NOT LISTEN");
            }
        });
        System.err.println("Creating remote object.");
        DisableMultiplexing obj = new DisableMultiplexing();
        try {
            System.err.println("Attempting to export remote object.");
            UnicastRemoteObject.exportObject(obj);
            try {
                UnicastRemoteObject.unexportObject(obj, true);
            } catch (NoSuchObjectException e) {
            }
            throw new RuntimeException(
                "TEST FAILED: remote object successfully exported");
        } catch (SecurityException e) {
            System.err.println("TEST PASSED: ");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
}
