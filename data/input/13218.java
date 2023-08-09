public class EscapedChars {
    public static void main(String[] args) throws Exception {
        String dn="CN=\\#user";
        X500Principal xp = new X500Principal(dn);
        System.out.println("RFC2253 DN is " +
            xp.getName(X500Principal.RFC2253));
        System.out.println("CANONICAL DN is is " +
            xp.getName(X500Principal.CANONICAL));
        String dn1 = xp.getName(X500Principal.CANONICAL);
        if (!(dn1.substring(3,5).equals("\\#")))
            throw new Exception("Leading # not escaped");
        X500Principal xp1 = new X500Principal(dn1);
        System.out.println("CANONICAL DN is " +
            xp1.getName(X500Principal.CANONICAL));
    }
}
