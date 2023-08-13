public class Enforcer extends SecurityManager {
    public static final Enforcer THE_ONE = new Enforcer();
    private boolean deny;
    private Enforcer() {
        deny = false;
    }
    public void denyNext() {
        deny = true;
    }
    private void denyIfAppropriate() {
        if (deny) {
            deny = false;
            throw new SecurityException("Denied!");
        }
    }
    public void checkPackageAccess(String pkg) {
        System.out.println("checkPackageAccess: " + pkg);
        denyIfAppropriate();
        super.checkPackageAccess(pkg);
    }
    public void checkMemberAccess(Class c, int which) {
        String member;
        switch (which) {
            case Member.DECLARED: member = "DECLARED"; break;
            case Member.PUBLIC:   member = "PUBLIC";   break;
            default:              member = "<" + which + ">?"; break;
        }
        System.out.println("checkMemberAccess: " + c.getName() + ", " +
                member);
        denyIfAppropriate();
        super.checkMemberAccess(c, which);
    }
    public void checkPermission(Permission perm) {
        System.out.println("checkPermission: " + perm);
        denyIfAppropriate();
        super.checkPermission(perm);
    }
    public void checkPermission(Permission perm, Object context) {
        System.out.println("checkPermission: " + perm + ", " + context);
        denyIfAppropriate();
        super.checkPermission(perm, context);
    }
}
