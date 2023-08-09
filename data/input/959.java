public class IdempotentActiveGroup {
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4720528\n");
        TestLibrary.suggestSecurityManager("java.lang.SecurityManager");
        RMID rmid = null;
        ActivationInstantiator inst1 = null;
        ActivationInstantiator inst2 = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            System.err.println("Create group descriptor");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(null, null);
            ActivationSystem system = ActivationGroup.getSystem();
            System.err.println("Register group descriptor");
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            inst1 = new FakeInstantiator();
            inst2 = new FakeInstantiator();
            System.err.println("Invoke activeGroup with inst1");
            system.activeGroup(groupID, inst1, 0);
            try {
            System.err.println("Invoke activeGroup with inst2");
                system.activeGroup(groupID, inst2, 0);
                throw new RuntimeException(
                    "TEST FAILED: activeGroup with unequal groups succeeded!");
            } catch (ActivationException expected) {
                System.err.println("Caught expected ActivationException");
                System.err.println("Test 1 (of 2) passed");
            }
            try {
                System.err.println("Invoke activeGroup with inst1");
                system.activeGroup(groupID, inst1, 0);
                System.err.println("activeGroup call succeeded");
                System.err.println("Test 2 (of 2) passed");
            } catch (ActivationException unexpected) {
                throw new RuntimeException(
                    "TEST FAILED: activeGroup with equal groups failed!",
                    unexpected);
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            try {
                if (inst1 != null) {
                    UnicastRemoteObject.unexportObject(inst1, true);
                }
                if (inst2 != null) {
                    UnicastRemoteObject.unexportObject(inst2, true);
                }
            } catch (NoSuchObjectException unexpected) {
                throw new AssertionError(unexpected);
            }
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
    private static class FakeInstantiator
        extends UnicastRemoteObject
        implements ActivationInstantiator
    {
        FakeInstantiator() throws RemoteException {}
        public MarshalledObject newInstance(ActivationID id,
                                            ActivationDesc desc)
        {
            throw new AssertionError();
        }
    }
}
