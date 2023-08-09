public class B6296240 {
    public static void main(String[] args) {
        String[] malformedIPv4s = {"192.168.1.220..."};
        BitSet expectedExceptions = new BitSet(malformedIPv4s.length);
        expectedExceptions.clear();
        for (int i = 0; i < malformedIPv4s.length; i++) {
            try {
                InetAddress.getAllByName(malformedIPv4s[i]);
            } catch (UnknownHostException e) {
                expectedExceptions.set(i);
            }
        }
        for (int i = 0; i < malformedIPv4s.length; i++) {
            if (!expectedExceptions.get(i)) {
                System.out.println("getAllByName(\"" + malformedIPv4s[i] + "\") should throw exception.");
            }
        }
        if (expectedExceptions.cardinality() != malformedIPv4s.length) {
            throw new RuntimeException("Failed: some expected UnknownHostExceptions are not thrown.");
        }
    }
}
