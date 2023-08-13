public class ProbeIB {
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(new File(args[0]));
        try {
            while (s.hasNextLine()) {
                String link = s.nextLine();
                NetworkInterface ni = NetworkInterface.getByName(link);
                if (ni != null) {
                    Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        System.out.println(addr.getHostAddress());
                    }
                }
            }
        } finally {
            s.close();
        }
    }
}
