public class CrossRealm implements CallbackHandler {
    public static void main(String[] args) throws Exception {
        startKDCs();
        xRealmAuth();
    }
    static void startKDCs() throws Exception {
        KDC kdc1 = KDC.create("RABBIT.HOLE");
        kdc1.addPrincipal("dummy", "bogus".toCharArray());
        kdc1.addPrincipalRandKey("krbtgt/RABBIT.HOLE");
        kdc1.addPrincipal("krbtgt/SNAKE.HOLE@RABBIT.HOLE",
                "rabbit->snake".toCharArray());
        KDC kdc2 = KDC.create("SNAKE.HOLE");
        kdc2.addPrincipalRandKey("krbtgt/SNAKE.HOLE");
        kdc2.addPrincipal("krbtgt/SNAKE.HOLE@RABBIT.HOLE",
                "rabbit->snake".toCharArray());
        kdc2.addPrincipalRandKey("host/www.snake.hole");
        KDC.saveConfig("krb5-localkdc.conf", kdc1, kdc2,
                "forwardable=true",
                "[domain_realm]",
                ".snake.hole=SNAKE.HOLE");
        new File("krb5-localkdc.conf").deleteOnExit();
        System.setProperty("java.security.krb5.conf", "krb5-localkdc.conf");
    }
    static void xRealmAuth() throws Exception {
        Security.setProperty("auth.login.defaultCallbackHandler", "CrossRealm");
        System.setProperty("java.security.auth.login.config", "jaas-localkdc.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        new File("jaas-localkdc.conf").deleteOnExit();
        FileOutputStream fos = new FileOutputStream("jaas-localkdc.conf");
        fos.write(("com.sun.security.jgss.krb5.initiate {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule\n" +
                "    required\n" +
                "    principal=dummy\n" +
                "    doNotPrompt=false\n" +
                "    useTicketCache=false\n" +
                "    ;\n" +
                "};").getBytes());
        fos.close();
        GSSManager m = GSSManager.getInstance();
        m.createContext(
                m.createName("host@www.snake.hole", GSSName.NT_HOSTBASED_SERVICE),
                GSSUtil.GSS_KRB5_MECH_OID,
                null,
                GSSContext.DEFAULT_LIFETIME).initSecContext(new byte[0], 0, 0);
    }
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName("dummy");
            }
            if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword("bogus".toCharArray());
            }
        }
    }
}
