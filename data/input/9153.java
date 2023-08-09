public class ModuleSubject {
    public static void main(String[] args) {
        LoginContext lc = null;
        try {
            lc = new LoginContext("SampleLogin");
        } catch (LoginException le) {
            System.out.println
                ("ModuleSubject test failed - login construction failed");
            throw new SecurityException(le.getMessage());
        }
        try {
            lc.login();
            throw new SecurityException
                ("ModuleSubject test failed: 1st login attempt did not fail!");
        } catch (LoginException le) {
            System.out.println
                ("Good: first attempt failed");
            le.printStackTrace();
        }
        if (lc.getSubject() != null) {
            throw new SecurityException
                ("ModuleSubject test failed - " +
                "Subject after failed attempt not null: " +
                lc.getSubject().toString());
        }
        try {
            lc.login();
            java.util.Set principals = lc.getSubject().getPrincipals();
            if (principals.size() != 1) {
                throw new SecurityException("ModuleSubject test failed: " +
                                        "corrupted subject");
            }
            java.util.Iterator i = principals.iterator();
            while (i.hasNext()) {
                Principal p = (Principal)i.next();
                System.out.println("principal after authentication = " +
                                p.toString());
            }
        } catch (LoginException le) {
            System.out.println
                ("ModuleSubject test failed - 2nd login attempt failed");
            throw new SecurityException(le.getMessage());
        }
        System.out.println("ModuleSubject test succeeded");
    }
}
