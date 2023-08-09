public class DefaultGssConfig {
    public static void main(String[] argv) throws Exception {
        try {
            Configuration.getInstance("JavaLoginConfig", new URIParameter(new URI("file:
        } catch (NoSuchAlgorithmException nsae) {
            if (nsae.getCause() instanceof IOException &&
                    !(nsae.getCause() instanceof FileNotFoundException)) {
            } else {
                throw nsae;
            }
        }
        Configuration.getConfiguration();
        LoginConfigImpl lc = new LoginConfigImpl(GSSCaller.CALLER_INITIATE, GSSUtil.GSS_KRB5_MECH_OID);
        if (lc.getAppConfigurationEntry("").length == 0) {
            throw new Exception("No default config for GSS krb5 client");
        }
        lc = new LoginConfigImpl(GSSCaller.CALLER_ACCEPT, GSSUtil.GSS_KRB5_MECH_OID);
        if (lc.getAppConfigurationEntry("").length == 0) {
            throw new Exception("No default config for GSS krb5 server");
        }
    }
}
