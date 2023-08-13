public class CheckFQDN extends UnicastRemoteObject
    implements TellServerName {
    static String propertyBeingTested = null;
    static String propertyBeingTestedValue = null;
    public static void main(String args[]) {
        Object dummy = new Object();
        CheckFQDN checkFQDN = null;
        try {
            checkFQDN = new CheckFQDN();
            System.err.println
                ("\nRegression test for bug/rfe 4115683\n");
            Registry registry = java.rmi.registry.LocateRegistry.
                createRegistry(TestLibrary.REGISTRY_PORT);
            registry.bind("CheckFQDN", checkFQDN);
            testProperty("java.rmi.server.useLocalHostname", "true", "");
            testProperty("java.rmi.server.hostname", "thisIsJustAnRMITest", "");
            testProperty("java.rmi.server.hostname", "thisIsJustAnRMITest",
                         " -Djava.rmi.server.useLocalHostname=true ");
            testProperty("", "", "");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            if (checkFQDN != null) {
                TestLibrary.unexport(checkFQDN);
            }
        }
        System.err.println("\nTest for bug/ref 4115683 passed.\n");
    }
    public static void testProperty(String property,
                                    String propertyValue,
                                    String extraProp)
    {
        try {
            String propOption = "";
            String equal = "";
            if (!property.equals("")) {
                propOption = " -D";
                equal = "=";
            }
            JavaVM jvm = new JavaVM("CheckFQDNClient",
                                    propOption + property +
                                    equal +
                                    propertyValue + extraProp,
                                    "");
            propertyBeingTested=property;
            propertyBeingTestedValue=propertyValue;
            jvm.start();
            if (jvm.getVM().waitFor() != 0 ) {
                TestLibrary.bomb("Test failed, error in client.");
            }
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
    }
    CheckFQDN() throws RemoteException { }
    public void tellServerName(String serverName)
        throws RemoteException {
        if (propertyBeingTested.equals("java.rmi.server.hostname")) {
            if ( !propertyBeingTestedValue.equals(serverName)) {
                TestLibrary.bomb(propertyBeingTested +
                     ":\n Client rmi server name does " +
                     "not equal the one specified " +
                     "by java.rmi.server.hostname: " +
                     serverName +" != " +
                     propertyBeingTestedValue);
            }
        } else if (propertyBeingTested.equals
                   ("java.rmi.server.useLocalHostname")) {
            if (serverName.indexOf('.') < 0) {
                TestLibrary.bomb(propertyBeingTested +
                     ":\nThe client servername contains no '.'");
            }
        } else {
            if ((serverName.indexOf('.') < 0) ||
                (!Character.isDigit(serverName.charAt(0)))) {
                TestLibrary.bomb("Default name scheme:\n"+
                     " The client servername contains no '.'"+
                     "or is not an ip address");
            }
        }
        System.err.println("Servername used: " + serverName);
    }
}
