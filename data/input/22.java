public class B6246242 {
    public static void main(String[] args) {
        InetAddress a;
        try {
            a = InetAddress.getByName("foo.bar");
        } catch (UnknownHostException e) {
            String s = e.getMessage();
            if (s.indexOf("foo.bar: foo.bar") >= 0)
                throw new RuntimeException("UnknownHostException has wrong message: " + s);
        }
    }
}
