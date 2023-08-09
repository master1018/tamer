public class NoDefaultSystemScope {
    public static void main(String args[]) throws Exception {
        IdentityScope s = IdentityScope.getSystemScope();
        if (s != null) {
            throw new Exception("The default system scope should be null");
        }
        System.out.println("TEST PASSED");
    }
}
