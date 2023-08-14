public class OneKDC extends KDC {
    public static final String USER = "dummy";
    public static final char[] PASS = "bogus".toCharArray();
    public static final String USER2 = "foo";
    public static final char[] PASS2 = "bar".toCharArray();
    public static final String KRB5_CONF = "localkdc-krb5.conf";
    public static final String KTAB = "localkdc.ktab";
    public static final String JAAS_CONF = "localkdc-jaas.conf";
    public static final String REALM = "RABBIT.HOLE";
    public static String SERVER = "server/host." + REALM.toLowerCase();
    public static String BACKEND = "backend/host." + REALM.toLowerCase();
    public static String KDCHOST = "kdc." + REALM.toLowerCase();
    public OneKDC(String etype) throws Exception {
        super(REALM, KDCHOST, 0, true);
        addPrincipal(USER, PASS);
        addPrincipal(USER2, PASS2);
        addPrincipalRandKey("krbtgt/" + REALM);
        addPrincipalRandKey(SERVER);
        addPrincipalRandKey(BACKEND);
        KDC.saveConfig(KRB5_CONF, this,
                "forwardable = true",
                "default_keytab_name = " + KTAB,
                etype == null ? "" : "default_tkt_enctypes=" + etype + "\ndefault_tgs_enctypes=" + etype);
        System.setProperty("java.security.krb5.conf", KRB5_CONF);
        Config.refresh();
        writeKtab(KTAB);
        new File(KRB5_CONF).deleteOnExit();
        new File(KTAB).deleteOnExit();
    }
    public void writeJAASConf() throws IOException {
        System.setProperty("java.security.auth.login.config", JAAS_CONF);
        File f = new File(JAAS_CONF);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write((
                "com.sun.security.jgss.krb5.initiate {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required;\n};\n" +
                "com.sun.security.jgss.krb5.accept {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required\n" +
                "    principal=\"" + SERVER + "\"\n" +
                "    useKeyTab=true\n" +
                "    isInitiator=false\n" +
                "    storeKey=true;\n};\n" +
                "client {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required;\n};\n" +
                "server {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required\n" +
                "    principal=\"" + SERVER + "\"\n" +
                "    useKeyTab=true\n" +
                "    storeKey=true;\n};\n" +
                "backend {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required\n" +
                "    principal=\"" + BACKEND + "\"\n" +
                "    useKeyTab=true\n" +
                "    storeKey=true\n" +
                "    isInitiator=false;\n};\n"
                ).getBytes());
        fos.close();
        f.deleteOnExit();
        Security.setProperty("auth.login.defaultCallbackHandler", "OneKDC$CallbackForClient");
    }
    public static class CallbackForClient implements CallbackHandler {
        public void handle(Callback[] callbacks) {
            String user = OneKDC.USER;
            char[] pass = OneKDC.PASS;
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    System.out.println("Callback for name: " + user);
                    ((NameCallback) callback).setName(user);
                }
                if (callback instanceof PasswordCallback) {
                    System.out.println("Callback for pass: "
                            + new String(pass));
                    ((PasswordCallback) callback).setPassword(pass);
                }
            }
        }
    }
}
