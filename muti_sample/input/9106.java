public class TwoPrinces {
    public static void main(String[] args)
            throws Exception {
        KDC k1 = KDC.create("R1");
        k1.addPrincipal("u1", "hello".toCharArray());
        k1.addPrincipalRandKey("krbtgt/R1");
        k1.addPrincipalRandKey("host/same.host");
        KDC k2 = KDC.create("R2");
        k2.addPrincipal("u2", "hello".toCharArray());
        k2.addPrincipalRandKey("krbtgt/R2");
        k2.addPrincipalRandKey("host/same.host");
        System.setProperty("java.security.krb5.conf", "krb5.conf");
        KDC.saveConfig("krb5.conf", k1, k2);
        Config.refresh();
        k1.writeKtab("ktab1");
        k2.writeKtab("ktab2");
        System.setProperty("java.security.auth.login.config", "jaas.conf");
        File f = new File("jaas.conf");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write((
                "me {\n"
                + "  com.sun.security.auth.module.Krb5LoginModule required"
                + "    isInitiator=true principal=\"host/same.host@R1\""
                + "    useKeyTab=true keyTab=ktab1 storeKey=true;\n"
                + "  com.sun.security.auth.module.Krb5LoginModule required"
                + "    isInitiator=true principal=\"host/same.host@R2\""
                + "    useKeyTab=true keyTab=ktab2 storeKey=true;\n"
                + "};\n"
                ).getBytes());
        fos.close();
        Context s = Context.fromJAAS("me");
        s.startAsServer("host@same.host", GSSUtil.GSS_KRB5_MECH_OID);
        Context c1 = Context.fromUserPass("u1", "hello".toCharArray(), false);
        c1.startAsClient("host@same.host", GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c1, s);
        KDC.saveConfig("krb5.conf", k2, k1);
        Config.refresh();
        s.startAsServer("host@same.host", GSSUtil.GSS_KRB5_MECH_OID);
        Context c2 = Context.fromUserPass("u2", "hello".toCharArray(), false);
        c2.startAsClient("host@same.host", GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c2, s);
    }
}
