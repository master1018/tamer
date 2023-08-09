public class EncodeURL {
    private static final String extInput = "foo bar";
    private static final String extAnswer = "foo%20bar";
    private static final String policy0 =
        "grant codebase \"${java.ext.dirs}\" { permission java.security.AllPermission; };";
    private static final String prop1 = "http:
    private static final String answer1 = "http:
    private static final String policy1 =
        "keystore \"${prop1}/foo\"; grant { permission java.security.AllPermission; };";
    private static final String prop2 = "foo#bar";
    private static final String answer2 = "http:
    private static final String policy2 =
        "keystore \"http:
    private static final String prop3 = "goofy:foo#bar";
    private static final String answer3 = "http:
    private static final String policy3 =
        "keystore \"http:
    public static void main(String[] args) throws Exception {
        System.setProperty("java.ext.dirs", extInput);
        PolicyParser pp = new PolicyParser(true);
        pp.read(new StringReader(policy0));
        Enumeration e = pp.grantElements();
        while (e.hasMoreElements()) {
            PolicyParser.GrantEntry ge =
                                (PolicyParser.GrantEntry)e.nextElement();
            if (ge.codeBase.indexOf("foo") >= 0 &&
                ge.codeBase.indexOf(extAnswer) < 0) {
                throw new SecurityException("test 0 failed: " +
                        "expected " + extAnswer +
                        " inside " + ge.codeBase);
            }
        }
        System.setProperty("prop1", prop1);
        pp = new PolicyParser(true);
        pp.read(new StringReader(policy1));
        if (!pp.getKeyStoreUrl().equals(answer1)) {
            throw new SecurityException("test 1 failed: " +
                "expected " + answer1 +
                ", and got " + pp.getKeyStoreUrl());
        }
        System.setProperty("prop2", prop2);
        pp = new PolicyParser(true);
        pp.read(new StringReader(policy2));
        if (!pp.getKeyStoreUrl().equals(answer2)) {
            throw new SecurityException("test 2 failed: " +
                "expected " + answer2 +
                ", and got " + pp.getKeyStoreUrl());
        }
        System.setProperty("prop3", prop3);
        pp = new PolicyParser(true);
        pp.read(new StringReader(policy3));
        if (!pp.getKeyStoreUrl().equals(answer3)) {
            throw new SecurityException("test 3 failed: " +
                "expected " + answer3 +
                ", and got " + pp.getKeyStoreUrl());
        }
        System.out.println("test passed");
    }
}
