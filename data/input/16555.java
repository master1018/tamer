public class KeyTabCompat {
    public static void main(String[] args)
            throws Exception {
        OneKDC kdc = new OneKDC("aes128-cts");
        kdc.writeJAASConf();
        kdc.addPrincipal(OneKDC.SERVER, "pass1".toCharArray());
        kdc.writeKtab(OneKDC.KTAB);
        Context c, s;
        c = Context.fromUserPass(OneKDC.USER, OneKDC.PASS, false);
        s = Context.fromUserPass(OneKDC.USER2, OneKDC.PASS2, true);
        s.s().getPrincipals().clear();
        c.startAsClient(OneKDC.USER2, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
        c = Context.fromJAAS("client");
        s = Context.fromJAAS("server");
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        s.status();
        if (s.s().getPrivateCredentials(KerberosKey.class).size() != 1) {
            throw new Exception("There should be one KerberosKey");
        }
        Thread.sleep(2000);     
        kdc.addPrincipal(OneKDC.SERVER, "pass2".toCharArray());
        kdc.writeKtab(OneKDC.KTAB);
        Context.handshake(c, s);
        s.status();
        if (s.s().getPrivateCredentials(KerberosKey.class).size() != 1) {
            throw new Exception("There should be only one KerberosKey");
        }
    }
}
