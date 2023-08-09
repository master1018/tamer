public class TrustedCert {
    public static void main(String[] args) {
        System.out.println(
            Subject.doAsPrivileged(
                new Subject(true,
                            Collections.singleton(new X500Principal("CN=Tim")),
                            Collections.EMPTY_SET,
                            Collections.EMPTY_SET),
                new PrivilegedAction() {
                    public Object run() {
                        return System.getProperty("foo");
                    }
                },
                null));
    }
}
