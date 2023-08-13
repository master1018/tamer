public class Synch {
    public static void main(String[] args) {
        Subject subject = new Subject();
        final Set principals = subject.getPrincipals();
        principals.add(new X500Principal("CN=Alice"));
        new Thread() {
            { setDaemon(true); }
            public void run() {
                Principal last = new X500Principal("CN=Bob");
                for (int i = 0; true; i++) {
                    Principal next = new X500Principal("CN=Bob" + i);
                    principals.add(next);
                    principals.remove(last);
                    last = next;
                }
            }
        }.start();
        for (int i = 0; i < 1000; i++) {
            Subject.doAs(
                subject,
                new PrivilegedAction() {
                    public Object run() {
                        return Subject.doAs(
                            new Subject(true,
                                        Collections.singleton(
                                            new X500Principal("CN=Claire")),
                                        Collections.EMPTY_SET,
                                        Collections.EMPTY_SET),
                            new PrivilegedAction() {
                                public Object run() {
                                    return null;
                                }
                            });
                    }
                });
        }
    }
}
