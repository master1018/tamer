public class LdapParserTests {
    public static void main(String args[])
                throws Exception {
        Rdn rdn = null;
        String[] mustEscSpecials = new String[]
                                {";", ",", "\\", "+"};
        String[] specials = new String[]
                        {";", ",", "\\", "<", ">", "#", "\"", "+"};
        System.out.println();
        System.out.print("*****Tests with unescaped special ");
        System.out.println("characters in the Rdn*****");
        for (int i = 0; i < mustEscSpecials.length; i++) {
            String rdnStr = "cn=Juicy" + mustEscSpecials[i] + "Fruit";
            testInvalids(rdnStr);
        }
        System.out.println();
        System.out.println("******Special character escaping tests ******");
        for (int i = 0; i < specials.length; i++) {
            rdn = new Rdn("cn=Juicy\\" + specials[i] + "Fruit");
        }
        System.out.println("Escape leading space:" +
                new Rdn("cn=\\  Juicy Fruit")); 
        System.out.println("Escaped leading #:" +
                new Rdn("cn=\\#Juicy Fruit"));  
        System.out.println("Escaped trailing space:" +
                new Rdn("cn=Juicy Fruit\\  ")); 
        System.out.println();
        System.out.println(
                "******Other unescaped special character tests ******");
        rdn = new Rdn("cn=  Juicy Fruit");
        System.out.println(
            "Accepted Rdn with value containing leading spaces:" +
            rdn.toString());
        rdn = new Rdn("cn=Juicy Fruit  ");
        System.out.println(
            "Accepted Rdn with value containing trailing spaces:" +
            rdn.toString());
        String[] invalids =  new String[]
                {"cn=#xabc", "cn=#axcd", "cn=#abcx", "cn=#abcdex"};
        for (int i = 0; i < invalids.length; i++) {
            testInvalids(invalids[i]);
        }
        System.out.println();
        System.out.println(
                "***************Other special cases****************");
        LdapName name = new LdapName("");
        System.out.println("Empty LDAP name:" + name.toString());
        rdn = new Rdn("cn=\"Juicy ,=+<>#; Fruit\"");
        System.out.println("Quoted Rdn string:" + rdn.toString());
        rdn = new Rdn("SN=Lu\\C4\\8Di\\C4\\87");
        System.out.println("Unicode Rdn string:" + rdn.toString());
        name = new LdapName(
                "1.3.6.1.4.1.466.0=#04024869,O=Test,C=GB");
        System.out.println("binary valued LDAP name:" + name.toString());
        name = new LdapName("CN=Steve Kille;O=Isode;C=GB");
        System.out.println("';' seperated LDAP name:" + name.toString());
    }
    static void testInvalids(String rdnStr) throws Exception {
        boolean isExcepThrown = false;
        Rdn rdn = null;
        try {
            rdn = new Rdn(rdnStr);
        } catch (InvalidNameException e) {
            System.out.println("Caught the right exception: " + e);
            isExcepThrown = true;
        } catch (IllegalArgumentException e) {
            System.out.println("Caught the right exception: " + e);
            isExcepThrown = true;
        }
        if (!isExcepThrown) {
            throw new Exception(
                    "Accepted an invalid Rdn string:" +
                    rdnStr + " as Rdn: " + rdn);
        }
    }
}
