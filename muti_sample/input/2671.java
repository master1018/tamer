public class LookupIPv6 {
    public static void main(String[] args) throws Exception {
        String[] urls = {
            "rmi:
            "
            "rmi:
            "
        };
        for (int i = 0; i < urls.length; i++) {
            try {
                Naming.lookup(urls[i]);
            } catch (MalformedURLException ex) {
                throw ex;
            } catch (Exception ex) {
            }
        }
        InetAddress localAddr = InetAddress.getAllByName(null)[0];
        if (localAddr instanceof Inet6Address) {
            System.out.println("IPv6 detected");
            Registry reg;
            try {
                reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            } catch (Exception ex) {
                reg = LocateRegistry.getRegistry();
            }
            reg.rebind("foo", reg);
            Naming.lookup("rmi:
        }
    }
}
