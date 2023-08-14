public class OkAsDelegateXRealm implements CallbackHandler {
    public static void main(String[] args)
            throws Exception {
        KDC kdc1 = KDC.create("R1");
        kdc1.setPolicy("ok-as-delegate",
                System.getProperty("test.kdc.policy.ok-as-delegate"));
        kdc1.addPrincipal("dummy", "bogus".toCharArray());
        kdc1.addPrincipalRandKey("krbtgt/R1");
        kdc1.addPrincipal("krbtgt/R2@R1", "r1->r2".toCharArray());
        KDC kdc2 = KDC.create("R2");
        kdc2.setPolicy("ok-as-delegate",
                System.getProperty("test.kdc.policy.ok-as-delegate"));
        kdc2.addPrincipalRandKey("krbtgt/R2");
        kdc2.addPrincipal("krbtgt/R2@R1", "r1->r2".toCharArray());
        kdc2.addPrincipal("krbtgt/R3@R2", "r2->r3".toCharArray());
        KDC kdc3 = KDC.create("R3");
        kdc3.setPolicy("ok-as-delegate",
                System.getProperty("test.kdc.policy.ok-as-delegate"));
        kdc3.addPrincipalRandKey("krbtgt/R3");
        kdc3.addPrincipal("krbtgt/R3@R2", "r2->r3".toCharArray());
        kdc3.addPrincipalRandKey("host/host.r3.local");
        KDC.saveConfig("krb5-localkdc.conf", kdc1, kdc2, kdc3,
                "forwardable=true",
                "[capaths]",
                "R1 = {",
                "    R2 = .",
                "    R3 = R2",
                "}",
                "[domain_realm]",
                ".r3.local=R3"
                );
        System.setProperty("java.security.krb5.conf", "krb5-localkdc.conf");
        kdc3.writeKtab("localkdc.ktab");
        FileOutputStream fos = new FileOutputStream("jaas-localkdc.conf");
        fos.write(("com.sun.security.jgss.krb5.initiate {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule\n" +
                "    required\n" +
                "    principal=dummy\n" +
                "    doNotPrompt=false\n" +
                "    useTicketCache=false\n" +
                "    ;\n};\n" +
                "com.sun.security.jgss.krb5.accept {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required\n" +
                "    principal=\"host/host.r3.local@R3\"\n" +
                "    useKeyTab=true\n" +
                "    keyTab=localkdc.ktab\n" +
                "    isInitiator=false\n" +
                "    storeKey=true;\n};\n" +
                "\n").getBytes());
        fos.close();
        Security.setProperty("auth.login.defaultCallbackHandler",
                "OkAsDelegateXRealm");
        System.setProperty("java.security.auth.login.config", "jaas-localkdc.conf");
        new File("krb5-localkdc.conf").deleteOnExit();
        new File("localkdc.ktab").deleteOnExit();
        new File("jaas-localkdc.conf").deleteOnExit();
        Config.refresh();
        Context c = Context.fromJAAS("com.sun.security.jgss.krb5.initiate");
        Context s = Context.fromJAAS("com.sun.security.jgss.krb5.accept");
        for (int i=0; i<2; i++) {
            c.startAsClient("host@host.r3.local", GSSUtil.GSS_KRB5_MECH_OID);
            s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
            c.x().requestDelegPolicy(true);
            Context.handshake(c, s);
            boolean succeed = true;
            try {
                s.x().getDelegCred();
            } catch (GSSException gsse) {
                succeed = false;
            }
            if (succeed != Boolean.parseBoolean(args[0])) {
                throw new Exception("Test fail at round #" + i);
            }
        }
    }
    @Override
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
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
