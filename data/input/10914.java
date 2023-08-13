public class W83 {
    public static void main(String[] args) throws Exception {
        W83 x = new W83();
        KDC kdc = new KDC(OneKDC.REALM, "127.0.0.1", 0, true);
        kdc.addPrincipal(OneKDC.USER, OneKDC.PASS);
        kdc.addPrincipalRandKey("krbtgt/" + OneKDC.REALM);
        KDC.saveConfig(OneKDC.KRB5_CONF, kdc);
        System.setProperty("java.security.krb5.conf", OneKDC.KRB5_CONF);
        Config.refresh();
        kdc.writeKtab(OneKDC.KTAB);
        new File(OneKDC.KRB5_CONF).deleteOnExit();
        new File(OneKDC.KTAB).deleteOnExit();
        KeyTab ktab = KeyTab.getInstance(OneKDC.KTAB);
        for (int etype: EType.getBuiltInDefaults()) {
            if (etype != EncryptedData.ETYPE_ARCFOUR_HMAC) {
                ktab.deleteEntries(new PrincipalName(OneKDC.USER), etype, -1);
            }
        }
        ktab.save();
        if (System.getProperty("6932525") != null) {
            kdc.setOption(KDC.Option.ONLY_RC4_TGT, true);
        }
        if (System.getProperty("6959292") != null) {
            kdc.setOption(KDC.Option.RC4_FIRST_PREAUTH, true);
        }
        x.go();
    }
    void go() throws Exception {
        Krb5LoginModule krb5 = new Krb5LoginModule();
        StringBuffer error = new StringBuffer();
        try {
            Context.fromUserPass(OneKDC.USER, OneKDC.PASS, false);
        } catch (Exception e) {
            e.printStackTrace();
            error.append("Krb5LoginModule password login error\n");
        }
        try {
            Context.fromUserKtab(OneKDC.USER, OneKDC.KTAB, false);
        } catch (Exception e) {
            e.printStackTrace();
            error.append("Krb5LoginModule keytab login error\n");
        }
        try {
            Class.forName("sun.security.krb5.internal.tools.Kinit");
            String cmd = System.getProperty("java.home") +
                    System.getProperty("file.separator") +
                    "bin" +
                    System.getProperty("file.separator") +
                    "kinit";
            int p = execute(
                cmd,
                "-J-Djava.security.krb5.conf=" + OneKDC.KRB5_CONF,
                "-c", "cache1",
                OneKDC.USER,
                new String(OneKDC.PASS));
            if (p != 0) {
                error.append("kinit password login error\n");
            }
            p = execute(
                cmd,
                "-J-Djava.security.krb5.conf=" + OneKDC.KRB5_CONF,
                "-c", "cache2",
                "-k", "-t", OneKDC.KTAB,
                OneKDC.USER);
            if (p != 0) {
                error.append("kinit keytab login error\n");
            }
        } catch (ClassNotFoundException cnfe) {
            System.out.println("No kinit, test ignored.");
        }
        if (error.length() != 0) {
            throw new Exception(error.toString());
        }
    }
    private static int execute(String... args) throws Exception {
        for (String arg: args) {
            System.out.printf("%s ", arg);
        }
        System.out.println();
        Process p = Runtime.getRuntime().exec(args);
        return p.waitFor();
    }
}
