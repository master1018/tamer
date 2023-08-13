public class Hang {
    public static void main(String[] args) throws Exception {
        try {
            InetAddress.getByName("host.company.com");
        } catch (IllegalStateException e) { }
        InetAddress.getByName("host.company.com");
    }
}
