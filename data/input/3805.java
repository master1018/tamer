public class TokenStore {
    private static final String POLICY_NO_STORE =
        "grant { permission java.security.AllPermission; };";
    private static final String POLICY_URL =
        "keystore \"file:${test.src}${/}TokenStore.keystore\";"         +
        "grant signedby \"POLICY_URL\" {"                               +
        "    permission java.security.AllPermission;"                   +
        "};"                                                            ;
    private static final String POLICY_URL_T =
        "keystore \"file:${test.src}${/}TokenStore.keystore\", \"JKS\";"+
        "grant signedby \"POLICY_URL_T\" {"                             +
        "    permission java.security.AllPermission;"                   +
        "};"                                                            ;
    private static final String POLICY_URL_T_P =
        "keystore \"file:${test.src}${/}TokenStore.keystore\","         +
        "               \"JKS\", \"SUN\";"                              +
        "grant signedby \"POLICY_URL_T_P\" {"                           +
        "    permission java.security.AllPermission;"                   +
        "};"                                                            ;
    private static final String POLICY_URL_PWD =
        "keystore \"file:${test.src}${/}TokenStore.keystore\";"         +
        "keystorePasswordURL \"file:${test.src}${/}TokenStore.pwd\";"   +
        "grant signedby \"POLICY_URL\" {"                               +
        "    permission java.security.AllPermission;"                   +
        "};"                                                            ;
    private static final String POLICY_URL_T_P_PWD =
        "keystore \"file:${test.src}${/}TokenStore.keystore\","         +
        "               \"JKS\", \"SUN\";"                              +
        "keystorePasswordURL \"file:${test.src}${/}TokenStore.pwd\";"   +
        "grant signedby \"POLICY_URL_T_P\" {"                           +
        "    permission java.security.AllPermission;"                   +
        "};"                                                            ;
    private static final String POLICY_PASS_NO_STORE =
        "keystorePasswordURL \"file:${test.src}${/}TokenStore.pwd\";"   +
        "grant signedby \"POLICY_URL_T_P\" {"                           +
        "    permission java.security.AllPermission;"                   +
        "};"                                                            ;
    public static void main(String[] args) throws Exception {
        PolicyParser p = new PolicyParser();
        p.read(new StringReader(POLICY_NO_STORE));
        doNoStore(p);
        StringWriter sw = new StringWriter();
        p.write(sw);
        PolicyParser newP = new PolicyParser();
        newP.read(new StringReader(sw.toString()));
        doNoStore(p);
        p = new PolicyParser();
        p.read(new StringReader(POLICY_URL));
        doURL(p, true);
        sw = new StringWriter();
        p.write(sw);
        newP = new PolicyParser();
        newP.read(new StringReader(sw.toString()));
        doURL(p, true);
        p = new PolicyParser();
        p.read(new StringReader(POLICY_URL_T));
        doURL_T(p, true);
        sw = new StringWriter();
        p.write(sw);
        newP = new PolicyParser();
        newP.read(new StringReader(sw.toString()));
        doURL_T(p, true);
        p = new PolicyParser();
        p.read(new StringReader(POLICY_URL_T_P));
        doURL_T_P(p, true);
        sw = new StringWriter();
        p.write(sw);
        newP = new PolicyParser();
        newP.read(new StringReader(sw.toString()));
        doURL_T_P(p, true);
        p = new PolicyParser();
        p.read(new StringReader(POLICY_URL_PWD));
        doURL(p, false);
        doPwd(p);
        sw = new StringWriter();
        p.write(sw);
        newP = new PolicyParser();
        newP.read(new StringReader(sw.toString()));
        doURL(p, false);
        doPwd(p);
        p = new PolicyParser();
        p.read(new StringReader(POLICY_URL_T_P_PWD));
        doURL_T_P(p, false);
        doPwd(p);
        sw = new StringWriter();
        p.write(sw);
        newP = new PolicyParser();
        newP.read(new StringReader(sw.toString()));
        doURL_T_P(p, false);
        doPwd(p);
        p = new PolicyParser();
        try {
            p.read(new StringReader(POLICY_PASS_NO_STORE));
            throw new SecurityException("expected parsing exception");
        } catch (PolicyParser.ParsingException pe) {
        }
    }
    private static void checkPerm(PolicyParser p) throws Exception {
        Enumeration e = p.grantElements();
        boolean foundOne = false;
        while (e.hasMoreElements()) {
            PolicyParser.GrantEntry ge = (PolicyParser.GrantEntry)
                                        e.nextElement();
            if (ge.permissionEntries == null) {
                throw new SecurityException("expected non-null perms");
            } else {
                foundOne = true;
            }
        }
        if (!foundOne) {
            throw new SecurityException("expected non-null grant entries");
        }
    }
    private static void doNoStore(PolicyParser p) throws Exception {
        if (p.getKeyStoreUrl() != null ||
            p.getKeyStoreType() != null ||
            p.getKeyStoreProvider() != null ||
            p.getStorePassURL() != null) {
            throw new SecurityException("expected null keystore");
        }
        checkPerm(p);
    }
    private static void doURL(PolicyParser p, boolean checkPwd)
                throws  Exception {
        if (p.getKeyStoreUrl() == null ||
            !(p.getKeyStoreUrl().endsWith("TokenStore.keystore")) ||
            p.getKeyStoreType() != null ||
            p.getKeyStoreProvider() != null) {
            throw new SecurityException("invalid keystore values");
        }
        if (checkPwd) {
            if (p.getStorePassURL() != null) {
                throw new SecurityException("invalid keystore values");
            }
        }
        checkPerm(p);
    }
    private static void doURL_T(PolicyParser p, boolean checkPwd)
                throws Exception {
        if (p.getKeyStoreUrl() == null ||
            !(p.getKeyStoreUrl().endsWith("TokenStore.keystore")) ||
            p.getKeyStoreType() == null ||
            !(p.getKeyStoreType().equals("JKS")) ||
            p.getKeyStoreProvider() != null) {
            throw new SecurityException("invalid keystore values");
        }
        if (checkPwd) {
            if (p.getStorePassURL() != null) {
                throw new SecurityException("invalid keystore values");
            }
        }
        checkPerm(p);
    }
    private static void doURL_T_P(PolicyParser p, boolean checkPwd)
                throws Exception {
        if (p.getKeyStoreUrl() == null ||
            !(p.getKeyStoreUrl().endsWith("TokenStore.keystore")) ||
            p.getKeyStoreType() == null ||
            !(p.getKeyStoreType().equals("JKS")) ||
            p.getKeyStoreProvider() == null ||
            !(p.getKeyStoreProvider().equals("SUN"))) {
            throw new SecurityException("invalid keystore values");
        }
        if (checkPwd) {
            if (p.getStorePassURL() != null) {
                throw new SecurityException("invalid keystore values");
            }
        }
        checkPerm(p);
    }
    private static void doPwd(PolicyParser p) throws Exception {
        if (p.getStorePassURL() == null ||
            !(p.getStorePassURL().endsWith("TokenStore.pwd"))) {
            throw new SecurityException("invalid password values");
        }
    }
}
