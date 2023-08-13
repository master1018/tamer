public class LookupActivationSystem implements Remote, Serializable {
    private static final String NAME = ActivationSystem.class.getName();
    public static void main(String[] args) throws Exception {
        System.out.println("\nRegression test for bug 6245733\n");
        RMID rmid = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            System.err.println("look up activation system");
            Registry rmidRegistry =
                LocateRegistry.getRegistry(ActivationSystem.SYSTEM_PORT);
            ActivationSystem system = (ActivationSystem)
                rmidRegistry.lookup(NAME);
            if (system instanceof ActivationSystem) {
                System.err.println("test1 passed: lookup succeeded");
            }
            System.err.println("get list of rmid internal registry");
            String[] list = rmidRegistry.list();
            if (list.length == 1 && list[0].equals(NAME)) {
                System.err.println(
                    "test2 passed: activation system found in list");
            } else {
                throw new RuntimeException("test2 FAILED");
            }
            try {
                rmidRegistry.bind(NAME, new LookupActivationSystem());
                throw new RuntimeException("test3 FAILED: bind succeeded!");
            } catch (ServerException e) {
                if (e.getCause() instanceof AccessException) {
                    System.err.println(
                        "test3 passed: binding ActivationSystem " +
                        "failed as expected");
                } else {
                    throw new RuntimeException(
                        "test3 FAILED: incorrect cause: " + e.getCause());
                }
            }
            try {
                rmidRegistry.rebind(NAME, new LookupActivationSystem());
                throw new RuntimeException("test4 FAILED: rebind succeeded!");
            } catch (ServerException e) {
                if (e.getCause() instanceof AccessException) {
                    System.err.println(
                        "test4 passed: rebinding ActivationSystem " +
                        "failed as expected");
                } else {
                    throw new RuntimeException(
                        "test4 FAILED: incorrect cause: " + e.getCause());
                }
            }
            try {
                rmidRegistry.unbind(NAME);
                throw new RuntimeException("test4 FAILED: unbind succeeded!");
            } catch (ServerException e) {
                if (e.getCause() instanceof AccessException) {
                    System.err.println(
                        "test5 passed: unbinding ActivationSystem " +
                        "failed as expected");
                } else {
                    throw new RuntimeException(
                        "test5 FAILED: incorrect cause: " + e.getCause());
                }
            }
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
