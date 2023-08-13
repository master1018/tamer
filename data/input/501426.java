public class SubjectDomainCombiner implements DomainCombiner {
    private Subject subject;
    private static final AuthPermission _GET = new AuthPermission(
            "getSubjectFromDomainCombiner"); 
    public SubjectDomainCombiner(Subject subject) {
        super();
        if (subject == null) {
            throw new NullPointerException();
        }
        this.subject = subject;
    }
    public Subject getSubject() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(_GET);
        }
        return subject;
    }
    public ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
            ProtectionDomain[] assignedDomains) {
        int len = 0;
        if (currentDomains != null) {
            len += currentDomains.length;
        }
        if (assignedDomains != null) {
            len += assignedDomains.length;
        }
        if (len == 0) {
            return null;
        }
        ProtectionDomain[] pd = new ProtectionDomain[len];
        int cur = 0;
        if (currentDomains != null) {
            Set<Principal> s = subject.getPrincipals();
            Principal[] p = s.toArray(new Principal[s.size()]);
            for (cur = 0; cur < currentDomains.length; cur++) {
                ProtectionDomain newPD;
                newPD = new ProtectionDomain(currentDomains[cur].getCodeSource(),
                        currentDomains[cur].getPermissions(), currentDomains[cur]
                                .getClassLoader(), p);
                pd[cur] = newPD;
            }
        }
        if (assignedDomains != null) {
            System.arraycopy(assignedDomains, 0, pd, cur, assignedDomains.length);
        }
        return pd;
    }
}
