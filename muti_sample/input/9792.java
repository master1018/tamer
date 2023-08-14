public class Test5653 {
    public static void main(String[] args)
            throws Exception {
        Oid oldOid = new Oid("1.3.6.1.5.6.2");
        new OneKDC(null).writeJAASConf();
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        GSSManager m = GSSManager.getInstance();
        boolean found = false;
        for (Oid tmp: m.getMechsForName(oldOid)) {
            if (tmp.equals(GSSUtil.GSS_KRB5_MECH_OID)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new Exception("Cannot found krb5 mech for old name type");
        }
        GSSName name = m.createName("server@host.rabbit.hole", oldOid);
        if (!name.getStringNameType().equals(GSSName.NT_HOSTBASED_SERVICE)) {
            throw new Exception("GSSName not correct name type");
        }
        GSSContext c1 = m.createContext(
                name,
                GSSUtil.GSS_KRB5_MECH_OID,
                null,
                GSSContext.DEFAULT_LIFETIME);
        byte[] token = c1.initSecContext(new byte[0], 0, 0);
        Context s;
        s = Context.fromJAAS("server");
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        s.x().acceptSecContext(token, 0, token.length);
    }
}
