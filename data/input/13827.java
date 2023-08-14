public class EmbeddedEquals {
    static String test = "WWW-Authenticate: Digest realm=\"testrealm\","+
                      "nonce=\"Ovqrpw==b20ff3b0ea3f3a18f1d6293331edaafdb98f5bef\", algorithm=MD5,"+
                      "domain=\"http:
    public static void main (String args[]) {
        HeaderParser hp = new HeaderParser (test);
        String r1 = hp.findValue ("nonce");
        if (r1 == null || !r1.equals ("Ovqrpw==b20ff3b0ea3f3a18f1d6293331edaafdb98f5bef")) {
            throw new RuntimeException ("first findValue returned wrong result: " + r1);
        }
        r1 = hp.findValue ("AlGoRiThm");
        if (r1 == null || !r1.equals ("MD5")) {
            throw new RuntimeException ("second findValue returned wrong result: " + r1);
        }
    }
}
