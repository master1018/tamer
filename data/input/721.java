public class NewSalt {
    public static void main(String[] args)
            throws Exception {
        KDC kdc = new OneKDC(null);
        if (System.getProperty("onlyonepreauth") != null) {
            KDC.saveConfig(OneKDC.KRB5_CONF, kdc,
                    "default_tgs_enctypes=des3-cbc-sha1");
            Config.refresh();
            kdc.setOption(KDC.Option.ONLY_ONE_PREAUTH, true);
        }
        if (System.getProperty("nopreauth") != null) {
            kdc.setOption(KDC.Option.PREAUTH_REQUIRED, false);
        }
        Context c1 = Context.fromUserPass(OneKDC.USER.toUpperCase(),
                OneKDC.PASS, true);
        Context c2 = Context.fromUserPass(OneKDC.USER2.toUpperCase(),
                OneKDC.PASS2, true);
        c1.startAsClient(OneKDC.USER2, GSSUtil.GSS_KRB5_MECH_OID);
        c2.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c1, c2);
    }
}
