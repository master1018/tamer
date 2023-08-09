abstract class CramMD5Base {
    protected boolean completed = false;
    protected boolean aborted = false;
    protected byte[] pw;
    protected CramMD5Base() {
        initLogger();
    }
    public String getMechanismName() {
        return "CRAM-MD5";
    }
    public boolean isComplete() {
        return completed;
    }
    public byte[] unwrap(byte[] incoming, int offset, int len)
        throws SaslException {
        if (completed) {
            throw new IllegalStateException(
                "CRAM-MD5 supports neither integrity nor privacy");
        } else {
            throw new IllegalStateException(
                "CRAM-MD5 authentication not completed");
        }
    }
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        if (completed) {
            throw new IllegalStateException(
                "CRAM-MD5 supports neither integrity nor privacy");
        } else {
            throw new IllegalStateException(
                "CRAM-MD5 authentication not completed");
        }
    }
    public Object getNegotiatedProperty(String propName) {
        if (completed) {
            if (propName.equals(Sasl.QOP)) {
                return "auth";
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException(
                "CRAM-MD5 authentication not completed");
        }
    }
    public void dispose() throws SaslException {
        clearPassword();
    }
    protected void clearPassword() {
        if (pw != null) {
            for (int i = 0; i < pw.length; i++) {
                pw[i] = (byte)0;
            }
            pw = null;
        }
    }
    protected void finalize() {
        clearPassword();
    }
    static private final int MD5_BLOCKSIZE = 64;
    final static String HMAC_MD5(byte[] key, byte[] text)
        throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        if (key.length > 64) {
            key = md5.digest(key);
        }
        byte[] ipad = new byte[MD5_BLOCKSIZE];  
        byte[] opad = new byte[MD5_BLOCKSIZE];  
        byte[] digest;
        int i;
        for (i = 0; i < MD5_BLOCKSIZE; i++) {
            for ( ; i < key.length; i++) {
                ipad[i] = key[i];
                opad[i] = key[i];
            }
            ipad[i] = 0x00;
            opad[i] = 0x00;
        }
        for (i = 0; i < MD5_BLOCKSIZE; i++) {
            ipad[i] ^= 0x36;
            opad[i] ^= 0x5c;
        }
        md5.update(ipad);
        md5.update(text);
        digest = md5.digest();
        md5.update(opad);
        md5.update(digest);
        digest = md5.digest();
        StringBuffer digestString = new StringBuffer();
        for (i = 0; i < digest.length; i++) {
            if ((digest[i] & 0x000000ff) < 0x10) {
                digestString.append("0" +
                    Integer.toHexString(digest[i] & 0x000000ff));
            } else {
                digestString.append(
                    Integer.toHexString(digest[i] & 0x000000ff));
            }
        }
        return (digestString.toString());
    }
    private static synchronized void initLogger() {
        if (logger == null) {
            logger = Logger.getLogger(SASL_LOGGER_NAME);
        }
    }
    private static final String SASL_LOGGER_NAME = "javax.security.sasl";
    protected static Logger logger;  
}
