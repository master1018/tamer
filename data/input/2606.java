public class UnderscoreHost extends UnicastRemoteObject implements Remote {
    private static final String HOSTNAME = "foo_bar";
    private static final String NAME = "name";
    private static class HostVerifyingSocketFactory extends RMISocketFactory {
        String host;
        public synchronized Socket createSocket(String host, int port)
            throws IOException  {
            if (this.host == null) {
                this.host = host;
            }
            return new Socket("localhost", port);
        }
        public ServerSocket createServerSocket(int port) throws IOException {
            return new ServerSocket(port);
        }
    }
    public UnderscoreHost() throws RemoteException {};
    public static void main(String args[]) {
        UnderscoreHost t = null;
        try {
            HostVerifyingSocketFactory hvf = new HostVerifyingSocketFactory();
            RMISocketFactory.setSocketFactory(hvf);
            Registry r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            t = new UnderscoreHost();
            r.rebind(NAME, t);
            Naming.lookup("rmi:
                          ":" + Registry.REGISTRY_PORT + "/" + NAME);
            if (!hvf.host.equals(HOSTNAME)) {
                throw new RuntimeException(
                    "java.rmi.Naming Parsing error:" +
                    hvf.host + ":" + HOSTNAME);
            }
        } catch (MalformedURLException e) {
        } catch (IOException ioe) {
            TestLibrary.bomb(ioe);
        } catch (java.rmi.NotBoundException nbe) {
            TestLibrary.bomb(nbe);
        } finally {
            TestLibrary.unexport(t);
        }
    }
}
