public class GssNPE {
    public static void main(String[] argv) throws Exception {
        System.setProperty("java.security.auth.login.config",
                "this.file.does.not.exist");
        try {
            GSSUtil.login(GSSCaller.CALLER_INITIATE, GSSUtil.GSS_KRB5_MECH_OID);
        } catch (SecurityException se) {
            if (se.getCause() instanceof java.io.IOException) {
            } else {
                throw se;
            }
        }
    }
}
