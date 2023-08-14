public class BlockAcceptTest
{
    public static void main(String[] args)
        throws Exception
    {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.err.println("(installing HTTP-out socket factory)");
        HttpOutFactory fac = new HttpOutFactory();
        RMISocketFactory.setSocketFactory(fac);
        TestImpl impl = new TestImpl();
        System.err.println("(exporting remote object)");
        TestIface stub = impl.export();
        try {
            int port = fac.whichPort();
            if (port == 0)
                throw new Error("TEST FAILED: export didn't reserve a port(?)");
            System.setProperty("http.proxyPort", port+"");
            System.err.println("(connecting to listening port on 127.0.0.1:" +
                               port + ")");
            Socket DoS = new Socket("127.0.0.1", port);
            System.err.println("(making RMI-through-HTTP call)");
            System.err.println("(typical test failure deadlocks here)");
            String result = stub.testCall("dummy load");
            System.err.println(" => " + result);
            if (!("OK".equals(result)))
                throw new Error("TEST FAILED: result not OK");
            System.err.println("Test passed.");
            try {
                DoS.getOutputStream().write(0);
                DoS.getOutputStream().close();
            } catch (Throwable apathy) {
            }
        } finally {
            try {
                impl.unexport();
            } catch (Throwable unmatter) {
            }
        }
    }
    private static class HttpOutFactory
        extends RMISocketFactory
    {
        private int servport = 0;
        public Socket createSocket(String h, int p)
            throws IOException
        {
            return ((new RMIHttpToPortSocketFactory()).createSocket(h, p));
        }
        public ServerSocket createServerSocket(int p)
            throws IOException
        {
            ServerSocket ss;
            ss = (new RMIMasterSocketFactory()).createServerSocket(p);
            if (p == 0) {
                if (servport != 0) {
                    System.err.println("TEST FAILED: " +
                                       "Duplicate createServerSocket(0)");
                    throw new Error("Test aborted (createServerSocket)");
                }
                servport = ss.getLocalPort();
            }
            return (ss);
        }
        public int whichPort() {
            return (servport);
        }
    } 
}
