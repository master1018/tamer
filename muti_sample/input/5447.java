public class BadKdc2 {
    public static void main(String[] args)
            throws Exception {
        Security.setProperty("krb5.kdc.bad.policy", "tryLess:2,1000");
        BadKdc.go(
                new int[]{1,2,1,2,1,2,2,2,2,2,2,2,3,2,1,1,1,1,2,1,2,1,3,2}, 
                new int[]{1,1,1,1,2,1,2,1,3,2,1,1,1,1,2,1,2,1,3,2}, 
                new int[]{1,2,1,2,1,2,2,2,2,2,2,2,3,2,1,1,1,1,2,1,2,1,3,2}, 
                new int[]{1,1,1,1,2,1,1,1,1,1,2,2}, 
                new int[]{1,1,1,2}  
        );
    }
}
