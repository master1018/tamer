public abstract class HandshakeMessage {
    HandshakeMessage() { }
    static final byte   ht_hello_request = 0;
    static final byte   ht_client_hello = 1;
    static final byte   ht_server_hello = 2;
    static final byte   ht_certificate = 11;
    static final byte   ht_server_key_exchange = 12;
    static final byte   ht_certificate_request = 13;
    static final byte   ht_server_hello_done = 14;
    static final byte   ht_certificate_verify = 15;
    static final byte   ht_client_key_exchange = 16;
    static final byte   ht_finished = 20;
    public static final Debug debug = Debug.getInstance("ssl");
    static byte[] toByteArray(BigInteger bi) {
        byte[] b = bi.toByteArray();
        if ((b.length > 1) && (b[0] == 0)) {
            int n = b.length - 1;
            byte[] newarray = new byte[n];
            System.arraycopy(b, 1, newarray, 0, n);
            b = newarray;
        }
        return b;
    }
    static final byte[] MD5_pad1 = genPad(0x36, 48);
    static final byte[] MD5_pad2 = genPad(0x5c, 48);
    static final byte[] SHA_pad1 = genPad(0x36, 40);
    static final byte[] SHA_pad2 = genPad(0x5c, 40);
    private static byte[] genPad(int b, int count) {
        byte[] padding = new byte[count];
        Arrays.fill(padding, (byte)b);
        return padding;
    }
    final void write(HandshakeOutStream s) throws IOException {
        int len = messageLength();
        if (len > (1 << 24)) {
            throw new SSLException("Handshake message too big"
                + ", type = " + messageType() + ", len = " + len);
        }
        s.write(messageType());
        s.putInt24(len);
        send(s);
    }
    abstract int  messageType();
    abstract int  messageLength();
    abstract void send(HandshakeOutStream s) throws IOException;
    abstract void print(PrintStream p) throws IOException;
static final class HelloRequest extends HandshakeMessage {
    int messageType() { return ht_hello_request; }
    HelloRequest() { }
    HelloRequest(HandshakeInStream in) throws IOException
    {
    }
    int messageLength() { return 0; }
    void send(HandshakeOutStream out) throws IOException
    {
    }
    void print(PrintStream out) throws IOException
    {
        out.println("*** HelloRequest (empty)");
    }
}
static final class ClientHello extends HandshakeMessage {
    ProtocolVersion     protocolVersion;
    RandomCookie        clnt_random;
    SessionId           sessionId;
    private CipherSuiteList    cipherSuites;
    byte[]              compression_methods;
    HelloExtensions extensions = new HelloExtensions();
    private final static byte[]  NULL_COMPRESSION = new byte[] {0};
    ClientHello(SecureRandom generator, ProtocolVersion protocolVersion,
            SessionId sessionId, CipherSuiteList cipherSuites) {
        this.protocolVersion = protocolVersion;
        this.sessionId = sessionId;
        this.cipherSuites = cipherSuites;
        if (cipherSuites.containsEC()) {
            extensions.add(SupportedEllipticCurvesExtension.DEFAULT);
            extensions.add(SupportedEllipticPointFormatsExtension.DEFAULT);
        }
        clnt_random = new RandomCookie(generator);
        compression_methods = NULL_COMPRESSION;
    }
    ClientHello(HandshakeInStream s, int messageLength) throws IOException {
        protocolVersion = ProtocolVersion.valueOf(s.getInt8(), s.getInt8());
        clnt_random = new RandomCookie(s);
        sessionId = new SessionId(s.getBytes8());
        cipherSuites = new CipherSuiteList(s);
        compression_methods = s.getBytes8();
        if (messageLength() != messageLength) {
            extensions = new HelloExtensions(s);
        }
    }
    CipherSuiteList getCipherSuites() {
        return cipherSuites;
    }
    void addRenegotiationInfoExtension(byte[] clientVerifyData) {
        HelloExtension renegotiationInfo = new RenegotiationInfoExtension(
                    clientVerifyData, new byte[0]);
        extensions.add(renegotiationInfo);
    }
    void addServerNameIndicationExtension(String hostname) {
        ArrayList<String> hostnames = new ArrayList<>(1);
        hostnames.add(hostname);
        try {
            extensions.add(new ServerNameExtension(hostnames));
        } catch (IOException ioe) {
        }
    }
    void addSignatureAlgorithmsExtension(
            Collection<SignatureAndHashAlgorithm> algorithms) {
        HelloExtension signatureAlgorithm =
                new SignatureAlgorithmsExtension(algorithms);
        extensions.add(signatureAlgorithm);
    }
    @Override
    int messageType() { return ht_client_hello; }
    @Override
    int messageLength() {
        return (2 + 32 + 1 + 2 + 1
            + sessionId.length()                
            + (cipherSuites.size() * 2)
            + compression_methods.length)
            + extensions.length();
    }
    @Override
    void send(HandshakeOutStream s) throws IOException {
        s.putInt8(protocolVersion.major);
        s.putInt8(protocolVersion.minor);
        clnt_random.send(s);
        s.putBytes8(sessionId.getId());
        cipherSuites.send(s);
        s.putBytes8(compression_methods);
        extensions.send(s);
    }
    @Override
    void print(PrintStream s) throws IOException {
        s.println("*** ClientHello, " + protocolVersion);
        if (debug != null && Debug.isOn("verbose")) {
            s.print("RandomCookie:  ");
            clnt_random.print(s);
            s.print("Session ID:  ");
            s.println(sessionId);
            s.println("Cipher Suites: " + cipherSuites);
            Debug.println(s, "Compression Methods", compression_methods);
            extensions.print(s);
            s.println("***");
        }
    }
}
static final
class ServerHello extends HandshakeMessage
{
    int messageType() { return ht_server_hello; }
    ProtocolVersion     protocolVersion;
    RandomCookie        svr_random;
    SessionId           sessionId;
    CipherSuite         cipherSuite;
    byte                compression_method;
    HelloExtensions extensions = new HelloExtensions();
    ServerHello() {
    }
    ServerHello(HandshakeInStream input, int messageLength)
            throws IOException {
        protocolVersion = ProtocolVersion.valueOf(input.getInt8(),
                                                  input.getInt8());
        svr_random = new RandomCookie(input);
        sessionId = new SessionId(input.getBytes8());
        cipherSuite = CipherSuite.valueOf(input.getInt8(), input.getInt8());
        compression_method = (byte)input.getInt8();
        if (messageLength() != messageLength) {
            extensions = new HelloExtensions(input);
        }
    }
    int messageLength()
    {
        return 38 + sessionId.length() + extensions.length();
    }
    void send(HandshakeOutStream s) throws IOException
    {
        s.putInt8(protocolVersion.major);
        s.putInt8(protocolVersion.minor);
        svr_random.send(s);
        s.putBytes8(sessionId.getId());
        s.putInt8(cipherSuite.id >> 8);
        s.putInt8(cipherSuite.id & 0xff);
        s.putInt8(compression_method);
        extensions.send(s);
    }
    void print(PrintStream s) throws IOException
    {
        s.println("*** ServerHello, " + protocolVersion);
        if (debug != null && Debug.isOn("verbose")) {
            s.print("RandomCookie:  ");
            svr_random.print(s);
            int i;
            s.print("Session ID:  ");
            s.println(sessionId);
            s.println("Cipher Suite: " + cipherSuite);
            s.println("Compression Method: " + compression_method);
            extensions.print(s);
            s.println("***");
        }
    }
}
static final
class CertificateMsg extends HandshakeMessage
{
    int messageType() { return ht_certificate; }
    private X509Certificate[] chain;
    private List<byte[]> encodedChain;
    private int messageLength;
    CertificateMsg(X509Certificate[] certs) {
        chain = certs;
    }
    CertificateMsg(HandshakeInStream input) throws IOException {
        int chainLen = input.getInt24();
        List<Certificate> v = new ArrayList<>(4);
        CertificateFactory cf = null;
        while (chainLen > 0) {
            byte[] cert = input.getBytes24();
            chainLen -= (3 + cert.length);
            try {
                if (cf == null) {
                    cf = CertificateFactory.getInstance("X.509");
                }
                v.add(cf.generateCertificate(new ByteArrayInputStream(cert)));
            } catch (CertificateException e) {
                throw (SSLProtocolException)new SSLProtocolException(
                    e.getMessage()).initCause(e);
            }
        }
        chain = v.toArray(new X509Certificate[v.size()]);
    }
    int messageLength() {
        if (encodedChain == null) {
            messageLength = 3;
            encodedChain = new ArrayList<byte[]>(chain.length);
            try {
                for (X509Certificate cert : chain) {
                    byte[] b = cert.getEncoded();
                    encodedChain.add(b);
                    messageLength += b.length + 3;
                }
            } catch (CertificateEncodingException e) {
                encodedChain = null;
                throw new RuntimeException("Could not encode certificates", e);
            }
        }
        return messageLength;
    }
    void send(HandshakeOutStream s) throws IOException {
        s.putInt24(messageLength() - 3);
        for (byte[] b : encodedChain) {
            s.putBytes24(b);
        }
    }
    void print(PrintStream s) throws IOException {
        s.println("*** Certificate chain");
        if (debug != null && Debug.isOn("verbose")) {
            for (int i = 0; i < chain.length; i++)
                s.println("chain [" + i + "] = " + chain[i]);
            s.println("***");
        }
    }
    X509Certificate[] getCertificateChain() {
        return chain.clone();
    }
}
static abstract class ServerKeyExchange extends HandshakeMessage
{
    int messageType() { return ht_server_key_exchange; }
}
static final
class RSA_ServerKeyExchange extends ServerKeyExchange
{
    private byte rsa_modulus[];     
    private byte rsa_exponent[];    
    private Signature signature;
    private byte[] signatureBytes;
    private void updateSignature(byte clntNonce[], byte svrNonce[])
            throws SignatureException {
        int tmp;
        signature.update(clntNonce);
        signature.update(svrNonce);
        tmp = rsa_modulus.length;
        signature.update((byte)(tmp >> 8));
        signature.update((byte)(tmp & 0x0ff));
        signature.update(rsa_modulus);
        tmp = rsa_exponent.length;
        signature.update((byte)(tmp >> 8));
        signature.update((byte)(tmp & 0x0ff));
        signature.update(rsa_exponent);
    }
    RSA_ServerKeyExchange(PublicKey ephemeralKey, PrivateKey privateKey,
            RandomCookie clntNonce, RandomCookie svrNonce, SecureRandom sr)
            throws GeneralSecurityException {
        RSAPublicKeySpec rsaKey = JsseJce.getRSAPublicKeySpec(ephemeralKey);
        rsa_modulus = toByteArray(rsaKey.getModulus());
        rsa_exponent = toByteArray(rsaKey.getPublicExponent());
        signature = RSASignature.getInstance();
        signature.initSign(privateKey, sr);
        updateSignature(clntNonce.random_bytes, svrNonce.random_bytes);
        signatureBytes = signature.sign();
    }
    RSA_ServerKeyExchange(HandshakeInStream input)
            throws IOException, NoSuchAlgorithmException {
        signature = RSASignature.getInstance();
        rsa_modulus = input.getBytes16();
        rsa_exponent = input.getBytes16();
        signatureBytes = input.getBytes16();
    }
    PublicKey getPublicKey() {
        try {
            KeyFactory kfac = JsseJce.getKeyFactory("RSA");
            RSAPublicKeySpec kspec = new RSAPublicKeySpec(
                new BigInteger(1, rsa_modulus),
                new BigInteger(1, rsa_exponent));
            return kfac.generatePublic(kspec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    boolean verify(PublicKey certifiedKey, RandomCookie clntNonce,
            RandomCookie svrNonce) throws GeneralSecurityException {
        signature.initVerify(certifiedKey);
        updateSignature(clntNonce.random_bytes, svrNonce.random_bytes);
        return signature.verify(signatureBytes);
    }
    int messageLength() {
        return 6 + rsa_modulus.length + rsa_exponent.length
               + signatureBytes.length;
    }
    void send(HandshakeOutStream s) throws IOException {
        s.putBytes16(rsa_modulus);
        s.putBytes16(rsa_exponent);
        s.putBytes16(signatureBytes);
    }
    void print(PrintStream s) throws IOException {
        s.println("*** RSA ServerKeyExchange");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "RSA Modulus", rsa_modulus);
            Debug.println(s, "RSA Public Exponent", rsa_exponent);
        }
    }
}
static final
class DH_ServerKeyExchange extends ServerKeyExchange
{
    private final static boolean dhKeyExchangeFix =
        Debug.getBooleanProperty("com.sun.net.ssl.dhKeyExchangeFix", true);
    private byte                dh_p [];        
    private byte                dh_g [];        
    private byte                dh_Ys [];       
    private byte                signature [];
    ProtocolVersion protocolVersion;
    private SignatureAndHashAlgorithm preferableSignatureAlgorithm;
    DH_ServerKeyExchange(DHCrypt obj, ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.preferableSignatureAlgorithm = null;
        setValues(obj);
        signature = null;
    }
    DH_ServerKeyExchange(DHCrypt obj, PrivateKey key, byte clntNonce[],
            byte svrNonce[], SecureRandom sr,
            SignatureAndHashAlgorithm signAlgorithm,
            ProtocolVersion protocolVersion) throws GeneralSecurityException {
        this.protocolVersion = protocolVersion;
        setValues(obj);
        Signature sig;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            this.preferableSignatureAlgorithm = signAlgorithm;
            sig = JsseJce.getSignature(signAlgorithm.getAlgorithmName());
        } else {
            this.preferableSignatureAlgorithm = null;
            if (key.getAlgorithm().equals("DSA")) {
                sig = JsseJce.getSignature(JsseJce.SIGNATURE_DSA);
            } else {
                sig = RSASignature.getInstance();
            }
        }
        sig.initSign(key, sr);
        updateSignature(sig, clntNonce, svrNonce);
        signature = sig.sign();
    }
    DH_ServerKeyExchange(HandshakeInStream input,
            ProtocolVersion protocolVersion) throws IOException {
        this.protocolVersion = protocolVersion;
        this.preferableSignatureAlgorithm = null;
        dh_p = input.getBytes16();
        dh_g = input.getBytes16();
        dh_Ys = input.getBytes16();
        signature = null;
    }
    DH_ServerKeyExchange(HandshakeInStream input, PublicKey publicKey,
            byte clntNonce[], byte svrNonce[], int messageSize,
            Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs,
            ProtocolVersion protocolVersion)
            throws IOException, GeneralSecurityException {
        this.protocolVersion = protocolVersion;
        dh_p = input.getBytes16();
        dh_g = input.getBytes16();
        dh_Ys = input.getBytes16();
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            int hash = input.getInt8();         
            int signature = input.getInt8();    
            preferableSignatureAlgorithm =
                SignatureAndHashAlgorithm.valueOf(hash, signature, 0);
            if (!localSupportedSignAlgs.contains(
                    preferableSignatureAlgorithm)) {
                throw new SSLHandshakeException(
                        "Unsupported SignatureAndHashAlgorithm in " +
                        "ServerKeyExchange message");
            }
        } else {
            this.preferableSignatureAlgorithm = null;
        }
        byte signature[];
        if (dhKeyExchangeFix) {
            signature = input.getBytes16();
        } else {
            messageSize -= (dh_p.length + 2);
            messageSize -= (dh_g.length + 2);
            messageSize -= (dh_Ys.length + 2);
            signature = new byte[messageSize];
            input.read(signature);
        }
        Signature sig;
        String algorithm = publicKey.getAlgorithm();
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            sig = JsseJce.getSignature(
                        preferableSignatureAlgorithm.getAlgorithmName());
        } else {
            if (algorithm.equals("DSA")) {
                sig = JsseJce.getSignature(JsseJce.SIGNATURE_DSA);
            } else if (algorithm.equals("RSA")) {
                sig = RSASignature.getInstance();
            } else {
                throw new SSLKeyException("neither an RSA or a DSA key");
            }
        }
        sig.initVerify(publicKey);
        updateSignature(sig, clntNonce, svrNonce);
        if (sig.verify(signature) == false ) {
            throw new SSLKeyException("Server D-H key verification failed");
        }
    }
    BigInteger getModulus() {
        return new BigInteger(1, dh_p);
    }
    BigInteger getBase() {
        return new BigInteger(1, dh_g);
    }
    BigInteger getServerPublicKey() {
        return new BigInteger(1, dh_Ys);
    }
    private void updateSignature(Signature sig, byte clntNonce[],
            byte svrNonce[]) throws SignatureException {
        int tmp;
        sig.update(clntNonce);
        sig.update(svrNonce);
        tmp = dh_p.length;
        sig.update((byte)(tmp >> 8));
        sig.update((byte)(tmp & 0x0ff));
        sig.update(dh_p);
        tmp = dh_g.length;
        sig.update((byte)(tmp >> 8));
        sig.update((byte)(tmp & 0x0ff));
        sig.update(dh_g);
        tmp = dh_Ys.length;
        sig.update((byte)(tmp >> 8));
        sig.update((byte)(tmp & 0x0ff));
        sig.update(dh_Ys);
    }
    private void setValues(DHCrypt obj) {
        dh_p = toByteArray(obj.getModulus());
        dh_g = toByteArray(obj.getBase());
        dh_Ys = toByteArray(obj.getPublicKey());
    }
    int messageLength() {
        int temp = 6;   
        temp += dh_p.length;
        temp += dh_g.length;
        temp += dh_Ys.length;
        if (signature != null) {
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                temp += SignatureAndHashAlgorithm.sizeInRecord();
            }
            temp += signature.length;
            if (dhKeyExchangeFix) {
                temp += 2;
            }
        }
        return temp;
    }
    void send(HandshakeOutStream s) throws IOException {
        s.putBytes16(dh_p);
        s.putBytes16(dh_g);
        s.putBytes16(dh_Ys);
        if (signature != null) {
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                s.putInt8(preferableSignatureAlgorithm.getHashValue());
                s.putInt8(preferableSignatureAlgorithm.getSignatureValue());
            }
            if (dhKeyExchangeFix) {
                s.putBytes16(signature);
            } else {
                s.write(signature);
            }
        }
    }
    void print(PrintStream s) throws IOException {
        s.println("*** Diffie-Hellman ServerKeyExchange");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "DH Modulus", dh_p);
            Debug.println(s, "DH Base", dh_g);
            Debug.println(s, "Server DH Public Key", dh_Ys);
            if (signature == null) {
                s.println("Anonymous");
            } else {
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    s.println("Signature Algorithm " +
                        preferableSignatureAlgorithm.getAlgorithmName());
                }
                s.println("Signed with a DSA or RSA public key");
            }
        }
    }
}
static final
class ECDH_ServerKeyExchange extends ServerKeyExchange {
    private final static int CURVE_EXPLICIT_PRIME = 1;
    private final static int CURVE_EXPLICIT_CHAR2 = 2;
    private final static int CURVE_NAMED_CURVE    = 3;
    private int curveId;
    private byte[] pointBytes;
    private byte[] signatureBytes;
    private ECPublicKey publicKey;
    ProtocolVersion protocolVersion;
    private SignatureAndHashAlgorithm preferableSignatureAlgorithm;
    ECDH_ServerKeyExchange(ECDHCrypt obj, PrivateKey privateKey,
            byte[] clntNonce, byte[] svrNonce, SecureRandom sr,
            SignatureAndHashAlgorithm signAlgorithm,
            ProtocolVersion protocolVersion) throws GeneralSecurityException {
        this.protocolVersion = protocolVersion;
        publicKey = (ECPublicKey)obj.getPublicKey();
        ECParameterSpec params = publicKey.getParams();
        ECPoint point = publicKey.getW();
        pointBytes = JsseJce.encodePoint(point, params.getCurve());
        curveId = SupportedEllipticCurvesExtension.getCurveIndex(params);
        if (privateKey == null) {
            return;
        }
        Signature sig;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            this.preferableSignatureAlgorithm = signAlgorithm;
            sig = JsseJce.getSignature(signAlgorithm.getAlgorithmName());
        } else {
            sig = getSignature(privateKey.getAlgorithm());
        }
        sig.initSign(privateKey);  
        updateSignature(sig, clntNonce, svrNonce);
        signatureBytes = sig.sign();
    }
    ECDH_ServerKeyExchange(HandshakeInStream input, PublicKey signingKey,
            byte[] clntNonce, byte[] svrNonce,
            Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs,
            ProtocolVersion protocolVersion)
            throws IOException, GeneralSecurityException {
        this.protocolVersion = protocolVersion;
        int curveType = input.getInt8();
        ECParameterSpec parameters;
        if (curveType == CURVE_NAMED_CURVE) {
            curveId = input.getInt16();
            if (SupportedEllipticCurvesExtension.isSupported(curveId)
                    == false) {
                throw new SSLHandshakeException(
                    "Unsupported curveId: " + curveId);
            }
            String curveOid =
                SupportedEllipticCurvesExtension.getCurveOid(curveId);
            if (curveOid == null) {
                throw new SSLHandshakeException(
                    "Unknown named curve: " + curveId);
            }
            parameters = JsseJce.getECParameterSpec(curveOid);
            if (parameters == null) {
                throw new SSLHandshakeException(
                    "Unsupported curve: " + curveOid);
            }
        } else {
            throw new SSLHandshakeException(
                "Unsupported ECCurveType: " + curveType);
        }
        pointBytes = input.getBytes8();
        ECPoint point = JsseJce.decodePoint(pointBytes, parameters.getCurve());
        KeyFactory factory = JsseJce.getKeyFactory("EC");
        publicKey = (ECPublicKey)factory.generatePublic(
            new ECPublicKeySpec(point, parameters));
        if (signingKey == null) {
            return;
        }
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            int hash = input.getInt8();         
            int signature = input.getInt8();    
            preferableSignatureAlgorithm =
                SignatureAndHashAlgorithm.valueOf(hash, signature, 0);
            if (!localSupportedSignAlgs.contains(
                    preferableSignatureAlgorithm)) {
                throw new SSLHandshakeException(
                        "Unsupported SignatureAndHashAlgorithm in " +
                        "ServerKeyExchange message");
            }
        }
        signatureBytes = input.getBytes16();
        Signature sig;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            sig = JsseJce.getSignature(
                        preferableSignatureAlgorithm.getAlgorithmName());
        } else {
            sig = getSignature(signingKey.getAlgorithm());
        }
        sig.initVerify(signingKey);
        updateSignature(sig, clntNonce, svrNonce);
        if (sig.verify(signatureBytes) == false ) {
            throw new SSLKeyException(
                "Invalid signature on ECDH server key exchange message");
        }
    }
    ECPublicKey getPublicKey() {
        return publicKey;
    }
    private static Signature getSignature(String keyAlgorithm)
            throws NoSuchAlgorithmException {
        if (keyAlgorithm.equals("EC")) {
            return JsseJce.getSignature(JsseJce.SIGNATURE_ECDSA);
        } else if (keyAlgorithm.equals("RSA")) {
            return RSASignature.getInstance();
        } else {
            throw new NoSuchAlgorithmException("neither an RSA or a EC key");
        }
    }
    private void updateSignature(Signature sig, byte clntNonce[],
            byte svrNonce[]) throws SignatureException {
        sig.update(clntNonce);
        sig.update(svrNonce);
        sig.update((byte)CURVE_NAMED_CURVE);
        sig.update((byte)(curveId >> 8));
        sig.update((byte)curveId);
        sig.update((byte)pointBytes.length);
        sig.update(pointBytes);
    }
    int messageLength() {
        int sigLen = 0;
        if (signatureBytes != null) {
            sigLen = 2 + signatureBytes.length;
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                sigLen += SignatureAndHashAlgorithm.sizeInRecord();
            }
        }
        return 4 + pointBytes.length + sigLen;
    }
    void send(HandshakeOutStream s) throws IOException {
        s.putInt8(CURVE_NAMED_CURVE);
        s.putInt16(curveId);
        s.putBytes8(pointBytes);
        if (signatureBytes != null) {
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                s.putInt8(preferableSignatureAlgorithm.getHashValue());
                s.putInt8(preferableSignatureAlgorithm.getSignatureValue());
            }
            s.putBytes16(signatureBytes);
        }
    }
    void print(PrintStream s) throws IOException {
        s.println("*** ECDH ServerKeyExchange");
        if (debug != null && Debug.isOn("verbose")) {
            if (signatureBytes == null) {
                s.println("Anonymous");
            } else {
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    s.println("Signature Algorithm " +
                            preferableSignatureAlgorithm.getAlgorithmName());
                }
            }
            s.println("Server key: " + publicKey);
        }
    }
}
static final class DistinguishedName {
    byte name[];
    DistinguishedName(HandshakeInStream input) throws IOException {
        name = input.getBytes16();
    }
    DistinguishedName(X500Principal dn) {
        name = dn.getEncoded();
    }
    X500Principal getX500Principal() throws IOException {
        try {
            return new X500Principal(name);
        } catch (IllegalArgumentException e) {
            throw (SSLProtocolException)new SSLProtocolException(
                e.getMessage()).initCause(e);
        }
    }
    int length() {
        return 2 + name.length;
    }
    void send(HandshakeOutStream output) throws IOException {
        output.putBytes16(name);
    }
    void print(PrintStream output) throws IOException {
        X500Principal principal = new X500Principal(name);
        output.println("<" + principal.toString() + ">");
    }
}
static final
class CertificateRequest extends HandshakeMessage
{
    static final int   cct_rsa_sign = 1;
    static final int   cct_dss_sign = 2;
    static final int   cct_rsa_fixed_dh = 3;
    static final int   cct_dss_fixed_dh = 4;
    static final int   cct_rsa_ephemeral_dh = 5;
    static final int   cct_dss_ephemeral_dh = 6;
    static final int    cct_ecdsa_sign       = 64;
    static final int    cct_rsa_fixed_ecdh   = 65;
    static final int    cct_ecdsa_fixed_ecdh = 66;
    private final static byte[] TYPES_NO_ECC = { cct_rsa_sign, cct_dss_sign };
    private final static byte[] TYPES_ECC =
        { cct_rsa_sign, cct_dss_sign, cct_ecdsa_sign };
    byte                types [];               
    DistinguishedName   authorities [];         
    ProtocolVersion protocolVersion;
    private Collection<SignatureAndHashAlgorithm> algorithms;
    private int algorithmsLen;
    CertificateRequest(X509Certificate ca[], KeyExchange keyExchange,
            Collection<SignatureAndHashAlgorithm> signAlgs,
            ProtocolVersion protocolVersion) throws IOException {
        this.protocolVersion = protocolVersion;
        authorities = new DistinguishedName[ca.length];
        for (int i = 0; i < ca.length; i++) {
            X500Principal x500Principal = ca[i].getSubjectX500Principal();
            authorities[i] = new DistinguishedName(x500Principal);
        }
        this.types = JsseJce.isEcAvailable() ? TYPES_ECC : TYPES_NO_ECC;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            if (signAlgs == null || signAlgs.isEmpty()) {
                throw new SSLProtocolException(
                        "No supported signature algorithms");
            }
            algorithms = new ArrayList<SignatureAndHashAlgorithm>(signAlgs);
            algorithmsLen =
                SignatureAndHashAlgorithm.sizeInRecord() * algorithms.size();
        } else {
            algorithms = new ArrayList<SignatureAndHashAlgorithm>();
            algorithmsLen = 0;
        }
    }
    CertificateRequest(HandshakeInStream input,
            ProtocolVersion protocolVersion) throws IOException {
        this.protocolVersion = protocolVersion;
        types = input.getBytes8();
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            algorithmsLen = input.getInt16();
            if (algorithmsLen < 2) {
                throw new SSLProtocolException(
                        "Invalid supported_signature_algorithms field");
            }
            algorithms = new ArrayList<SignatureAndHashAlgorithm>();
            int remains = algorithmsLen;
            int sequence = 0;
            while (remains > 1) {    
                int hash = input.getInt8();         
                int signature = input.getInt8();    
                SignatureAndHashAlgorithm algorithm =
                    SignatureAndHashAlgorithm.valueOf(hash, signature,
                                                                ++sequence);
                algorithms.add(algorithm);
                remains -= 2;  
            }
            if (remains != 0) {
                throw new SSLProtocolException(
                        "Invalid supported_signature_algorithms field");
            }
        } else {
            algorithms = new ArrayList<SignatureAndHashAlgorithm>();
            algorithmsLen = 0;
        }
        int len = input.getInt16();
        ArrayList<DistinguishedName> v = new ArrayList<>();
        while (len >= 3) {
            DistinguishedName dn = new DistinguishedName(input);
            v.add(dn);
            len -= dn.length();
        }
        if (len != 0) {
            throw new SSLProtocolException("Bad CertificateRequest DN length");
        }
        authorities = v.toArray(new DistinguishedName[v.size()]);
    }
    X500Principal[] getAuthorities() throws IOException {
        X500Principal[] ret = new X500Principal[authorities.length];
        for (int i = 0; i < authorities.length; i++) {
            ret[i] = authorities[i].getX500Principal();
        }
        return ret;
    }
    Collection<SignatureAndHashAlgorithm> getSignAlgorithms() {
        return algorithms;
    }
    @Override
    int messageType() {
        return ht_certificate_request;
    }
    @Override
    int messageLength() {
        int len = 1 + types.length + 2;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            len += algorithmsLen + 2;
        }
        for (int i = 0; i < authorities.length; i++) {
            len += authorities[i].length();
        }
        return len;
    }
    @Override
    void send(HandshakeOutStream output) throws IOException {
        output.putBytes8(types);
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            output.putInt16(algorithmsLen);
            for (SignatureAndHashAlgorithm algorithm : algorithms) {
                output.putInt8(algorithm.getHashValue());      
                output.putInt8(algorithm.getSignatureValue()); 
            }
        }
        int len = 0;
        for (int i = 0; i < authorities.length; i++) {
            len += authorities[i].length();
        }
        output.putInt16(len);
        for (int i = 0; i < authorities.length; i++) {
            authorities[i].send(output);
        }
    }
    @Override
    void print(PrintStream s) throws IOException {
        s.println("*** CertificateRequest");
        if (debug != null && Debug.isOn("verbose")) {
            s.print("Cert Types: ");
            for (int i = 0; i < types.length; i++) {
                switch (types[i]) {
                  case cct_rsa_sign:
                    s.print("RSA"); break;
                  case cct_dss_sign:
                    s.print("DSS"); break;
                  case cct_rsa_fixed_dh:
                    s.print("Fixed DH (RSA sig)"); break;
                  case cct_dss_fixed_dh:
                    s.print("Fixed DH (DSS sig)"); break;
                  case cct_rsa_ephemeral_dh:
                    s.print("Ephemeral DH (RSA sig)"); break;
                  case cct_dss_ephemeral_dh:
                    s.print("Ephemeral DH (DSS sig)"); break;
                  case cct_ecdsa_sign:
                    s.print("ECDSA"); break;
                  case cct_rsa_fixed_ecdh:
                    s.print("Fixed ECDH (RSA sig)"); break;
                  case cct_ecdsa_fixed_ecdh:
                    s.print("Fixed ECDH (ECDSA sig)"); break;
                  default:
                    s.print("Type-" + (types[i] & 0xff)); break;
                }
                if (i != types.length - 1) {
                    s.print(", ");
                }
            }
            s.println();
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                StringBuffer buffer = new StringBuffer();
                boolean opened = false;
                for (SignatureAndHashAlgorithm signAlg : algorithms) {
                    if (opened) {
                        buffer.append(", " + signAlg.getAlgorithmName());
                    } else {
                        buffer.append(signAlg.getAlgorithmName());
                        opened = true;
                    }
                }
                s.println("Supported Signature Algorithms: " + buffer);
            }
            s.println("Cert Authorities:");
            if (authorities.length == 0) {
                s.println("<Empty>");
            } else {
                for (int i = 0; i < authorities.length; i++) {
                    authorities[i].print(s);
                }
            }
        }
    }
}
static final
class ServerHelloDone extends HandshakeMessage
{
    int messageType() { return ht_server_hello_done; }
    ServerHelloDone() { }
    ServerHelloDone(HandshakeInStream input)
    {
    }
    int messageLength()
    {
        return 0;
    }
    void send(HandshakeOutStream s) throws IOException
    {
    }
    void print(PrintStream s) throws IOException
    {
        s.println("*** ServerHelloDone");
    }
}
static final class CertificateVerify extends HandshakeMessage {
    private byte[] signature;
    ProtocolVersion protocolVersion;
    private SignatureAndHashAlgorithm preferableSignatureAlgorithm = null;
    CertificateVerify(ProtocolVersion protocolVersion,
            HandshakeHash handshakeHash, PrivateKey privateKey,
            SecretKey masterSecret, SecureRandom sr,
            SignatureAndHashAlgorithm signAlgorithm)
            throws GeneralSecurityException {
        this.protocolVersion = protocolVersion;
        String algorithm = privateKey.getAlgorithm();
        Signature sig = null;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            this.preferableSignatureAlgorithm = signAlgorithm;
            sig = JsseJce.getSignature(signAlgorithm.getAlgorithmName());
        } else {
            sig = getSignature(protocolVersion, algorithm);
        }
        sig.initSign(privateKey, sr);
        updateSignature(sig, protocolVersion, handshakeHash, algorithm,
                        masterSecret);
        signature = sig.sign();
    }
    CertificateVerify(HandshakeInStream input,
            Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs,
            ProtocolVersion protocolVersion) throws IOException  {
        this.protocolVersion = protocolVersion;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            int hashAlg = input.getInt8();         
            int signAlg = input.getInt8();         
            preferableSignatureAlgorithm =
                SignatureAndHashAlgorithm.valueOf(hashAlg, signAlg, 0);
            if (!localSupportedSignAlgs.contains(
                    preferableSignatureAlgorithm)) {
                throw new SSLHandshakeException(
                        "Unsupported SignatureAndHashAlgorithm in " +
                        "ServerKeyExchange message");
            }
        }
        signature = input.getBytes16();
    }
    SignatureAndHashAlgorithm getPreferableSignatureAlgorithm() {
        return preferableSignatureAlgorithm;
    }
    boolean verify(ProtocolVersion protocolVersion,
            HandshakeHash handshakeHash, PublicKey publicKey,
            SecretKey masterSecret) throws GeneralSecurityException {
        String algorithm = publicKey.getAlgorithm();
        Signature sig = null;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            sig = JsseJce.getSignature(
                        preferableSignatureAlgorithm.getAlgorithmName());
        } else {
            sig = getSignature(protocolVersion, algorithm);
        }
        sig.initVerify(publicKey);
        updateSignature(sig, protocolVersion, handshakeHash, algorithm,
                        masterSecret);
        return sig.verify(signature);
    }
    private static Signature getSignature(ProtocolVersion protocolVersion,
            String algorithm) throws GeneralSecurityException {
        if (algorithm.equals("RSA")) {
            return RSASignature.getInternalInstance();
        } else if (algorithm.equals("DSA")) {
            return JsseJce.getSignature(JsseJce.SIGNATURE_RAWDSA);
        } else if (algorithm.equals("EC")) {
            return JsseJce.getSignature(JsseJce.SIGNATURE_RAWECDSA);
        } else {
            throw new SignatureException("Unrecognized algorithm: "
                + algorithm);
        }
    }
    private static void updateSignature(Signature sig,
            ProtocolVersion protocolVersion,
            HandshakeHash handshakeHash, String algorithm, SecretKey masterKey)
            throws SignatureException {
        if (algorithm.equals("RSA")) {
            if (protocolVersion.v < ProtocolVersion.TLS12.v) { 
                MessageDigest md5Clone = handshakeHash.getMD5Clone();
                MessageDigest shaClone = handshakeHash.getSHAClone();
                if (protocolVersion.v < ProtocolVersion.TLS10.v) { 
                    updateDigest(md5Clone, MD5_pad1, MD5_pad2, masterKey);
                    updateDigest(shaClone, SHA_pad1, SHA_pad2, masterKey);
                }
                RSASignature.setHashes(sig, md5Clone, shaClone);
            } else {  
                sig.update(handshakeHash.getAllHandshakeMessages());
            }
        } else { 
            if (protocolVersion.v < ProtocolVersion.TLS12.v) { 
                MessageDigest shaClone = handshakeHash.getSHAClone();
                if (protocolVersion.v < ProtocolVersion.TLS10.v) { 
                    updateDigest(shaClone, SHA_pad1, SHA_pad2, masterKey);
                }
                sig.update(shaClone.digest());
            } else {  
                sig.update(handshakeHash.getAllHandshakeMessages());
            }
        }
    }
    private static void updateDigest(MessageDigest md,
            byte[] pad1, byte[] pad2,
            SecretKey masterSecret) {
        byte[] keyBytes = "RAW".equals(masterSecret.getFormat())
                        ? masterSecret.getEncoded() : null;
        if (keyBytes != null) {
            md.update(keyBytes);
        } else {
            digestKey(md, masterSecret);
        }
        md.update(pad1);
        byte[] temp = md.digest();
        if (keyBytes != null) {
            md.update(keyBytes);
        } else {
            digestKey(md, masterSecret);
        }
        md.update(pad2);
        md.update(temp);
    }
    private final static Class delegate;
    private final static Field spiField;
    static {
        try {
            delegate = Class.forName("java.security.MessageDigest$Delegate");
            spiField = delegate.getDeclaredField("digestSpi");
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed", e);
        }
        makeAccessible(spiField);
    }
    private static void makeAccessible(final AccessibleObject o) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                o.setAccessible(true);
                return null;
            }
        });
    }
    private final static Object NULL_OBJECT = new Object();
    private final static Map<Class,Object> methodCache =
                                        new ConcurrentHashMap<>();
    private static void digestKey(MessageDigest md, SecretKey key) {
        try {
            if (md.getClass() != delegate) {
                throw new Exception("Digest is not a MessageDigestSpi");
            }
            MessageDigestSpi spi = (MessageDigestSpi)spiField.get(md);
            Class<?> clazz = spi.getClass();
            Object r = methodCache.get(clazz);
            if (r == null) {
                try {
                    r = clazz.getDeclaredMethod("implUpdate", SecretKey.class);
                    makeAccessible((Method)r);
                } catch (NoSuchMethodException e) {
                    r = NULL_OBJECT;
                }
                methodCache.put(clazz, r);
            }
            if (r == NULL_OBJECT) {
                throw new Exception(
                    "Digest does not support implUpdate(SecretKey)");
            }
            Method update = (Method)r;
            update.invoke(spi, key);
        } catch (Exception e) {
            throw new RuntimeException(
                "Could not obtain encoded key and "
                + "MessageDigest cannot digest key", e);
        }
    }
    @Override
    int messageType() {
        return ht_certificate_verify;
    }
    @Override
    int messageLength() {
        int temp = 2;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            temp += SignatureAndHashAlgorithm.sizeInRecord();
        }
        return temp + signature.length;
    }
    @Override
    void send(HandshakeOutStream s) throws IOException {
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            s.putInt8(preferableSignatureAlgorithm.getHashValue());
            s.putInt8(preferableSignatureAlgorithm.getSignatureValue());
        }
        s.putBytes16(signature);
    }
    @Override
    void print(PrintStream s) throws IOException {
        s.println("*** CertificateVerify");
        if (debug != null && Debug.isOn("verbose")) {
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                s.println("Signature Algorithm " +
                        preferableSignatureAlgorithm.getAlgorithmName());
            }
        }
    }
}
static final class Finished extends HandshakeMessage {
    final static int CLIENT = 1;
    final static int SERVER = 2;
    private static final byte[] SSL_CLIENT = { 0x43, 0x4C, 0x4E, 0x54 };
    private static final byte[] SSL_SERVER = { 0x53, 0x52, 0x56, 0x52 };
    private byte[] verifyData;
    private ProtocolVersion protocolVersion;
    private CipherSuite cipherSuite;
    Finished(ProtocolVersion protocolVersion, HandshakeHash handshakeHash,
            int sender, SecretKey master, CipherSuite cipherSuite) {
        this.protocolVersion = protocolVersion;
        this.cipherSuite = cipherSuite;
        verifyData = getFinished(handshakeHash, sender, master);
    }
    Finished(ProtocolVersion protocolVersion, HandshakeInStream input,
            CipherSuite cipherSuite) throws IOException {
        this.protocolVersion = protocolVersion;
        this.cipherSuite = cipherSuite;
        int msgLen = (protocolVersion.v >= ProtocolVersion.TLS10.v) ? 12 : 36;
        verifyData = new byte[msgLen];
        input.read(verifyData);
    }
    boolean verify(HandshakeHash handshakeHash, int sender, SecretKey master) {
        byte[] myFinished = getFinished(handshakeHash, sender, master);
        return Arrays.equals(myFinished, verifyData);
    }
    private byte[] getFinished(HandshakeHash handshakeHash,
            int sender, SecretKey masterKey) {
        byte[] sslLabel;
        String tlsLabel;
        if (sender == CLIENT) {
            sslLabel = SSL_CLIENT;
            tlsLabel = "client finished";
        } else if (sender == SERVER) {
            sslLabel = SSL_SERVER;
            tlsLabel = "server finished";
        } else {
            throw new RuntimeException("Invalid sender: " + sender);
        }
        if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
            try {
                byte [] seed;
                String prfAlg;
                PRF prf;
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    seed = handshakeHash.getFinishedHash();
                    prfAlg = "SunTls12Prf";
                    prf = cipherSuite.prfAlg;
                } else {
                    MessageDigest md5Clone = handshakeHash.getMD5Clone();
                    MessageDigest shaClone = handshakeHash.getSHAClone();
                    seed = new byte[36];
                    md5Clone.digest(seed, 0, 16);
                    shaClone.digest(seed, 16, 20);
                    prfAlg = "SunTlsPrf";
                    prf = P_NONE;
                }
                String prfHashAlg = prf.getPRFHashAlg();
                int prfHashLength = prf.getPRFHashLength();
                int prfBlockSize = prf.getPRFBlockSize();
                TlsPrfParameterSpec spec = new TlsPrfParameterSpec(
                    masterKey, tlsLabel, seed, 12,
                    prfHashAlg, prfHashLength, prfBlockSize);
                KeyGenerator kg = JsseJce.getKeyGenerator(prfAlg);
                kg.init(spec);
                SecretKey prfKey = kg.generateKey();
                if ("RAW".equals(prfKey.getFormat()) == false) {
                    throw new ProviderException(
                        "Invalid PRF output, format must be RAW");
                }
                byte[] finished = prfKey.getEncoded();
                return finished;
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("PRF failed", e);
            }
        } else {
            MessageDigest md5Clone = handshakeHash.getMD5Clone();
            MessageDigest shaClone = handshakeHash.getSHAClone();
            updateDigest(md5Clone, sslLabel, MD5_pad1, MD5_pad2, masterKey);
            updateDigest(shaClone, sslLabel, SHA_pad1, SHA_pad2, masterKey);
            byte[] finished = new byte[36];
            try {
                md5Clone.digest(finished, 0, 16);
                shaClone.digest(finished, 16, 20);
            } catch (DigestException e) {
                throw new RuntimeException("Digest failed", e);
            }
            return finished;
        }
    }
    private static void updateDigest(MessageDigest md, byte[] sender,
            byte[] pad1, byte[] pad2, SecretKey masterSecret) {
        md.update(sender);
        CertificateVerify.updateDigest(md, pad1, pad2, masterSecret);
    }
    byte[] getVerifyData() {
        return verifyData;
    }
    @Override
    int messageType() { return ht_finished; }
    @Override
    int messageLength() {
        return verifyData.length;
    }
    @Override
    void send(HandshakeOutStream out) throws IOException {
        out.write(verifyData);
    }
    @Override
    void print(PrintStream s) throws IOException {
        s.println("*** Finished");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "verify_data", verifyData);
            s.println("***");
        }
    }
}
}
