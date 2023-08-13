public class IiopUrlIPv6 {
    public static void main(String[] args) {
        String[] urls = {"iiop:
                        "iiop:
                        "iiop:
                        "iiop:
                      };
        for (int u = 0; u < urls.length; u++) {
            try {
                IiopUrl url = new IiopUrl(urls[u]);
                Vector addrs = url.getAddresses();
                for (int i = 0; i < addrs.size(); i++) {
                    Address addr = (Address)addrs.elementAt(i);
                    System.out.println("================");
                    System.out.println("url: " + urls[u]);
                    System.out.println("host: " + addr.host);
                    System.out.println("port: " + addr.port);
                    System.out.println("version: " + addr.major
                                + " " + addr.minor);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
