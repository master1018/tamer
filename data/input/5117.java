public class RFC4514 {
    private int failed = 0;
    public static void main(String[] args) throws Exception {
        new RFC4514().test();
    }
    private void test() throws Exception {
        parse("CN=\\ Space\\ ,C=US");
        parse("CN=Sp\\ ace,C=US");
        parse("CN=Eq=uals,C=US");
        parse("CN=\\00,C=US");
        parse("CN=Num#ber,C=US");
        parse("CN=Trailing \\20,C=US");
        parse("CN=Con\\09trol,C=US");
        if (failed != 0) {
            throw new Exception("Some RFC4514 tests FAILED");
        }
    }
    public void parse(String dnString) throws Exception {
        System.out.println("Parsing " + dnString);
        X500Principal dn = new X500Principal(dnString);
        String dnString2 = dn.getName();
        X500Principal dn2 = new X500Principal(dnString2);
        if (dn.equals(dn2)) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED");
            failed++;
        }
    }
}
