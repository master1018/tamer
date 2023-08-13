public class Regression {
    public static void main(String[] args) {
        Set principals = new HashSet();
        principals.add(new com.sun.security.auth.NTUserPrincipal("test1"));
        principals.add(new com.sun.security.auth.NTUserPrincipal("test2"));
        Subject subject = new Subject
                (false, principals, new HashSet(), new HashSet());
        SubjectDomainCombiner sdc = new SubjectDomainCombiner(subject);
        URL url1;
        URL url2;
        URL url3;
        URL url4;
        try {
            url1 = new URL("http:
            url2 = new URL("http:
            url3 = new URL("http:
            url4 = new URL("http:
        } catch (java.net.MalformedURLException mue) {
            mue.printStackTrace();
            throw new SecurityException("Test failed: " + mue.toString());
        }
        ProtectionDomain d1 = new ProtectionDomain
                                (new CodeSource(url1,
                                    (java.security.cert.Certificate[]) null),
                                null,                   
                                null,                   
                                null);                  
        ProtectionDomain d2 = new ProtectionDomain
                                (new CodeSource(url2,
                                    (java.security.cert.Certificate[]) null),
                                null,                   
                                null,                   
                                null);                  
        ProtectionDomain d3 = new ProtectionDomain
                                (new CodeSource(url3,
                                    (java.security.cert.Certificate[]) null),
                                null,                   
                                null,                   
                                null);                  
        ProtectionDomain d4 = new ProtectionDomain
                                (new CodeSource(url4,
                                    (java.security.cert.Certificate[]) null),
                                null,                   
                                null,                   
                                null);                  
        ProtectionDomain currentDomains[] = { d1, d2, d3 };
        ProtectionDomain assignedDomains[] = { d4 };
        ProtectionDomain domains1[] = sdc.combine
                        (currentDomains, assignedDomains);
        if (domains1.length != 4 ||
            domains1[0] == d1 || domains1[1] == d2 || domains1[2] == d3 ||
            domains1[3] != d4 ||
            !domains1[0].implies(new RuntimePermission("queuePrintJob"))) {
            throw new SecurityException("Test failed: combine test 1 failed");
        }
        System.out.println("-------- TEST ONE PASSED --------");
        ProtectionDomain domains2[] = sdc.combine
                        (currentDomains, assignedDomains);
        if (domains2.length != 4 ||
            domains2[0] != domains1[0] || domains2[1] != domains1[1] ||
            domains2[2] != domains1[2] ||
            domains2[3] != domains1[3] ||
            !domains2[0].implies(new RuntimePermission("queuePrintJob"))) {
            throw new SecurityException("Test failed: combine test 2 failed");
        }
        System.out.println("-------- TEST TWO PASSED --------");
        subject.getPrincipals().remove
                (new com.sun.security.auth.NTUserPrincipal("test2"));
        ProtectionDomain domains3[] = sdc.combine
                        (currentDomains, assignedDomains);
        if (domains3.length != 4 ||
            domains3[0] == domains1[0] || domains3[1] == domains1[1] ||
            domains3[2] == domains1[2] ||
            domains3[3] != domains1[3] ||
            !domains3[0].implies(new RuntimePermission("createClassLoader")) ||
            domains3[0].implies(new RuntimePermission("queuePrintJob"))) {
            throw new SecurityException("Test failed: combine test 3 failed");
        }
        System.out.println("-------- TEST THREE PASSED --------");
        System.out.println("Test Passed");
    }
}
