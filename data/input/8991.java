public class CheckFQDNClient implements Runnable {
    final static int NAME_SERVICE_TIME_OUT = 12000;
    static TCPEndpoint ep = null;
    public static void main (String args[]) {
        TellServerName tell;
        String hostname = null;
        try {
            hostname = retrieveServerName();
            System.err.println("Client host name: " +
                               hostname);
            tell = (TellServerName) Naming.lookup("rmi:
                                                  TestLibrary.REGISTRY_PORT
                                                  + "/CheckFQDN");
            tell.tellServerName(hostname);
            System.err.println("client has exited");
        } catch (Exception e ) {
            throw new RuntimeException(e.getMessage());
        }
        System.exit(0);
    }
    public static String retrieveServerName () {
        try {
            CheckFQDNClient chk = new CheckFQDNClient();
            synchronized(chk) {
                (new Thread (chk)).start();
                chk.wait(NAME_SERVICE_TIME_OUT);
            }
            if (ep == null) {
                throw new RuntimeException
                    ("Timeout getting the local endpoint.");
            }
            return ep.getHost();
        }catch (Exception e){
            throw new RuntimeException (e.getMessage());
        }
    }
    public void run () {
        try {
            synchronized(this) {
                ep = TCPEndpoint.getLocalEndpoint(0);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            synchronized(this) {
                this.notify();
            }
        }
    }
}
