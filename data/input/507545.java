public abstract class HandshakeProtocol {
    public final static int NEED_UNWRAP = 1;
    public final static int NOT_HANDSHAKING = 2;
    public final static int FINISHED = 3;
    public final static int NEED_TASK = 4;
    protected int status = NOT_HANDSHAKING;
    protected HandshakeIODataStream io_stream = new HandshakeIODataStream();
    protected SSLRecordProtocol recordProtocol;
    protected SSLParameters parameters;
    protected Vector<DelegatedTask> delegatedTasks = new Vector<DelegatedTask>();
    protected boolean nonBlocking;
    protected SSLSessionImpl session;
    protected ClientHello clientHello;
    protected ServerHello serverHello;
    protected CertificateMessage serverCert;
    protected ServerKeyExchange serverKeyExchange;
    protected CertificateRequest certificateRequest;
    protected ServerHelloDone serverHelloDone;
    protected CertificateMessage clientCert;
    protected ClientKeyExchange clientKeyExchange;
    protected CertificateVerify certificateVerify;
    protected Finished clientFinished;
    protected Finished serverFinished;
    protected boolean changeCipherSpecReceived = false;
    protected boolean isResuming = false;
    protected byte[] preMasterSecret;
    protected Exception delegatedTaskErr;
    private byte[] verify_data = new byte[12];
    private byte[] master_secret_bytes = 
            {109, 97, 115, 116, 101, 114, 32, 115, 101, 99, 114, 101, 116 };
    private boolean needSendCCSpec = false;
    protected boolean needSendHelloRequest = false;
    public SSLEngineImpl engineOwner;
    protected HandshakeProtocol(Object owner) {
        if (owner instanceof SSLEngineImpl) {
            engineOwner = (SSLEngineImpl) owner;
            nonBlocking = true;
            this.parameters = engineOwner.sslParameters;
        }
    }
    public void setRecordProtocol(SSLRecordProtocol recordProtocol) {
        this.recordProtocol = recordProtocol;
    }
    public abstract void start();
    protected void stop() {
        clearMessages();
        status = NOT_HANDSHAKING;
    }
    public SSLEngineResult.HandshakeStatus getStatus() {
        if (io_stream.hasData() || needSendCCSpec || 
                needSendHelloRequest || delegatedTaskErr != null) {
            return SSLEngineResult.HandshakeStatus.NEED_WRAP;
        }
        if (!delegatedTasks.isEmpty()) {
            return SSLEngineResult.HandshakeStatus.NEED_TASK;
        }
        switch (status) {
        case HandshakeProtocol.NEED_UNWRAP:
            return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
        case HandshakeProtocol.FINISHED:
            status = NOT_HANDSHAKING;
            clearMessages();
            return SSLEngineResult.HandshakeStatus.FINISHED;
        default: 
            return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }
    }
    public SSLSessionImpl getSession() {
        return session;
    }
    protected void sendChangeCipherSpec() {
        needSendCCSpec = true;
    }
    protected void sendHelloRequest() {
        needSendHelloRequest = true;
    }
    abstract void receiveChangeCipherSpec();
    abstract void makeFinished();
    public abstract void unwrap(byte[] bytes);
    public abstract void unwrapSSLv2(byte[] bytes);
    public byte[] wrap() {
        if (delegatedTaskErr != null) {
            Exception e = delegatedTaskErr;
            delegatedTaskErr = null;
            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                    "Error occured in delegated task:" + e.getMessage(), e);
        }
        if (io_stream.hasData()) {
            return recordProtocol.wrap(ContentType.HANDSHAKE, io_stream);
        } else if (needSendCCSpec) {
            makeFinished();
            needSendCCSpec = false;
            return recordProtocol.getChangeCipherSpecMesage(getSession());
        } else if (needSendHelloRequest) {
            needSendHelloRequest = false;
            return recordProtocol.wrap(ContentType.HANDSHAKE, 
                    new byte[] {0, 0, 0, 0}, 0, 4);
        } else {
            return null; 
        }
    }
    protected void sendWarningAlert(byte description) {
        recordProtocol.alert(AlertProtocol.WARNING, description);
    }
    protected void fatalAlert(byte description, String reason) {
        throw new AlertException(description, new SSLHandshakeException(reason));
    }
    protected void fatalAlert(byte description, String reason, Exception cause) {
        throw new AlertException(description, new SSLException(reason, cause));
    }
    protected void fatalAlert(byte description, SSLException cause) {
        throw new AlertException(description, cause);
    }
    protected void computerReferenceVerifyDataTLS(String label) {
        computerVerifyDataTLS(label, verify_data);
    }
    protected void computerVerifyDataTLS(String label, byte[] buf) {
        byte[] md5_digest = io_stream.getDigestMD5();
        byte[] sha_digest = io_stream.getDigestSHA();
        byte[] digest = new byte[md5_digest.length + sha_digest.length];
        System.arraycopy(md5_digest, 0, digest, 0, md5_digest.length);
        System.arraycopy(sha_digest, 0, digest, md5_digest.length,
                sha_digest.length);
        try {
            PRF.computePRF(buf, session.master_secret, 
                    label.getBytes(), digest);
        } catch (GeneralSecurityException e) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR, "PRF error", e);
        }
    }
    protected void computerReferenceVerifyDataSSLv3(byte[] sender) {
        verify_data = new byte[36];
        computerVerifyDataSSLv3(sender, verify_data);
    }
    protected void computerVerifyDataSSLv3(byte[] sender, byte[] buf) {
        MessageDigest md5;
        MessageDigest sha;
        try {
            md5 = MessageDigest.getInstance("MD5");
            sha = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR, "Could not initialize the Digest Algorithms.", e);
            return;
        }
        try {
            byte[] hanshake_messages = io_stream.getMessages();
            md5.update(hanshake_messages);
            md5.update(sender);
            md5.update(session.master_secret);
            byte[] b = md5.digest(SSLv3Constants.MD5pad1);
            md5.update(session.master_secret);
            md5.update(SSLv3Constants.MD5pad2);
            System.arraycopy(md5.digest(b), 0, buf, 0, 16);
            sha.update(hanshake_messages);
            sha.update(sender);
            sha.update(session.master_secret);
            b = sha.digest(SSLv3Constants.SHApad1);
            sha.update(session.master_secret);
            sha.update(SSLv3Constants.SHApad2);
            System.arraycopy(sha.digest(b), 0, buf, 16, 20);
        } catch (Exception e) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR, "INTERNAL ERROR", e);
        }
    }
    protected void verifyFinished(byte[] data) {
        if (!Arrays.equals(verify_data, data)) {
            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE, "Incorrect FINISED");
        }
    }
    protected void unexpectedMessage() {
        fatalAlert(AlertProtocol.UNEXPECTED_MESSAGE, "UNEXPECTED MESSAGE");
    }
    public void send(Message message) {
        io_stream.writeUint8(message.getType());
        io_stream.writeUint24(message.length());
        message.send(io_stream);
    }
    public void computerMasterSecret() {
        byte[] seed = new byte[64];
        System.arraycopy(clientHello.getRandom(), 0, seed, 0, 32);
        System.arraycopy(serverHello.getRandom(), 0, seed, 32, 32);
        session.master_secret = new byte[48];
        if (serverHello.server_version[1] == 1) { 
            try {
                PRF.computePRF(session.master_secret, preMasterSecret,
                        master_secret_bytes, seed);
            } catch (GeneralSecurityException e) {
                fatalAlert(AlertProtocol.INTERNAL_ERROR, "PRF error", e);
            }
        } else { 
            PRF.computePRF_SSLv3(session.master_secret, preMasterSecret, seed);
        }
        Arrays.fill(preMasterSecret, (byte)0);
        preMasterSecret = null;        
    }
    public Runnable getTask() {
        if (delegatedTasks.isEmpty()) {
            return null;
        }
        return delegatedTasks.remove(0);
    }
    protected void clearMessages() {
        io_stream.clearBuffer();
        clientHello = null;
        serverHello = null;
        serverCert = null;
        serverKeyExchange = null;
        certificateRequest = null;
        serverHelloDone = null;
        clientCert = null;
        clientKeyExchange = null;
        certificateVerify = null;
        clientFinished = null;
        serverFinished = null;
    }
    protected static int getRSAKeyLength(PublicKey pk)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger mod;
        if (pk instanceof RSAKey) {
            mod = ((RSAKey) pk).getModulus();
        } else {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            mod = kf.getKeySpec(pk, RSAPublicKeySpec.class)
                    .getModulus();
        }
        return mod.bitLength();
    }
    protected void shutdown() {
        clearMessages();
        session = null;
        preMasterSecret = null;
        delegatedTasks.clear();
    }
}
