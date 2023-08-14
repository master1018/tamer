public class Support_PortManager {
    private static int lastAssignedPort = somewhatRandomPort();
    private static boolean failedOnce = false;
    public static synchronized int getNextPort() {
        if (!failedOnce) {
            try {
                ServerSocket ss = new ServerSocket(0);
                int port = ss.getLocalPort();
                ss.close();
                return port;
            } catch (Exception ex) {
                failedOnce = true;
            }
        }
        return getNextPort_unsafe();
    }
    public static synchronized int getNextPortForUDP() {
        return getNextPortsForUDP(1)[0];
    }
    public static synchronized int[] getNextPortsForUDP(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("Invalid ports number: " + num);
        }
        DatagramSocket[] dss = new DatagramSocket[num];
        int[] ports = new int[num];
        try {
            for (int i = 0; i < num; ++i) {
                dss[i] = new DatagramSocket(0);
                ports[i] = dss[i].getLocalPort();
            }
        } catch (Exception ex) {
            throw new Error("Unable to get " + num + " ports for UDP: " + ex);
        } finally {
            for (int i = 0; i < num; ++i) {
                if (dss[i] != null) {
                    dss[i].close();
                }
            }
        }
        return ports;
    }
    public static synchronized int getNextPort_unsafe() {
        if (++lastAssignedPort > 65534) {
            lastAssignedPort = 6000;
        }
        return lastAssignedPort;
    }
    private static int somewhatRandomPort() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);
        return 6000 + (1000 * minutes) + ((seconds / 6) * 100);
    }
}
