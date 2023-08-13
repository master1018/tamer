public class Synch2 {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        Subject subject = new Subject();
        final Set principals = subject.getPrincipals();
        principals.add(new X500Principal("CN=Alice"));
        final Set credentials = subject.getPrivateCredentials();
        credentials.add("Dummy credential");
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
            synchronized (credentials) {
                for (Iterator it = credentials.iterator(); it.hasNext(); ) {
                    it.next();
                }
            }
        }
    }
}
