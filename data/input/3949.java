public class NoSpnegoAsDefMech {
    public static void main(String[] argv) throws Exception {
        System.setProperty("sun.security.jgss.mechanism", GSSUtil.GSS_SPNEGO_MECH_OID.toString());
        try {
            GSSManager.getInstance().createName("service@host", GSSName.NT_HOSTBASED_SERVICE, new Oid("1.3.6.1.5.5.2"));
        } catch (GSSException e) {
        }
    }
}
