public class ValidationTests {
    private static SignatureValidator validator;
    private final static String DIR = System.getProperty("test.src", ".");
    private final static String DATA_DIR =
        DIR + System.getProperty("file.separator") + "data";
    private final static String KEYSTORE =
        DATA_DIR + System.getProperty("file.separator") + "certs" +
        System.getProperty("file.separator") + "xmldsig.jks";
    private final static String STYLESHEET =
        "http:
    private final static String STYLESHEET_B64 =
        "http:
    private final static String[] FILES = {
        "signature-enveloped-dsa.xml",
        "signature-enveloping-b64-dsa.xml",
        "signature-enveloping-dsa.xml",
        "signature-enveloping-rsa.xml",
        "signature-enveloping-hmac-sha1.xml",
        "signature-external-dsa.xml",
        "signature-external-b64-dsa.xml",
        "signature-retrievalmethod-rawx509crt.xml",
        "signature-keyname.xml",
        "signature-x509-crt-crl.xml",
        "signature-x509-crt.xml",
        "signature-x509-is.xml",
        "signature-x509-ski.xml",
        "signature-x509-sn.xml",
        "exc-signature.xml",
        "sign-spec.xml"
    };
    static KeySelector skks;
    static {
        try {
            skks =
                new KeySelectors.SecretKeySelector("secret".getBytes("ASCII"));
        } catch (Exception e) {
        }
    }
    private final static KeySelector SKKS = skks;
    private final static KeySelector KVKS =
        new KeySelectors.KeyValueKeySelector();
    private final static KeySelector CKS =
        new KeySelectors.CollectionKeySelector(new File(DATA_DIR));
    private final static KeySelector RXKS =
        new KeySelectors.RawX509KeySelector();
    private final static KeySelector XKS = null;
    private final static KeySelector[] KEY_SELECTORS = {
        KVKS,
        KVKS,
        KVKS,
        KVKS,
        SKKS,
        KVKS,
        KVKS,
        CKS,
        CKS,
        RXKS,
        RXKS,
        CKS,
        CKS,
        CKS,
        KVKS,
        RXKS
    };
    private static URIDereferencer httpUd = null;
    public static void main(String args[]) throws Exception {
        httpUd = new HttpURIDereferencer();
        validator = new SignatureValidator(new File(DATA_DIR));
        boolean atLeastOneFailed = false;
        for (int i=0; i < FILES.length; i++) {
            System.out.println("Validating " + FILES[i]);
            if (test_signature(FILES[i], KEY_SELECTORS[i])) {
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                atLeastOneFailed = true;
            }
        }
        System.out.println("Validating sign-spec.xml with caching enabled");
        if (test_signature("sign-spec.xml", RXKS, true)) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED");
            atLeastOneFailed = true;
        }
        System.out.println("Validating signature-enveloping-hmac-sha1-40.xml");
        try {
            test_signature("signature-enveloping-hmac-sha1-40.xml", SKKS, false);
            System.out.println("FAILED");
            atLeastOneFailed = true;
        } catch (XMLSignatureException xse) {
            System.out.println(xse.getMessage());
            System.out.println("PASSED");
        }
        System.out.println("Validating signature-enveloping-hmac-sha1-trunclen-0-attack.xml");
        try {
            test_signature("signature-enveloping-hmac-sha1-trunclen-0-attack.xml", SKKS, false);
            System.out.println("FAILED");
            atLeastOneFailed = true;
        } catch (XMLSignatureException xse) {
            System.out.println(xse.getMessage());
            System.out.println("PASSED");
        }
        System.out.println("Validating signature-enveloping-hmac-sha1-trunclen-8-attack.xml");
        try {
            test_signature("signature-enveloping-hmac-sha1-trunclen-8-attack.xml", SKKS, false);
            System.out.println("FAILED");
            atLeastOneFailed = true;
        } catch (XMLSignatureException xse) {
            System.out.println(xse.getMessage());
            System.out.println("PASSED");
        }
        if (atLeastOneFailed) {
            throw new Exception
                ("At least one signature did not validate as expected");
        }
    }
    public static boolean test_signature(String file, KeySelector ks)
        throws Exception {
        return test_signature(file, ks, false);
    }
    public static boolean test_signature(String file, KeySelector ks,
        boolean cache) throws Exception {
        if (ks == null) {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load
                (new FileInputStream(KEYSTORE), "changeit".toCharArray());
            ks = new X509KeySelector(keystore, false);
        }
        return validator.validate(file, ks, httpUd, cache);
    }
    private static class HttpURIDereferencer implements URIDereferencer {
        private URIDereferencer defaultUd;
        HttpURIDereferencer() {
            defaultUd = XMLSignatureFactory.getInstance().getURIDereferencer();
        }
        public Data dereference(final URIReference ref, XMLCryptoContext ctx)
        throws URIReferenceException {
            String uri = ref.getURI();
            if (uri.equals(STYLESHEET) || uri.equals(STYLESHEET_B64)) {
                try {
                    FileInputStream fis = new FileInputStream(new File
                        (DATA_DIR, uri.substring(uri.lastIndexOf('/'))));
                    return new OctetStreamData(fis,ref.getURI(),ref.getType());
                } catch (Exception e) { throw new URIReferenceException(e); }
            }
            return defaultUd.dereference(ref, ctx);
        }
    }
}
