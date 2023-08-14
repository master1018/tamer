public class SignatureFileVerifier {
    private static final Debug debug = Debug.getInstance("jar");
    private ArrayList<CodeSigner[]> signerCache;
    private static final String ATTR_DIGEST =
        ("-DIGEST-" + ManifestDigester.MF_MAIN_ATTRS).toUpperCase
        (Locale.ENGLISH);
    private PKCS7 block;
    private byte sfBytes[];
    private String name;
    private ManifestDigester md;
    private HashMap<String, MessageDigest> createdDigests;
    private boolean workaround = false;
    private CertificateFactory certificateFactory = null;
    public SignatureFileVerifier(ArrayList<CodeSigner[]> signerCache,
                                 ManifestDigester md,
                                 String name,
                                 byte rawBytes[])
        throws IOException, CertificateException
    {
        Object obj = null;
        try {
            obj = Providers.startJarVerification();
            block = new PKCS7(rawBytes);
            sfBytes = block.getContentInfo().getData();
            certificateFactory = CertificateFactory.getInstance("X509");
        } finally {
            Providers.stopJarVerification(obj);
        }
        this.name = name.substring(0, name.lastIndexOf("."))
                                                   .toUpperCase(Locale.ENGLISH);
        this.md = md;
        this.signerCache = signerCache;
    }
    public boolean needSignatureFileBytes()
    {
        return sfBytes == null;
    }
    public boolean needSignatureFile(String name)
    {
        return this.name.equalsIgnoreCase(name);
    }
    public void setSignatureFile(byte sfBytes[])
    {
        this.sfBytes = sfBytes;
    }
    public static boolean isBlockOrSF(String s) {
        if (s.endsWith(".SF") || s.endsWith(".DSA") ||
                s.endsWith(".RSA") || s.endsWith(".EC")) {
            return true;
        }
        return false;
    }
    private MessageDigest getDigest(String algorithm)
    {
        if (createdDigests == null)
            createdDigests = new HashMap<String, MessageDigest>();
        MessageDigest digest = createdDigests.get(algorithm);
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance(algorithm);
                createdDigests.put(algorithm, digest);
            } catch (NoSuchAlgorithmException nsae) {
            }
        }
        return digest;
    }
    public void process(Hashtable<String, CodeSigner[]> signers,
            List manifestDigests)
        throws IOException, SignatureException, NoSuchAlgorithmException,
            JarException, CertificateException
    {
        Object obj = null;
        try {
            obj = Providers.startJarVerification();
            processImpl(signers, manifestDigests);
        } finally {
            Providers.stopJarVerification(obj);
        }
    }
    private void processImpl(Hashtable<String, CodeSigner[]> signers,
            List manifestDigests)
        throws IOException, SignatureException, NoSuchAlgorithmException,
            JarException, CertificateException
    {
        Manifest sf = new Manifest();
        sf.read(new ByteArrayInputStream(sfBytes));
        String version =
            sf.getMainAttributes().getValue(Attributes.Name.SIGNATURE_VERSION);
        if ((version == null) || !(version.equalsIgnoreCase("1.0"))) {
            return;
        }
        SignerInfo[] infos = block.verify(sfBytes);
        if (infos == null) {
            throw new SecurityException("cannot verify signature block file " +
                                        name);
        }
        BASE64Decoder decoder = new BASE64Decoder();
        CodeSigner[] newSigners = getSigners(infos, block);
        if (newSigners == null)
            return;
        Iterator<Map.Entry<String,Attributes>> entries =
                                sf.getEntries().entrySet().iterator();
        boolean manifestSigned = verifyManifestHash(sf, md, decoder, manifestDigests);
        if (!manifestSigned && !verifyManifestMainAttrs(sf, md, decoder)) {
            throw new SecurityException
                ("Invalid signature file digest for Manifest main attributes");
        }
        while(entries.hasNext()) {
            Map.Entry<String,Attributes> e = entries.next();
            String name = e.getKey();
            if (manifestSigned ||
                (verifySection(e.getValue(), name, md, decoder))) {
                if (name.startsWith("./"))
                    name = name.substring(2);
                if (name.startsWith("/"))
                    name = name.substring(1);
                updateSigners(newSigners, signers, name);
                if (debug != null) {
                    debug.println("processSignature signed name = "+name);
                }
            } else if (debug != null) {
                debug.println("processSignature unsigned name = "+name);
            }
        }
        updateSigners(newSigners, signers, JarFile.MANIFEST_NAME);
    }
    private boolean verifyManifestHash(Manifest sf,
                                       ManifestDigester md,
                                       BASE64Decoder decoder,
                                       List manifestDigests)
         throws IOException
    {
        Attributes mattr = sf.getMainAttributes();
        boolean manifestSigned = false;
        for (Map.Entry<Object,Object> se : mattr.entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST")) {
                String algorithm = key.substring(0, key.length()-16);
                manifestDigests.add(key);
                manifestDigests.add(se.getValue());
                MessageDigest digest = getDigest(algorithm);
                if (digest != null) {
                    byte[] computedHash = md.manifestDigest(digest);
                    byte[] expectedHash =
                        decoder.decodeBuffer((String)se.getValue());
                    if (debug != null) {
                     debug.println("Signature File: Manifest digest " +
                                          digest.getAlgorithm());
                     debug.println( "  sigfile  " + toHex(expectedHash));
                     debug.println( "  computed " + toHex(computedHash));
                     debug.println();
                    }
                    if (MessageDigest.isEqual(computedHash,
                                              expectedHash)) {
                        manifestSigned = true;
                    } else {
                    }
                }
            }
        }
        return manifestSigned;
    }
    private boolean verifyManifestMainAttrs(Manifest sf,
                                        ManifestDigester md,
                                        BASE64Decoder decoder)
         throws IOException
    {
        Attributes mattr = sf.getMainAttributes();
        boolean attrsVerified = true;
        for (Map.Entry<Object,Object> se : mattr.entrySet()) {
            String key = se.getKey().toString();
            if (key.toUpperCase(Locale.ENGLISH).endsWith(ATTR_DIGEST)) {
                String algorithm =
                        key.substring(0, key.length() - ATTR_DIGEST.length());
                MessageDigest digest = getDigest(algorithm);
                if (digest != null) {
                    ManifestDigester.Entry mde =
                        md.get(ManifestDigester.MF_MAIN_ATTRS, false);
                    byte[] computedHash = mde.digest(digest);
                    byte[] expectedHash =
                        decoder.decodeBuffer((String)se.getValue());
                    if (debug != null) {
                     debug.println("Signature File: " +
                                        "Manifest Main Attributes digest " +
                                        digest.getAlgorithm());
                     debug.println( "  sigfile  " + toHex(expectedHash));
                     debug.println( "  computed " + toHex(computedHash));
                     debug.println();
                    }
                    if (MessageDigest.isEqual(computedHash,
                                              expectedHash)) {
                    } else {
                        attrsVerified = false;
                        if (debug != null) {
                            debug.println("Verification of " +
                                        "Manifest main attributes failed");
                            debug.println();
                        }
                        break;
                    }
                }
            }
        }
        return attrsVerified;
    }
    private boolean verifySection(Attributes sfAttr,
                                  String name,
                                  ManifestDigester md,
                                  BASE64Decoder decoder)
         throws IOException
    {
        boolean oneDigestVerified = false;
        ManifestDigester.Entry mde = md.get(name,block.isOldStyle());
        if (mde == null) {
            throw new SecurityException(
                  "no manifiest section for signature file entry "+name);
        }
        if (sfAttr != null) {
            for (Map.Entry<Object,Object> se : sfAttr.entrySet()) {
                String key = se.getKey().toString();
                if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                    String algorithm = key.substring(0, key.length()-7);
                    MessageDigest digest = getDigest(algorithm);
                    if (digest != null) {
                        boolean ok = false;
                        byte[] expected =
                            decoder.decodeBuffer((String)se.getValue());
                        byte[] computed;
                        if (workaround) {
                            computed = mde.digestWorkaround(digest);
                        } else {
                            computed = mde.digest(digest);
                        }
                        if (debug != null) {
                          debug.println("Signature Block File: " +
                                   name + " digest=" + digest.getAlgorithm());
                          debug.println("  expected " + toHex(expected));
                          debug.println("  computed " + toHex(computed));
                          debug.println();
                        }
                        if (MessageDigest.isEqual(computed, expected)) {
                            oneDigestVerified = true;
                            ok = true;
                        } else {
                            if (!workaround) {
                               computed = mde.digestWorkaround(digest);
                               if (MessageDigest.isEqual(computed, expected)) {
                                   if (debug != null) {
                                       debug.println("  re-computed " + toHex(computed));
                                       debug.println();
                                   }
                                   workaround = true;
                                   oneDigestVerified = true;
                                   ok = true;
                               }
                            }
                        }
                        if (!ok){
                            throw new SecurityException("invalid " +
                                       digest.getAlgorithm() +
                                       " signature file digest for " + name);
                        }
                    }
                }
            }
        }
        return oneDigestVerified;
    }
    private CodeSigner[] getSigners(SignerInfo infos[], PKCS7 block)
        throws IOException, NoSuchAlgorithmException, SignatureException,
            CertificateException {
        ArrayList<CodeSigner> signers = null;
        for (int i = 0; i < infos.length; i++) {
            SignerInfo info = infos[i];
            ArrayList<X509Certificate> chain = info.getCertificateChain(block);
            CertPath certChain = certificateFactory.generateCertPath(chain);
            if (signers == null) {
                signers = new ArrayList<CodeSigner>();
            }
            signers.add(new CodeSigner(certChain, getTimestamp(info)));
            if (debug != null) {
                debug.println("Signature Block Certificate: " +
                    chain.get(0));
            }
        }
        if (signers != null) {
            return signers.toArray(new CodeSigner[signers.size()]);
        } else {
            return null;
        }
    }
    private Timestamp getTimestamp(SignerInfo info)
        throws IOException, NoSuchAlgorithmException, SignatureException,
            CertificateException {
        Timestamp timestamp = null;
        PKCS9Attributes unsignedAttrs = info.getUnauthenticatedAttributes();
        if (unsignedAttrs != null) {
            PKCS9Attribute timestampTokenAttr =
                unsignedAttrs.getAttribute("signatureTimestampToken");
            if (timestampTokenAttr != null) {
                PKCS7 timestampToken =
                    new PKCS7((byte[])timestampTokenAttr.getValue());
                byte[] encodedTimestampTokenInfo =
                    timestampToken.getContentInfo().getData();
                SignerInfo[] tsa =
                    timestampToken.verify(encodedTimestampTokenInfo);
                ArrayList<X509Certificate> chain =
                                tsa[0].getCertificateChain(timestampToken);
                CertPath tsaChain = certificateFactory.generateCertPath(chain);
                TimestampToken timestampTokenInfo =
                    new TimestampToken(encodedTimestampTokenInfo);
                timestamp =
                    new Timestamp(timestampTokenInfo.getDate(), tsaChain);
            }
        }
        return timestamp;
    }
    private static final char[] hexc =
            {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    static String toHex(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length*2);
        for (int i=0; i<data.length; i++) {
            sb.append(hexc[(data[i] >>4) & 0x0f]);
            sb.append(hexc[data[i] & 0x0f]);
        }
        return sb.toString();
    }
    static boolean contains(CodeSigner[] set, CodeSigner signer)
    {
        for (int i = 0; i < set.length; i++) {
            if (set[i].equals(signer))
                return true;
        }
        return false;
    }
    static boolean isSubSet(CodeSigner[] subset, CodeSigner[] set)
    {
        if (set == subset)
            return true;
        boolean match;
        for (int i = 0; i < subset.length; i++) {
            if (!contains(set, subset[i]))
                return false;
        }
        return true;
    }
    static boolean matches(CodeSigner[] signers, CodeSigner[] oldSigners,
        CodeSigner[] newSigners) {
        if ((oldSigners == null) && (signers == newSigners))
            return true;
        boolean match;
        if ((oldSigners != null) && !isSubSet(oldSigners, signers))
            return false;
        if (!isSubSet(newSigners, signers)) {
            return false;
        }
        for (int i = 0; i < signers.length; i++) {
            boolean found =
                ((oldSigners != null) && contains(oldSigners, signers[i])) ||
                contains(newSigners, signers[i]);
            if (!found)
                return false;
        }
        return true;
    }
    void updateSigners(CodeSigner[] newSigners,
        Hashtable<String, CodeSigner[]> signers, String name) {
        CodeSigner[] oldSigners = signers.get(name);
        CodeSigner[] cachedSigners;
        for (int i = signerCache.size() - 1; i != -1; i--) {
            cachedSigners = signerCache.get(i);
            if (matches(cachedSigners, oldSigners, newSigners)) {
                signers.put(name, cachedSigners);
                return;
            }
        }
        if (oldSigners == null) {
            cachedSigners = newSigners;
        } else {
            cachedSigners =
                new CodeSigner[oldSigners.length + newSigners.length];
            System.arraycopy(oldSigners, 0, cachedSigners, 0,
                oldSigners.length);
            System.arraycopy(newSigners, 0, cachedSigners, oldSigners.length,
                newSigners.length);
        }
        signerCache.add(cachedSigners);
        signers.put(name, cachedSigners);
    }
}
