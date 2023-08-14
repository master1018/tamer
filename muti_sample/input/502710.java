public class KeyStoreTestSupport {
    public static final String srvKeyStore = "KeyStore";
    public static String[] validValues = { "bks", "BKS", "bKS", "Bks", "bKs",
            "BkS" };
    public static String defaultType = "bks";
    public static boolean JKSSupported = false;
    public static String defaultProviderName = null;
    public static Provider defaultProvider = null;
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType, srvKeyStore);
        JKSSupported = (defaultProvider != null);
        defaultProviderName = (JKSSupported ? defaultProvider.getName() : null);
    }
    public static class SKey implements SecretKey {
        private String type;
        private byte[] encoded;
        public SKey(String type, byte[] encoded) {
            this.type = type;
            this.encoded = encoded;
        }
        public String getAlgorithm() {
            return type;
        }
        public byte[] getEncoded() {
            return encoded;
        }
        public String getFormat() {
            return "test";
        }
    }
    public static class MyPrivateKey implements PrivateKey {
        private String algorithm;
        private String format;
        private byte[] encoded;
        public MyPrivateKey(String algorithm, String format, byte[] encoded) {
            this.algorithm = algorithm;
            this.format = format;
            this.encoded = encoded;
        }
        public String getAlgorithm() {
            return algorithm;
        }
        public String getFormat() {
            return format;
        }
        public byte[] getEncoded() {
            return encoded;
        }
    }
    public static class MCertificate extends Certificate {
        private final byte[] encoding;
        private final String type;
        public MCertificate(String type, byte[] encoding) {
            super(type);
            this.encoding = encoding;
            this.type = type;
        }
        public byte[] getEncoded() throws CertificateEncodingException {
            return encoding.clone();
        }
        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }
        public String toString() {
            return "[MCertificate, type: " + getType() + "]";
        }
        public PublicKey getPublicKey() {
            return new PublicKey() {
                public String getAlgorithm() {
                    return type;
                }
                public byte[] getEncoded() {
                    return encoding;
                }
                public String getFormat() {
                    return "test";
                }
            };
        }
    }
    public static class ProtPar implements KeyStore.ProtectionParameter {
    }
    public static class AnotherEntry implements KeyStore.Entry {
    }
}
