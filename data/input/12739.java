public class LegalRegistryNames extends UnicastRemoteObject
    implements Legal
{
    public LegalRegistryNames() throws java.rmi.RemoteException {}
    public static void main(String args[]) throws RuntimeException {
        System.err.println("\nRegression test for bug/rfe 4254808\n");
        Registry registry = null;
        LegalRegistryNames legal = null;
        boolean oneFormFailed = false;
        String[] names = null;
        Vector legalForms = getLegalForms();
        Remote shouldFind = null;
        try {
            legal = new LegalRegistryNames();
            System.err.println("Starting registry on default port");
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (Exception e) {
            TestLibrary.bomb("registry already running on test port");
        }
        String s = null;
        Enumeration en = legalForms.elements();
        while (en.hasMoreElements()) {
            s = (String) en.nextElement();
            System.err.println("\ntesting form: " + s);
            try {
                Naming.rebind(s, legal);
                names = registry.list();
                if ((names.length > 0) &&
                    (names[0].compareTo("MyName") != 0))
                {
                    oneFormFailed = true;
                    System.err.println("\tRegistry entry for form: " +
                                       s + " is incorrect: " + names[0]);
                }
                shouldFind = Naming.lookup(s);
                Naming.unbind(s);
                System.err.println("\tform " + s + " OK");
            } catch (Exception e) {
                e.printStackTrace();
                oneFormFailed = true;
                System.err.println("\tunexpected lookup or unbind " +
                                   "exception for form: " + s + e.getMessage() );
            }
        }
        if (oneFormFailed) {
            TestLibrary.bomb("Test failed");
        }
        TestLibrary.unexport(legal);
    }
    private static Vector getLegalForms() {
        String localHostAddress = null;
        String localHostName = null;
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
            localHostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException e) {
            TestLibrary.bomb("Test failed: unexpected exception", e);
        }
        Vector legalForms = new Vector();
        legalForms.add("
        legalForms.add("
        legalForms.add("
        legalForms.add("
                       Registry.REGISTRY_PORT + "/MyName");
        legalForms.add("
        legalForms.add("
        legalForms.add("
        legalForms.add("
                       "/MyName");
        legalForms.add("MyName");
        legalForms.add("/MyName");
        legalForms.add("rmi:
        legalForms.add("rmi:
        legalForms.add("rmi:
        legalForms.add("rmi:
                       Registry.REGISTRY_PORT + "/MyName");
        legalForms.add("rmi:
        legalForms.add("rmi:
        legalForms.add("rmi:
        legalForms.add("rmi:
                       Registry.REGISTRY_PORT + "/MyName");
        return legalForms;
    }
}
