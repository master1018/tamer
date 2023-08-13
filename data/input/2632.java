public class Optimize {
    public static void main(String[] args) {
        ProtectionDomain pd1 = new ProtectionDomain(
            new CodeSource(null, (java.security.cert.Certificate[]) null),
            new Permissions());
        ProtectionDomain pd2 = new ProtectionDomain(
            new CodeSource(null, (java.security.cert.Certificate[]) null),
            new Permissions());
        ProtectionDomain pd3 = new ProtectionDomain(
            new CodeSource(null, (java.security.cert.Certificate[]) null),
            new Permissions());
        ProtectionDomain[] current = new ProtectionDomain[] {pd1, pd2};
        ProtectionDomain[] assigned = new ProtectionDomain[] {pd3, pd2};
        SubjectDomainCombiner sdc = new SubjectDomainCombiner(new Subject());
        ProtectionDomain[] combined = sdc.combine(current, assigned);
        if (combined.length == 4 &&
            combined[0] != pd1 && combined[1] != pd2 &&
            combined[2] == pd3 && combined[3] == pd2) {
            System.out.println("test passed");
        } else {
            System.out.println("test failed");
            throw new SecurityException("Test Failed");
        }
    }
}
