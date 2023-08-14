public class SelfExpansion {
    public static void main(String[] args) throws Exception {
        Subject s = new Subject();
        s.getPrincipals().add
                (new javax.security.auth.x500.X500Principal("CN=test"));
        s.getPrivateCredentials().add(new String("test"));
        try {
            Subject.doAsPrivileged(s, new PrivilegedAction() {
                public Object run() {
                    java.util.Iterator i = Subject.getSubject
                                (AccessController.getContext
                                ()).getPrivateCredentials().iterator();
                    return i.next();
                }
            }, null);
            System.out.println("Test succeeded");
        } catch (Exception e) {
            System.out.println("Test failed");
            e.printStackTrace();
            throw e;
        }
    }
}
