public class MoreKvno {
    static PrincipalName p;
    public static void main(String[] args)
            throws Exception {
        OneKDC kdc = new OneKDC(null);
        kdc.writeJAASConf();
        KeyTab ktab = KeyTab.create(OneKDC.KTAB);
        p = new PrincipalName(
            OneKDC.SERVER+"@"+OneKDC.REALM, PrincipalName.KRB_NT_SRV_HST);
        ktab.addEntry(p, "pass1".toCharArray(), 1, true);
        ktab.addEntry(p, "pass3".toCharArray(), 3, true);
        ktab.addEntry(p, "pass2".toCharArray(), 2, true);
        ktab.save();
        char[] pass = "pass2".toCharArray();
        kdc.addPrincipal(OneKDC.SERVER, pass);
        go(OneKDC.SERVER, "com.sun.security.jgss.krb5.accept", pass);
        pass = "pass3".toCharArray();
        kdc.addPrincipal(OneKDC.SERVER, pass);
        go(OneKDC.SERVER, "server", pass);
        try {
            pass = "pass4".toCharArray();
            kdc.addPrincipal(OneKDC.SERVER, pass);
            go(OneKDC.SERVER, "com.sun.security.jgss.krb5.accept", pass);
            throw new Exception("This test should fail");
        } catch (GSSException gsse) {
            KrbException ke = (KrbException)gsse.getCause();
            if (ke.returnCode() != Krb5.KRB_AP_ERR_BADKEYVER) {
                throw new Exception("Not expected failure code: " +
                        ke.returnCode());
            }
        }
    }
    static void go(String server, String entry, char[] pass) throws Exception {
        Context c, s;
        c = Context.fromUserPass("dummy", "bogus".toCharArray(), false);
        s = Context.fromJAAS(entry);
        c.startAsClient(server, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
        s.dispose();
        c.dispose();
        c = Context.fromUserPass("dummy", "bogus".toCharArray(), false);
        s = Context.fromUserPass(p.getNameString(), pass, true);
        c.startAsClient(server, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
        s.dispose();
        c.dispose();
    }
}
