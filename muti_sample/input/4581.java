public final class CertAndKeyGen {
    public CertAndKeyGen (String keyType, String sigAlg)
    throws NoSuchAlgorithmException
    {
        keyGen = KeyPairGenerator.getInstance(keyType);
        this.sigAlg = sigAlg;
    }
    public CertAndKeyGen (String keyType, String sigAlg, String providerName)
    throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (providerName == null) {
            keyGen = KeyPairGenerator.getInstance(keyType);
        } else {
            try {
                keyGen = KeyPairGenerator.getInstance(keyType, providerName);
            } catch (Exception e) {
                keyGen = KeyPairGenerator.getInstance(keyType);
            }
        }
        this.sigAlg = sigAlg;
    }
    public void         setRandom (SecureRandom generator)
    {
        prng = generator;
    }
    public void generate (int keyBits)
    throws InvalidKeyException
    {
        KeyPair pair;
        try {
            if (prng == null) {
                prng = new SecureRandom();
            }
            keyGen.initialize(keyBits, prng);
            pair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
    }
    public X509Key getPublicKey()
    {
        if (!(publicKey instanceof X509Key)) {
            return null;
        }
        return (X509Key)publicKey;
    }
    public PrivateKey getPrivateKey ()
    {
        return privateKey;
    }
    public X509Certificate getSelfCertificate (
            X500Name myname, Date firstDate, long validity)
    throws CertificateException, InvalidKeyException, SignatureException,
        NoSuchAlgorithmException, NoSuchProviderException
    {
        X509CertImpl    cert;
        Date            lastDate;
        try {
            lastDate = new Date ();
            lastDate.setTime (firstDate.getTime () + validity * 1000);
            CertificateValidity interval =
                                   new CertificateValidity(firstDate,lastDate);
            X509CertInfo info = new X509CertInfo();
            info.set(X509CertInfo.VERSION,
                     new CertificateVersion(CertificateVersion.V3));
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                    new java.util.Random().nextInt() & 0x7fffffff));
            AlgorithmId algID = AlgorithmId.getAlgorithmId(sigAlg);
            info.set(X509CertInfo.ALGORITHM_ID,
                     new CertificateAlgorithmId(algID));
            info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(myname));
            info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.ISSUER, new CertificateIssuerName(myname));
            cert = new X509CertImpl(info);
            cert.sign(privateKey, this.sigAlg);
            return (X509Certificate)cert;
        } catch (IOException e) {
             throw new CertificateEncodingException("getSelfCert: " +
                                                    e.getMessage());
        }
    }
    public X509Certificate getSelfCertificate (X500Name myname, long validity)
    throws CertificateException, InvalidKeyException, SignatureException,
        NoSuchAlgorithmException, NoSuchProviderException
    {
        return getSelfCertificate(myname, new Date(), validity);
    }
    public PKCS10 getCertRequest (X500Name myname)
    throws InvalidKeyException, SignatureException
    {
        PKCS10  req = new PKCS10 (publicKey);
        try {
            Signature signature = Signature.getInstance(sigAlg);
            signature.initSign (privateKey);
            req.encodeAndSign(myname, signature);
        } catch (CertificateException e) {
            throw new SignatureException (sigAlg + " CertificateException");
        } catch (IOException e) {
            throw new SignatureException (sigAlg + " IOException");
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException (sigAlg + " unavailable?");
        }
        return req;
    }
    private SecureRandom        prng;
    private String              sigAlg;
    private KeyPairGenerator    keyGen;
    private PublicKey           publicKey;
    private PrivateKey          privateKey;
}
