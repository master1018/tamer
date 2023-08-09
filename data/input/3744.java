public class CanonError {
    public static void main(String[] args) {
        PrivateCredentialPermission pcp1 = new PrivateCredentialPermission
                ("a b \"pcp1\"", "read");
        PrivateCredentialPermission pcp2 = new PrivateCredentialPermission
                ("a b \"pcp1\"", "read");
        if (!pcp1.equals(pcp2) || !pcp2.equals(pcp1))
            throw new SecurityException("CanonError test failed: #1");
        if (!pcp1.implies(pcp2) || !pcp2.implies(pcp1))
            throw new SecurityException("CanonError test failed: #2");
        PrivateCredentialPermission pcp3 = new PrivateCredentialPermission
                ("a b \"pcp3\"", "read");
        if (pcp1.equals(pcp3) || pcp3.equals(pcp1))
            throw new SecurityException("CanonError test failed: #3");
        if (pcp1.implies(pcp3) || pcp3.implies(pcp1))
            throw new SecurityException("CanonError test failed: #4");
        PrivateCredentialPermission pcp_4 = new PrivateCredentialPermission
                ("a b \"pcp 4\"", "read");
        PrivateCredentialPermission pcp__4 = new PrivateCredentialPermission
                ("a b \"pcp  4\"", "read");
        if (pcp_4.equals(pcp__4) || pcp__4.equals(pcp_4))
            throw new SecurityException("CanonError test failed: #5");
        if (pcp_4.implies(pcp__4) || pcp__4.implies(pcp_4))
            throw new SecurityException("CanonError test failed: #6");
        String credClass = pcp__4.getCredentialClass();
        System.out.println("credentialClass = " + credClass);
        String[][] principals = pcp__4.getPrincipals();
        if (!principals[0][1].equals("pcp  4"))
            throw new SecurityException("CanonError test failed: #7");
        for (int i = 0; i < principals.length; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println("principals[" + i + "][" + j + "] = " +
                                principals[i][j]);
            }
        }
        credClass = pcp_4.getCredentialClass();
        System.out.println("credentialClass = " + credClass);
        principals = pcp_4.getPrincipals();
        if (!principals[0][1].equals("pcp 4"))
            throw new SecurityException("CanonError test failed: #8");
        for (int i = 0; i < principals.length; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println("principals[" + i + "][" + j + "] = " +
                                principals[i][j]);
            }
        }
        System.out.println("CanonError test passed");
    }
}
