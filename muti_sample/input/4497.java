public class GetLocalHostWithSM {
        public static void main(String[] args) throws Exception {
            try {
                System.setProperty("host.name", InetAddress.
                                                getLocalHost().
                                                getHostName());
            } catch (UnknownHostException e) {
                System.out.println("Cannot find the local hostname, " +
                        "no nameserver entry found");
            }
            String policyFileName = System.getProperty("test.src", ".") +
                          "/" + "policy.file";
            System.setProperty("java.security.policy", policyFileName);
            System.setSecurityManager(new SecurityManager());
            InetAddress localHost1 = null;
            InetAddress localHost2 = null;
            localHost1 = InetAddress.getLocalHost();
            Subject mySubject = new Subject();
            MyPrincipal userPrincipal = new MyPrincipal("test");
            mySubject.getPrincipals().add(userPrincipal);
            localHost2 = (InetAddress)Subject.doAsPrivileged(mySubject,
                                new MyAction(), null);
            if (localHost1.equals(localHost2)) {
                throw new RuntimeException("InetAddress.getLocalHost() test " +
                                           " fails. localHost2 should be " +
                                           " the real address instead of " +
                                           " the loopback address."+localHost2);
            }
        }
}
class MyAction implements PrivilegedExceptionAction {
    public Object run() throws Exception {
        return InetAddress.getLocalHost();
    }
}
