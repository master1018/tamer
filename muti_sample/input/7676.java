public class IndexTest {
    public static void main(String[] args) throws Exception {
        Enumeration<NetworkInterface> netifs = NetworkInterface.getNetworkInterfaces();
        NetworkInterface nif = null;
        while (netifs.hasMoreElements()) {
            nif = netifs.nextElement();
            int index = nif.getIndex();
            if (index >= 0) {
                NetworkInterface nif2 = NetworkInterface.getByIndex(index);
                if (! nif.equals(nif2)) {
                    throw new RuntimeException("both interfaces should be equal");
                }
            }
        }
        try {
            nif = NetworkInterface.getByIndex(-1);
            throw new RuntimeException("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        nif = NetworkInterface.getByIndex(Integer.MAX_VALUE - 1);
        if (nif != null) {
            throw new RuntimeException("getByIndex() should have returned null");
        }
    }
}
