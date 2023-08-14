public class PrincipalExpansionError {
    public static void main(String[] args) {
        Subject s = new Subject();
        try {
            Set principals = s.getPrincipals();
            principals.add(new SolarisPrincipal("TestPrincipal"));
        } catch (SecurityException se) {
            throw new SecurityException
                ("PrincipalExpansionError test incorrectly set up:" + se);
        }
        try {
            Subject.doAs(s, new PrincipalExpansionErrorAction());
            System.out.println("PrincipalExpansionError test failed");
            throw new SecurityException("PrincipalExpansionError test failed");
        } catch (java.security.PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof java.io.FileNotFoundException) {
                System.out.println
                    ("PrincipalExpansionError test failed (file not found)");
                java.io.FileNotFoundException fnfe =
                        (java.io.FileNotFoundException)e;
                throw new SecurityException("PrincipalExpansionError" +
                        "test failed (file not found)");
            } else {
                System.out.println("what happened?");
                pae.printStackTrace();
            }
        } catch (SecurityException se) {
                System.out.println("PrincipalExpansionError test succeeded");
                se.printStackTrace();
        }
    }
}
