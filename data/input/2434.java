public class InquireSecContextPermissionCheck {
    public static void main(String[] args) throws Exception {
        InquireSecContextPermission p0, p1;
        p0 = new InquireSecContextPermission(
                "KRB5_GET_SESSION_KEY");
        p1 = new InquireSecContextPermission("*");
        if (!p1.implies(p0) || !p1.implies(p1) || !p0.implies(p0)) {
            throw new Exception("Check failed");
        }
        if (p0.implies(p1)) {
            throw new Exception("This is bad");
        }
    }
}
