public class DNParserTest extends TestCase {
    public void testFind() {
        checkFind("", "cn", null);
        checkFind("ou=xxx", "cn", null);
        checkFind("ou=xxx,cn=xxx", "cn", "xxx");
        checkFind("ou=xxx+cn=yyy,cn=zzz+cn=abc", "cn", "yyy");
        checkFind("2.5.4.3=a,ou=xxx", "cn", "a"); 
        checkFind("cn=a,cn=b", "cn", "a");
        checkFind("ou=Cc,ou=Bb,ou=Aa", "ou", "Cc");
        checkFind("cn=imap.gmail.com", "cn", "imap.gmail.com");
        checkFind("o=\"\\\" a ,=<>#;\"", "o", "\" a ,=<>#;");
        checkFind("o=abc\\,def", "o", "abc,def");
        checkFind("cn=Lu\\C4\\8Di\\C4\\87", "cn", "\u004c\u0075\u010d\u0069\u0107");
        checkFind("ou=a, o=  a  b  ,cn=x", "o", "a  b");
        checkFind("o=\"  a  b  \" ,cn=x", "o", "  a  b  ");
    }
    private void checkFind(String dn, String attrType, String expected) {
        String actual = new DNParser(new X500Principal(dn)).find(attrType);
        assertEquals("dn:" + dn + "  attr:" + attrType, expected, actual);
    }
}
