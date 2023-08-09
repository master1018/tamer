public class DynamicKeytab {
    Context c, s;
    public static void main(String[] args)
            throws Exception {
        new DynamicKeytab().go();
    }
    void go() throws Exception {
        OneKDC k = new OneKDC(null);
        k.writeJAASConf();
        Files.delete(Paths.get(OneKDC.KTAB));
        c = Context.fromJAAS("client");
        s = Context.fromJAAS("com.sun.security.jgss.krb5.accept");
        k.addPrincipal(OneKDC.SERVER, "pass1".toCharArray());
        k.writeKtab(OneKDC.KTAB);
        connect();
        k.addPrincipal(OneKDC.SERVER, "pass2".toCharArray());
        k.appendKtab(OneKDC.KTAB);
        connect();
        c = Context.fromJAAS("client");
        connect();
        c = Context.fromJAAS("client");
        k.addPrincipal(OneKDC.SERVER, "pass3".toCharArray());
        k.appendKtab(OneKDC.KTAB);
        k.addPrincipal(OneKDC.SERVER, "pass4".toCharArray());
        k.appendKtab(OneKDC.KTAB);
        k.addPrincipal(OneKDC.SERVER, "pass3".toCharArray());
        connect();
        try (FileOutputStream fos = new FileOutputStream(OneKDC.KTAB)) {
            fos.write("BADBADBAD".getBytes());
        }
        connect();
        Files.delete(Paths.get(OneKDC.KTAB));
        try {
            connect();
            throw new Exception("Should not success");
        } catch (GSSException gsse) {
            System.out.println(gsse);
            KrbException ke = (KrbException)gsse.getCause();
            if (ke.returnCode() != Krb5.API_INVALID_ARG) {
                throw new Exception("Not expected failure code: " +
                        ke.returnCode());
            }
        }
        k.addPrincipal(OneKDC.SERVER, "pass5".toCharArray());
        k.writeKtab(OneKDC.KTAB);   
        try {
            connect();
            throw new Exception("Should not success");
        } catch (GSSException gsse) {
            System.out.println(gsse);
            KrbException ke = (KrbException)gsse.getCause();
            if (ke.returnCode() != Krb5.KRB_AP_ERR_BADKEYVER) {
                throw new Exception("Not expected failure code: " +
                        ke.returnCode());
            }
        }
        KDC.create("EMPTY.REALM").writeKtab(OneKDC.KTAB);
        try {
            connect();
            throw new Exception("Should not success");
        } catch (GSSException gsse) {
            System.out.println(gsse);
            KrbException ke = (KrbException)gsse.getCause();
            if (ke.returnCode() != Krb5.API_INVALID_ARG) {
                throw new Exception("Not expected failure code: " +
                        ke.returnCode());
            }
        }
    }
    void connect() throws Exception {
        Thread.sleep(2000);     
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
    }
}
