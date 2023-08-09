public class TestInterfaces {
    public static void main(String args[]) throws Exception {
        int failures = 0;
        MulticastSocket soc = new MulticastSocket();
        Enumeration nifs = NetworkInterface.getNetworkInterfaces();
        while (nifs.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface)nifs.nextElement();
            Enumeration addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress ia = (InetAddress)addrs.nextElement();
                System.out.println("********************************");
                System.out.println("MulticastSocket.setInterface(" + ia + ")");
                try {
                    soc.setInterface(ia);
                } catch (IOException ioe) {
                    System.err.println("Can't set interface to: " + ia
                        + " " + ioe.getMessage());
                    continue;
                }
                InetAddress curr = soc.getInterface();
                if (!curr.equals(ia)) {
                    System.err.println("MulticastSocket.getInterface returned: " + curr);
                    System.err.println("Failed! Expected: " + ia);
                    failures++;
                } else {
                    System.out.println("Passed.");
                }
            }
            System.out.println("********************************");
            System.out.println("MulticastSocket.setNetworkInterface(" +
                ni.getName() + ")");
            try {
                soc.setNetworkInterface(ni);
            } catch (IOException ioe) {
                System.err.println("Can't set interface to: " + ni.getName()
                        + " " + ioe.getMessage());
                continue;
            }
            NetworkInterface curr = soc.getNetworkInterface();
            if (!curr.equals(ni)) {
                System.err.println("MulticastSocket.getNetworkInterface returned: " + curr);
                System.err.println("Failed! Expected: " + ni);
                failures++;
            } else {
                System.out.println("Passed.");
            }
        }
        if (failures > 0) {
            System.out.println("********************************");
            throw new Exception(failures + " test(s) failed!!!");
        }
    }
}
