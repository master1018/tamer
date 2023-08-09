public class SimpleNameService implements NameService {
    private HashMap<String, String> hosts = new LinkedHashMap<String, String>();
    public void put(String host, String addr) {
        hosts.put(host, addr);
    }
    private static String addrToString(byte addr[]) {
        return Byte.toString(addr[0]) + "." +
               Byte.toString(addr[1]) + "." +
               Byte.toString(addr[2]) + "." +
               Byte.toString(addr[3]);
    }
    public SimpleNameService() {
    }
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        String addr = hosts.get(host);
        if (addr == null) {
            throw new UnknownHostException(host);
        }
        StringTokenizer tokenizer = new StringTokenizer(addr, ".");
        byte addrs[] = new byte[4];
        for (int i = 0; i < 4; i++) {
            addrs[i] = (byte)Integer.parseInt(tokenizer.nextToken());
        }
        InetAddress[] ret = new InetAddress[1];
        ret[0] = InetAddress.getByAddress(host, addrs);
        return ret;
    }
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        String addrString = addrToString(addr);
        Iterator i = hosts.keySet().iterator();
        while (i.hasNext()) {
            String host = (String)i.next();
            String value = (String)hosts.get(host);
            if (value.equals(addrString)) {
                return host;
            }
        }
        throw new UnknownHostException();
    }
}
