public class Synch3 {
    public static void main(String[] args) {
        Subject subject = new Subject();
        final Set principals = subject.getPrincipals();
        principals.add(new X500Principal("CN=Alice"));
        new Thread() {
            {
                setDaemon(true);
                start();
            }
            public void run() {
                X500Principal p = new X500Principal("CN=Bob");
                while (true) {
                    principals.add(p);
                    principals.remove(p);
                }
            }
        };
        for (int i = 0; i < 1000; i++) {
            subject.getPrincipals(X500Principal.class);
        }
    }
}
