final class HandshakeHash {
    private int version = -1;
    private ByteArrayOutputStream data = new ByteArrayOutputStream();
    private final boolean isServer;
    private MessageDigest md5, sha;
    private final int clonesNeeded;    
    private boolean cvAlgDetermined = false;
    private String cvAlg;
    private MessageDigest finMD;
    HandshakeHash(boolean isServer, boolean needCertificateVerify,
            Set<String> algs) {
        this.isServer = isServer;
        clonesNeeded = needCertificateVerify ? 3 : 2;
    }
    void update(byte[] b, int offset, int len) {
        switch (version) {
            case 1:
                md5.update(b, offset, len);
                sha.update(b, offset, len);
                break;
            default:
                if (finMD != null) {
                    finMD.update(b, offset, len);
                }
                data.write(b, offset, len);
                break;
        }
    }
    void reset() {
        if (version != -1) {
            throw new RuntimeException(
                    "reset() can be only be called before protocolDetermined");
        }
        data.reset();
    }
    void protocolDetermined(ProtocolVersion pv) {
        if (version != -1) return;
        version = pv.compareTo(ProtocolVersion.TLS12) >= 0 ? 2 : 1;
        switch (version) {
            case 1:
                try {
                    md5 = CloneableDigest.getDigest("MD5", clonesNeeded);
                    sha = CloneableDigest.getDigest("SHA", clonesNeeded);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException
                                ("Algorithm MD5 or SHA not available", e);
                }
                byte[] bytes = data.toByteArray();
                update(bytes, 0, bytes.length);
                break;
            case 2:
                break;
        }
    }
    MessageDigest getMD5Clone() {
        if (version != 1) {
            throw new RuntimeException(
                    "getMD5Clone() can be only be called for TLS 1.1");
        }
        return cloneDigest(md5);
    }
    MessageDigest getSHAClone() {
        if (version != 1) {
            throw new RuntimeException(
                    "getSHAClone() can be only be called for TLS 1.1");
        }
        return cloneDigest(sha);
    }
    private static MessageDigest cloneDigest(MessageDigest digest) {
        try {
            return (MessageDigest)digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone digest", e);
        }
    }
    private static String normalizeAlgName(String alg) {
        alg = alg.toUpperCase(Locale.US);
        if (alg.startsWith("SHA")) {
            if (alg.length() == 3) {
                return "SHA-1";
            }
            if (alg.charAt(3) != '-') {
                return "SHA-" + alg.substring(3);
            }
        }
        return alg;
    }
    void setFinishedAlg(String s) {
        if (s == null) {
            throw new RuntimeException(
                    "setFinishedAlg's argument cannot be null");
        }
        if (finMD != null) return;
        try {
            finMD = CloneableDigest.getDigest(normalizeAlgName(s), 2);
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        finMD.update(data.toByteArray());
    }
    void restrictCertificateVerifyAlgs(Set<String> algs) {
        if (version == 1) {
            throw new RuntimeException(
                    "setCertificateVerifyAlg() cannot be called for TLS 1.1");
        }
    }
    void setCertificateVerifyAlg(String s) {
        if (cvAlgDetermined) return;
        cvAlg = s == null ? null : normalizeAlgName(s);
        cvAlgDetermined = true;
    }
    byte[] getAllHandshakeMessages() {
        return data.toByteArray();
    }
    byte[] getFinishedHash() {
        try {
            return cloneDigest(finMD).digest();
        } catch (Exception e) {
            throw new Error("BAD");
        }
    }
}
final class CloneableDigest extends MessageDigest implements Cloneable {
    private final MessageDigest[] digests;
    private CloneableDigest(MessageDigest digest, int n, String algorithm)
            throws NoSuchAlgorithmException {
        super(algorithm);
        digests = new MessageDigest[n];
        digests[0] = digest;
        for (int i = 1; i < n; i++) {
            digests[i] = JsseJce.getMessageDigest(algorithm);
        }
    }
    static MessageDigest getDigest(String algorithm, int n)
            throws NoSuchAlgorithmException {
        MessageDigest digest = JsseJce.getMessageDigest(algorithm);
        try {
            digest.clone();
            return digest;
        } catch (CloneNotSupportedException e) {
            return new CloneableDigest(digest, n, algorithm);
        }
    }
    private void checkState() {
    }
    protected int engineGetDigestLength() {
        checkState();
        return digests[0].getDigestLength();
    }
    protected void engineUpdate(byte b) {
        checkState();
        for (int i = 0; (i < digests.length) && (digests[i] != null); i++) {
            digests[i].update(b);
        }
    }
    protected void engineUpdate(byte[] b, int offset, int len) {
        checkState();
        for (int i = 0; (i < digests.length) && (digests[i] != null); i++) {
            digests[i].update(b, offset, len);
        }
    }
    protected byte[] engineDigest() {
        checkState();
        byte[] digest = digests[0].digest();
        digestReset();
        return digest;
    }
    protected int engineDigest(byte[] buf, int offset, int len)
            throws DigestException {
        checkState();
        int n = digests[0].digest(buf, offset, len);
        digestReset();
        return n;
    }
    private void digestReset() {
        for (int i = 1; (i < digests.length) && (digests[i] != null); i++) {
            digests[i].reset();
        }
    }
    protected void engineReset() {
        checkState();
        for (int i = 0; (i < digests.length) && (digests[i] != null); i++) {
            digests[i].reset();
        }
    }
    public Object clone() {
        checkState();
        for (int i = digests.length - 1; i >= 0; i--) {
            if (digests[i] != null) {
                MessageDigest digest = digests[i];
                digests[i] = null;
                return digest;
            }
        }
        throw new InternalError();
    }
}
