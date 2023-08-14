public class PolicyUtil {
    private static final String P11KEYSTORE = "PKCS11";
    private static final String NONE = "NONE";
    public static InputStream getInputStream(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            String path = url.getFile().replace('/', File.separatorChar);
            path = ParseUtil.decode(path);
            return new FileInputStream(path);
        } else {
            return url.openStream();
        }
    }
    public static KeyStore getKeyStore
                (URL policyUrl,                 
                String keyStoreName,            
                String keyStoreType,            
                String keyStoreProvider,        
                String storePassURL,            
                Debug debug)
        throws KeyStoreException, MalformedURLException, IOException,
                NoSuchProviderException, NoSuchAlgorithmException,
                java.security.cert.CertificateException {
        if (keyStoreName == null) {
            throw new IllegalArgumentException("null KeyStore name");
        }
        char[] keyStorePassword = null;
        try {
            KeyStore ks;
            if (keyStoreType == null) {
                keyStoreType = KeyStore.getDefaultType();
            }
            if (P11KEYSTORE.equalsIgnoreCase(keyStoreType) &&
                !NONE.equals(keyStoreName)) {
                throw new IllegalArgumentException
                        ("Invalid value (" +
                        keyStoreName +
                        ") for keystore URL.  If the keystore type is \"" +
                        P11KEYSTORE +
                        "\", the keystore url must be \"" +
                        NONE +
                        "\"");
            }
            if (keyStoreProvider != null) {
                ks = KeyStore.getInstance(keyStoreType, keyStoreProvider);
            } else {
                ks = KeyStore.getInstance(keyStoreType);
            }
            if (storePassURL != null) {
                URL passURL;
                try {
                    passURL = new URL(storePassURL);
                } catch (MalformedURLException e) {
                    if (policyUrl == null) {
                        throw e;
                    }
                    passURL = new URL(policyUrl, storePassURL);
                }
                if (debug != null) {
                    debug.println("reading password"+passURL);
                }
                InputStream in = null;
                try {
                    in = passURL.openStream();
                    keyStorePassword = Password.readPassword(in);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            if (NONE.equals(keyStoreName)) {
                ks.load(null, keyStorePassword);
                return ks;
            } else {
                URL keyStoreUrl = null;
                try {
                    keyStoreUrl = new URL(keyStoreName);
                } catch (MalformedURLException e) {
                    if (policyUrl == null) {
                        throw e;
                    }
                    keyStoreUrl = new URL(policyUrl, keyStoreName);
                }
                if (debug != null) {
                    debug.println("reading keystore"+keyStoreUrl);
                }
                InputStream inStream = null;
                try {
                    inStream =
                        new BufferedInputStream(getInputStream(keyStoreUrl));
                    ks.load(inStream, keyStorePassword);
                } finally {
                    inStream.close();
                }
                return ks;
            }
        } finally {
            if (keyStorePassword != null) {
                Arrays.fill(keyStorePassword, ' ');
            }
        }
    }
}
