public class NullCombinerEquals {
    public static void main(String[] args) throws Exception {
        NullCombinerEquals nce = new NullCombinerEquals();
        try {
            nce.go();
        } catch (Exception e) {
            throw new Exception("Test Failed: " + e.toString());
        }
    }
    void go() throws Exception {
        AccessControlContext acc = AccessController.getContext();
        acc.equals(acc);
        AccessControlContext acc2 = new AccessControlContext(acc, new DC());
        acc.equals(acc2);
        acc2.equals(acc);
        AccessControlContext acc3 = new AccessControlContext(acc, new DC());
        acc2.equals(acc3);
    }
    private static class DC implements DomainCombiner {
        public ProtectionDomain[] combine(ProtectionDomain[] a,
                                        ProtectionDomain[] b) {
            return a;
        }
    }
}
