public class Reuse {
    public static void main(String[] args) throws Exception {
        MulticastSocket s1, s2;
        try {
            s1 = new MulticastSocket(4160);
            s2 = new MulticastSocket(4160);
            s1.close();
            s2.close();
        } catch (BindException e) {
            throw new RuntimeException("MulticastSocket do no set SO_REUSEADDR");
        }
    }
}
