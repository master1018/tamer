public class SHA1withDSA_SignatureImpl extends Signature {
    private MessageDigest msgDigest;
    private DSAKey dsaKey;
    public SHA1withDSA_SignatureImpl() throws NoSuchAlgorithmException {
        super("SHA1withDSA"); 
        msgDigest = MessageDigest.getInstance("SHA1"); 
    }
    protected Object engineGetParameter(String param)
            throws InvalidParameterException {
        if (param == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        return null;
    }
    protected void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException {
        DSAParams params;
        BigInteger p, q, x;
        int n;
        if (privateKey == null || !(privateKey instanceof DSAPrivateKey)) {
            throw new InvalidKeyException(
                    Messages.getString("security.168")); 
        }
        params = ((DSAPrivateKey) privateKey).getParams();
        p = params.getP();
        q = params.getQ();
        x = ((DSAPrivateKey) privateKey).getX();
        n = p.bitLength();
        if (p.compareTo(BigInteger.valueOf(1)) != 1 || n < 512 || n > 1024
                || (n & 077) != 0) {
            throw new InvalidKeyException(Messages.getString("security.169")); 
        }
        if (q.signum() != 1 && q.bitLength() != 160) {
            throw new InvalidKeyException(Messages.getString("security.16A")); 
        }
        if (x.signum() != 1 || x.compareTo(q) != -1) {
            throw new InvalidKeyException(Messages.getString("security.16B")); 
        }
        dsaKey = (DSAKey) privateKey;
        msgDigest.reset();
    }
    protected void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException {
        BigInteger p, q, y;
        int n1;
        if (publicKey == null || !(publicKey instanceof DSAPublicKey)) {
            throw new InvalidKeyException(
                    Messages.getString("security.16C")); 
        }
        DSAParams params = ((DSAPublicKey) publicKey).getParams();
        p = params.getP();
        q = params.getQ();
        y = ((DSAPublicKey) publicKey).getY();
        n1 = p.bitLength();
        if (p.compareTo(BigInteger.valueOf(1)) != 1 || n1 < 512 || n1 > 1024
                || (n1 & 077) != 0) {
            throw new InvalidKeyException(Messages.getString("security.169")); 
        }
        if (q.signum() != 1 || q.bitLength() != 160) {
            throw new InvalidKeyException(Messages.getString("security.16A")); 
        }
        if (y.signum() != 1) {
            throw new InvalidKeyException(Messages.getString("security.16D")); 
        }
        dsaKey = (DSAKey) publicKey;
        msgDigest.reset();
    }
    protected void engineSetParameter(String param, Object value)
            throws InvalidParameterException {
        if (param == null) {
            throw new NullPointerException(Messages.getString("security.83", "param")); 
        }
        throw new InvalidParameterException(Messages.getString("security.16E")); 
    }
    protected byte[] engineSign() throws SignatureException {
        BigInteger r = null;
        BigInteger s = null;
        BigInteger k = null;
        BigInteger p, q, g, x;
        BigInteger digestBI;
        byte randomBytes[];
        byte rBytes[], sBytes[], signature[];
        int n, n1, n2;
        DSAParams params;
        if (appRandom == null) {
            appRandom = new SecureRandom();
        }
        params = dsaKey.getParams();
        p = params.getP();
        q = params.getQ();
        g = params.getG();
        x = ((DSAPrivateKey) dsaKey).getX();
        digestBI = new BigInteger(1, msgDigest.digest());
        randomBytes = new byte[20];
        for (;;) {
            appRandom.nextBytes(randomBytes);
            k = new BigInteger(1, randomBytes);
            if (k.compareTo(q) != -1) {
                continue;
            }
            r = g.modPow(k, p).mod(q);
            if (r.signum() == 0) {
                continue;
            }
            s = k.modInverse(q).multiply(digestBI.add(x.multiply(r)).mod(q))
                    .mod(q);
            if (s.signum() != 0) {
                break;
            }
        }
        rBytes = r.toByteArray();
        n1 = rBytes.length;
        if ((rBytes[0] & 0x80) != 0) {
            n1++;
        }
        sBytes = s.toByteArray();
        n2 = sBytes.length;
        if ((sBytes[0] & 0x80) != 0) {
            n2++;
        }
        signature = new byte[6 + n1 + n2]; 
        signature[0] = (byte) 0x30; 
        signature[1] = (byte) (4 + n1 + n2); 
        signature[2] = (byte) 0x02; 
        signature[3] = (byte) n1; 
        signature[4 + n1] = (byte) 0x02; 
        signature[5 + n1] = (byte) n2; 
        if (n1 == rBytes.length) {
            n = 4;
        } else {
            n = 5;
        }
        System.arraycopy(rBytes, 0, signature, n, rBytes.length);
        if (n2 == sBytes.length) {
            n = 6 + n1;
        } else {
            n = 7 + n1;
        }
        System.arraycopy(sBytes, 0, signature, n, sBytes.length);
        return signature;
    }
    protected void engineUpdate(byte b) throws SignatureException {
        msgDigest.update(b);
    }
    protected void engineUpdate(byte[] b, int off, int len)
            throws SignatureException {
        msgDigest.update(b, off, len);
    }
    private boolean checkSignature(byte[] sigBytes, int offset, int length)
            throws SignatureException {
        BigInteger r, s, w;
        BigInteger u1, u2, v;
        BigInteger p, q, g, y;
        DSAParams params;
        int n1, n2;
        byte bytes[];
        byte digest[];
        try {
            byte dummy;
            n1 = sigBytes[offset + 3];
            n2 = sigBytes[offset + n1 + 5];
            if (sigBytes[offset + 0] != 0x30 || sigBytes[offset + 2] != 2
                    || sigBytes[offset + n1 + 4] != 2
                    || sigBytes[offset + 1] != (n1 + n2 + 4) || n1 > 21
                    || n2 > 21
                    || (length != 0 && (sigBytes[offset + 1] + 2) > length)) {
                throw new SignatureException(Messages.getString("security.16F")); 
            }
            dummy = sigBytes[5 + n1 + n2]; 
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SignatureException(Messages.getString("security.170")); 
        }
        digest = msgDigest.digest();
        bytes = new byte[n1];
        System.arraycopy(sigBytes, offset + 4, bytes, 0, n1);
        r = new BigInteger(bytes);
        bytes = new byte[n2];
        System.arraycopy(sigBytes, offset + 6 + n1, bytes, 0, n2);
        s = new BigInteger(bytes);
        params = dsaKey.getParams();
        p = params.getP();
        q = params.getQ();
        g = params.getG();
        y = ((DSAPublicKey) dsaKey).getY();
        if (r.signum() != 1 || r.compareTo(q) != -1 || s.signum() != 1
                || s.compareTo(q) != -1) {
            return false;
        }
        w = s.modInverse(q);
        u1 = (new BigInteger(1, digest)).multiply(w).mod(q);
        u2 = r.multiply(w).mod(q);
        v = g.modPow(u1, p).multiply(y.modPow(u2, p)).mod(p).mod(q);
        if (v.compareTo(r) != 0) {
            return false;
        }
        return true;
    }
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        if (sigBytes == null) {
            throw new NullPointerException(Messages.getString("security.83", "sigBytes")); 
        }
        return checkSignature(sigBytes, 0, 0);
    }
    protected boolean engineVerify(byte[] sigBytes, int offset, int length)
            throws SignatureException {
        return checkSignature(sigBytes, offset, length);
    }
}
