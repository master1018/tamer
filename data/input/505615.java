public class PKCS10CertificationRequest
    extends CertificationRequest
{
    private static Hashtable            algorithms = new Hashtable();
    private static Hashtable            keyAlgorithms = new Hashtable();
    private static Hashtable            oids = new Hashtable();
    private static Set                  noParams = new HashSet();
    static
    {
        algorithms.put("MD5WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("MD5WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("RSAWITHMD5", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("SHA1WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA1WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA224WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA224WITHRSA", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA256WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA256WITHRSA", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA384WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA384WITHRSA", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA512WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA512WITHRSA", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("RSAWITHSHA1", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("RIPEMD160WITHRSAENCRYPTION", new DERObjectIdentifier("1.3.36.3.3.1.2"));
        algorithms.put("RIPEMD160WITHRSA", new DERObjectIdentifier("1.3.36.3.3.1.2"));
        algorithms.put("SHA1WITHDSA", new DERObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("DSAWITHSHA1", new DERObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("SHA224WITHDSA", NISTObjectIdentifiers.dsa_with_sha224);
        algorithms.put("SHA256WITHDSA", NISTObjectIdentifiers.dsa_with_sha256);
        algorithms.put("SHA1WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("SHA224WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA224);
        algorithms.put("SHA256WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA256);
        algorithms.put("SHA384WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA384);
        algorithms.put("SHA512WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA512);
        algorithms.put("ECDSAWITHSHA1", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("GOST3411WITHGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3410WITHGOST3411", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHECGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        oids.put(new DERObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3411WITHGOST3410");
        oids.put(new DERObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
        oids.put(new DERObjectIdentifier("1.2.840.10040.4.3"), "SHA1WITHDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512WITHECDSA");
        oids.put(OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
        oids.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha224, "SHA224WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha256, "SHA256WITHDSA");
        keyAlgorithms.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        keyAlgorithms.put(X9ObjectIdentifiers.id_dsa, "DSA");
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA1);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA224);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA256);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA384);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA512);
        noParams.add(X9ObjectIdentifiers.id_dsa_with_sha1);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha224);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha256);
    }
    private static ASN1Sequence toDERSequence(
        byte[]  bytes)
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(bytes);
            ASN1InputStream         dIn = new ASN1InputStream(bIn);
            return (ASN1Sequence)dIn.readObject();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("badly encoded request");
        }
    }
    public PKCS10CertificationRequest(
        byte[]  bytes)
    {
        super(toDERSequence(bytes));
    }
    public PKCS10CertificationRequest(
        ASN1Sequence  sequence)
    {
        super(sequence);
    }
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X509Name            subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        this(signatureAlgorithm, subject, key, attributes, signingKey, "BC");
    }
    private static X509Name convertName(
        X500Principal    name)
    {
        try
        {
            return new X509Principal(name.getEncoded());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't convert name");
        }
    }
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X500Principal       subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        this(signatureAlgorithm, convertName(subject), key, attributes, signingKey, "BC");
    }
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X500Principal       subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        this(signatureAlgorithm, convertName(subject), key, attributes, signingKey, provider);
    }
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X509Name            subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        DERObjectIdentifier sigOID = (DERObjectIdentifier)algorithms.get(Strings.toUpperCase(signatureAlgorithm));
        if (sigOID == null)
        {
            throw new IllegalArgumentException("Unknown signature type requested");
        }
        if (subject == null)
        {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (key == null)
        {
            throw new IllegalArgumentException("public key must not be null");
        }
        if (noParams.contains(sigOID))
        {
            this.sigAlgId = new AlgorithmIdentifier(sigOID);
        }
        else
        {
            this.sigAlgId = new AlgorithmIdentifier(sigOID, null);
        }
        byte[]                  bytes = key.getEncoded();
        ByteArrayInputStream    bIn = new ByteArrayInputStream(bytes);
        ASN1InputStream         dIn = new ASN1InputStream(bIn);
        try
        {
            this.reqInfo = new CertificationRequestInfo(subject, new SubjectPublicKeyInfo((ASN1Sequence)dIn.readObject()), attributes);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't encode public key");
        }
        Signature sig = null;
        try
        {
            sig = Signature.getInstance(sigAlgId.getObjectId().getId(), provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            sig = Signature.getInstance(signatureAlgorithm, provider);
        }
        sig.initSign(signingKey);
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(reqInfo);
            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding TBS cert request - " + e);
        }
        this.sigBits = new DERBitString(sig.sign());
    }
    public PublicKey getPublicKey()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException
    {
        return getPublicKey("BC");
    }
    public PublicKey getPublicKey(
        String  provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException
    {
        SubjectPublicKeyInfo    subjectPKInfo = reqInfo.getSubjectPublicKeyInfo();
        X509EncodedKeySpec      xspec = new X509EncodedKeySpec(new DERBitString(subjectPKInfo).getBytes());
        AlgorithmIdentifier     keyAlg = subjectPKInfo.getAlgorithmId();
        try
        {
            try
            {
                return KeyFactory.getInstance(keyAlg.getObjectId().getId(), provider).generatePublic(xspec);
            }
            catch (NoSuchAlgorithmException e)
            {
                if (keyAlgorithms.get(keyAlg.getObjectId()) != null)
                {
                    String  keyAlgorithm = (String)keyAlgorithms.get(keyAlg.getObjectId());
                    return KeyFactory.getInstance(keyAlgorithm, provider).generatePublic(xspec);
                }
                throw e;
            }
        }
        catch (InvalidKeySpecException e)
        {
            throw new InvalidKeyException("error decoding public key");
        }
    }
    public boolean verify()
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        return verify("BC");
    }
    public boolean verify(
        String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        Signature   sig = null;
        try
        {
            sig = Signature.getInstance(sigAlgId.getObjectId().getId(), provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            if (oids.get(sigAlgId.getObjectId()) != null)
            {
                String  signatureAlgorithm = (String)oids.get(sigAlgId.getObjectId());
                sig = Signature.getInstance(signatureAlgorithm, provider);
            }
            else
            {
                throw e;
            }
        }
        sig.initVerify(this.getPublicKey(provider));
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(reqInfo);
            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding TBS cert request - " + e);
        }
        return sig.verify(sigBits.getBytes());
    }
    public byte[] getEncoded()
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        try
        {
            dOut.writeObject(this);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.toString());
        }
        return bOut.toByteArray();
    }
}
