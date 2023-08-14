public class BadKdc4 {
    public static void main(String[] args)
            throws Exception {
        Security.setProperty("krb5.kdc.bad.policy", "");
        BadKdc.go(
            new int[]{1,2,1,2,1,2,2,2,2,2,2,2,3,2,1,2,1,2,1,2,2,2,2,2,2,2,3,2},
            new int[]{1,2,1,2,1,2,2,2,2,2,2,2,3,2,1,2,1,2,1,2,2,2,2,2,2,2,3,2},
            new int[]{1,2,1,2,1,2,2,2,2,2,2,2,3,2,1,2,1,2,1,2,2,2,2,2,2,2,3,2},
            new int[]{1,2,1,2,1,2,2,2,1,2,1,2,1,2,2,2},
            new int[]{1,2,1,2}
        );
    }
}
