public class ServerHandshakeImpl extends HandshakeProtocol {
    private PrivateKey privKey;
    public ServerHandshakeImpl(Object owner) {
        super(owner);
        status = NEED_UNWRAP;
    }
    @Override
    public void start() {
        if (session == null) { 
            status = NEED_UNWRAP;
            return; 
        }
        if (clientHello != null && this.status != FINISHED) {
            return; 
        }
        sendHelloRequest();
        status = NEED_UNWRAP;
    }
    @Override
    public void unwrap(byte[] bytes) {
        io_stream.append(bytes);
        while (io_stream.available() > 0) {
            int handshakeType;
            int length;
            io_stream.mark();
            try {
                handshakeType = io_stream.read();
                length = io_stream.readUint24();
                if (io_stream.available() < length) {
                    io_stream.reset();
                    return;
                }
                switch (handshakeType) {
                case 1: 
                    if (clientHello != null && this.status != FINISHED) {
                            unexpectedMessage();
                            return;
                    }
                    needSendHelloRequest = false;
                    clientHello = new ClientHello(io_stream, length);
                    if (nonBlocking) {
                        delegatedTasks.add(new DelegatedTask(new PrivilegedExceptionAction<Void>() {
                            public Void run() throws Exception {
                                processClientHello();
                                return null;
                            }
                        }, this, AccessController.getContext()));
                        return;
                    }
                    processClientHello();
                    break;
                case 11: 
                    if (isResuming || certificateRequest == null
                            || serverHelloDone == null || clientCert != null) {
                        unexpectedMessage();
                        return;
                    }
                    clientCert = new CertificateMessage(io_stream, length);
                    if (clientCert.certs.length == 0) {
                        if (parameters.getNeedClientAuth()) {
                            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                                    "HANDSHAKE FAILURE: no client certificate received");
                        }
                    } else {
                        String authType = clientCert.certs[0].getPublicKey()
                                .getAlgorithm();
                        try {
                            parameters.getTrustManager().checkClientTrusted(
                                    clientCert.certs, authType);
                        } catch (CertificateException e) {
                            fatalAlert(AlertProtocol.BAD_CERTIFICATE,
                                    "Untrusted Client Certificate ", e);
                        }
                        session.peerCertificates = clientCert.certs;
                    }
                    break;
                case 15: 
                    if (isResuming
                            || clientKeyExchange == null
                            || clientCert == null
                            || clientKeyExchange.isEmpty() 
                            || certificateVerify != null
                            || changeCipherSpecReceived) {
                        unexpectedMessage();
                        return;
                    }
                    certificateVerify = new CertificateVerify(io_stream, length);
                    DigitalSignature ds = new DigitalSignature(session.cipherSuite.keyExchange);
                    ds.init(serverCert.certs[0]);                 
                    byte[] md5_hash = null;
                    byte[] sha_hash = null;
                    if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT
                            || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA
                            || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_RSA
                            || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_RSA_EXPORT) {
                        md5_hash = io_stream.getDigestMD5withoutLast();
                        sha_hash = io_stream.getDigestSHAwithoutLast();
                    } else if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_DSS
                            || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_DSS_EXPORT) {
                        sha_hash = io_stream.getDigestSHAwithoutLast();
                    } else if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DH_anon
                            || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DH_anon_EXPORT) {
                    }
                    ds.setMD5(md5_hash);
                    ds.setSHA(sha_hash);
                    if (!ds.verifySignature(certificateVerify.signedHash)) {
                        fatalAlert(AlertProtocol.DECRYPT_ERROR,
                                "DECRYPT ERROR: CERTIFICATE_VERIFY incorrect signature");
                    }
                    break;
                case 16: 
                    if (isResuming
                            || serverHelloDone == null
                            || clientKeyExchange != null
                            || (clientCert == null && parameters
                                    .getNeedClientAuth())) {
                        unexpectedMessage();
                        return;
                    }
                    if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA
                            || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT) {
                        clientKeyExchange = new ClientKeyExchange(io_stream,
                                length, serverHello.server_version[1] == 1,
                                true);
                        Cipher c = null;
                        try {
                            c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                            c.init(Cipher.DECRYPT_MODE, privKey);
                            preMasterSecret = c
                                    .doFinal(clientKeyExchange.exchange_keys);
                            if (preMasterSecret.length != 48
                                    || preMasterSecret[0] != clientHello.client_version[0]
                                    || preMasterSecret[1] != clientHello.client_version[1]) {
                                preMasterSecret = new byte[48];
                                parameters.getSecureRandom().nextBytes(
                                        preMasterSecret);
                            }
                        } catch (Exception e) {
                            fatalAlert(AlertProtocol.INTERNAL_ERROR,
                                    "INTERNAL ERROR", e);
                        }
                    } else { 
                        clientKeyExchange = new ClientKeyExchange(io_stream,
                                length, serverHello.server_version[1] == 1,
                                false);
                        if (clientKeyExchange.isEmpty()) {
                            preMasterSecret = ((DHPublicKey) clientCert.certs[0]
                                    .getPublicKey()).getY().toByteArray();
                        } else {
                            PublicKey clientPublic;
                            KeyAgreement agreement;
                            try {
                                KeyFactory kf = null;
                                try {
                                    kf = KeyFactory.getInstance("DH");
                                } catch (NoSuchAlgorithmException ee) {
                                    kf = KeyFactory
                                            .getInstance("DiffieHellman");
                                }
                                try {
                                    agreement = KeyAgreement.getInstance("DH");
                                } catch (NoSuchAlgorithmException ee) {
                                    agreement = KeyAgreement
                                            .getInstance("DiffieHellman");
                                }
                                clientPublic = kf
                                        .generatePublic(new DHPublicKeySpec(
                                                new BigInteger(
                                                        1,
                                                        clientKeyExchange.exchange_keys),
                                                serverKeyExchange.par1,
                                                serverKeyExchange.par2));
                                agreement.init(privKey);
                                agreement.doPhase(clientPublic, true);
                                preMasterSecret = agreement.generateSecret();
                            } catch (Exception e) {
                                fatalAlert(AlertProtocol.INTERNAL_ERROR,
                                        "INTERNAL ERROR", e);
                                return;
                            }
                        }
                    }
                    computerMasterSecret();
                    break;
                case 20: 
                    if (!isResuming && !changeCipherSpecReceived) {
                        unexpectedMessage();
                        return;
                    }
                    clientFinished = new Finished(io_stream, length);
                    verifyFinished(clientFinished.getData());
                    session.context = parameters.getServerSessionContext();
                    parameters.getServerSessionContext().putSession(session);
                    if (!isResuming) {
                        sendChangeCipherSpec();
                    } else {
                        session.lastAccessedTime = System.currentTimeMillis();
                        status = FINISHED;
                    }
                    break;
                default:
                    unexpectedMessage();
                    return;
                }
            } catch (IOException e) {
                io_stream.reset();
                return;
            }
        }
    }
    @Override
    public void unwrapSSLv2(byte[] bytes) {
        io_stream.append(bytes);
        io_stream.mark();
        try {
            clientHello = new ClientHello(io_stream);
        } catch (IOException e) {
            io_stream.reset();
            return;
        }
        if (nonBlocking) {
            delegatedTasks.add(new DelegatedTask(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws Exception {
                            processClientHello();
                            return null;
                        }
                    }, this, AccessController.getContext()));
            return;
        }
        processClientHello();
    }
    void processClientHello() {
        CipherSuite cipher_suite;
        checkCompression: {
            for (int i = 0; i < clientHello.compression_methods.length; i++) {
                if (clientHello.compression_methods[i] == 0) {
                    break checkCompression;
                }
            }
            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                    "HANDSHAKE FAILURE. Incorrect client hello message");
        }
        if (!ProtocolVersion.isSupported(clientHello.client_version)) {
            fatalAlert(AlertProtocol.PROTOCOL_VERSION, 
                    "PROTOCOL VERSION. Unsupported client version "
                    + clientHello.client_version[0]
                    + clientHello.client_version[1]);        
        }
        isResuming = false;
        FIND: if (clientHello.session_id.length != 0) {
            SSLSessionImpl sessionToResume;
            boolean reuseCurrent = false;
            if (session != null
                    && Arrays.equals(session.id, clientHello.session_id)) {
                if (session.isValid()) {
                    isResuming = true;
                    break FIND;
                }
                reuseCurrent = true;
            }
            sessionToResume = findSessionToResume(clientHello.session_id);
            if (sessionToResume == null || !sessionToResume.isValid()) {
                if (!parameters.getEnableSessionCreation()) {
                    if (reuseCurrent) {
                        sendWarningAlert(AlertProtocol.NO_RENEGOTIATION);
                        status = NOT_HANDSHAKING;
                        clearMessages();
                        return;
                    }
                    fatalAlert(AlertProtocol.HANDSHAKE_FAILURE, "SSL Session may not be created");
                }
                session = null;
            } else {
                session = (SSLSessionImpl)sessionToResume.clone();
                isResuming = true;
            }
        }
        if (isResuming) {
            cipher_suite = session.cipherSuite;
            checkCipherSuite: {
                for (int i = 0; i < clientHello.cipher_suites.length; i++) {
                    if (cipher_suite.equals(clientHello.cipher_suites[i])) {
                        break checkCipherSuite;
                    }
                }
                fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                        "HANDSHAKE FAILURE. Incorrect client hello message");
            }
        } else {
            cipher_suite = selectSuite(clientHello.cipher_suites);
            if (cipher_suite == null) {
                fatalAlert(AlertProtocol.HANDSHAKE_FAILURE, "HANDSHAKE FAILURE. NO COMMON SUITE");
            }
            if (!parameters.getEnableSessionCreation()) {
                fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                        "SSL Session may not be created");
            }
            session = new SSLSessionImpl(cipher_suite, parameters.getSecureRandom());
            session.setPeer(engineOwner.getPeerHost(), engineOwner.getPeerPort());
        }
        recordProtocol.setVersion(clientHello.client_version);
        session.protocol = ProtocolVersion.getByVersion(clientHello.client_version);
        session.clientRandom = clientHello.random;
        serverHello = new ServerHello(parameters.getSecureRandom(), 
                clientHello.client_version,
                session.getId(), cipher_suite, (byte) 0); 
        session.serverRandom = serverHello.random;
        send(serverHello);
        if (isResuming) {
            sendChangeCipherSpec();
            return;
        }
        if (!cipher_suite.isAnonymous()) { 
            X509Certificate[] certs = null;
            String certType = null;
            if (cipher_suite.keyExchange == CipherSuite.KeyExchange_RSA
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_RSA
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_RSA_EXPORT) {
                certType = "RSA";
            } else if (cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_DSS
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_DSS_EXPORT) {
                certType = "DSA";
            } else if (cipher_suite.keyExchange == CipherSuite.KeyExchange_DH_DSS) {
                certType = "DH_DSA";
            } else if (cipher_suite.keyExchange == CipherSuite.KeyExchange_DH_RSA) {
                certType = "DH_RSA";
            }
            String alias = null;
            X509KeyManager km = parameters.getKeyManager();
            if (km instanceof X509ExtendedKeyManager) {
                X509ExtendedKeyManager ekm = (X509ExtendedKeyManager)km;
                    alias = ekm.chooseEngineServerAlias(certType, null,
                            this.engineOwner);
                if (alias != null) {
                    certs = ekm.getCertificateChain(alias);
                }
            } else {
                    certs = km.getCertificateChain(alias);
            }
            if (certs == null) {
                fatalAlert(AlertProtocol.HANDSHAKE_FAILURE, "NO SERVER CERTIFICATE FOUND");
                return;
            }
            session.localCertificates = certs;
            serverCert = new CertificateMessage(certs);
            privKey = parameters.getKeyManager().getPrivateKey(alias);
            send(serverCert);
        }
        RSAPublicKey rsakey = null;
        DHPublicKeySpec dhkeySpec = null;
        byte[] hash = null;
        BigInteger p = null;
        BigInteger g = null;
        KeyPairGenerator kpg = null;
        try {
            if (cipher_suite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT) {
                PublicKey pk = serverCert.certs[0].getPublicKey();                
                if (getRSAKeyLength(pk) > 512) {
                    kpg = KeyPairGenerator.getInstance("RSA");
                    kpg.initialize(512);
                }
            } else if (cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_DSS
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_DSS_EXPORT
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_RSA
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DHE_RSA_EXPORT
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DH_anon
                    || cipher_suite.keyExchange == CipherSuite.KeyExchange_DH_anon_EXPORT) {
                try {
                    kpg = KeyPairGenerator.getInstance("DH");
                } catch (NoSuchAlgorithmException ee) {
                    kpg = KeyPairGenerator.getInstance("DiffieHellman");
                }
                p = new BigInteger(1, DHParameters.getPrime());
                g = new BigInteger("2");
                DHParameterSpec spec = new DHParameterSpec(p, g);
                kpg.initialize(spec);
            }
        } catch (Exception e) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR, "INTERNAL ERROR", e);
        }
        if (kpg != null) {
            DigitalSignature ds = new DigitalSignature(cipher_suite.keyExchange);
            KeyPair kp = null;
            try {
                kp = kpg.genKeyPair();
                if (cipher_suite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT) {
                    rsakey = (RSAPublicKey) kp.getPublic();
                } else {
                    DHPublicKey dhkey = (DHPublicKey) kp.getPublic();
                    KeyFactory kf = null;
                    try {
                        kf = KeyFactory.getInstance("DH");
                    } catch (NoSuchAlgorithmException e) {
                            kf = KeyFactory.getInstance("DiffieHellman");
                    }
                    dhkeySpec = kf.getKeySpec(dhkey,
                            DHPublicKeySpec.class);
                }
                if (!cipher_suite.isAnonymous()) { 
                    ds.init(privKey);
                    privKey = kp.getPrivate();
                    ds.update(clientHello.getRandom());
                    ds.update(serverHello.getRandom());
                    byte[] tmp;
                    byte[] tmpLength = new byte[2];
                    if (cipher_suite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT) {
                        tmp = ServerKeyExchange.toUnsignedByteArray(rsakey.getModulus());
                        tmpLength[0] = (byte) ((tmp.length & 0xFF00) >>> 8);
                        tmpLength[1] = (byte) (tmp.length & 0xFF);
                        ds.update(tmpLength);
                        ds.update(tmp);
                        tmp = ServerKeyExchange.toUnsignedByteArray(rsakey.getPublicExponent());
                        tmpLength[0] = (byte) ((tmp.length & 0xFF00) >>> 8);
                        tmpLength[1] = (byte) (tmp.length & 0xFF);
                        ds.update(tmpLength);
                        ds.update(tmp);
                    } else {
                        tmp = ServerKeyExchange.toUnsignedByteArray(dhkeySpec.getP());
                        tmpLength[0] = (byte) ((tmp.length & 0xFF00) >>> 8);
                        tmpLength[1] = (byte) (tmp.length & 0xFF);
                        ds.update(tmpLength);
                        ds.update(tmp);
                        tmp = ServerKeyExchange.toUnsignedByteArray(dhkeySpec.getG());
                        tmpLength[0] = (byte) ((tmp.length & 0xFF00) >>> 8);
                        tmpLength[1] = (byte) (tmp.length & 0xFF);
                        ds.update(tmpLength);
                        ds.update(tmp);
                        tmp = ServerKeyExchange.toUnsignedByteArray(dhkeySpec.getY());
                        tmpLength[0] = (byte) ((tmp.length & 0xFF00) >>> 8);
                        tmpLength[1] = (byte) (tmp.length & 0xFF);
                        ds.update(tmpLength);
                        ds.update(tmp);
                    }
                    hash = ds.sign();
                } else {
                    privKey = kp.getPrivate(); 
                }
            } catch (Exception e) {
                fatalAlert(AlertProtocol.INTERNAL_ERROR, "INTERNAL ERROR", e);
            }
            if (cipher_suite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT) {
                serverKeyExchange = new ServerKeyExchange(rsakey.getModulus(),
                        rsakey.getPublicExponent(), null, hash);
            } else {
                serverKeyExchange = new ServerKeyExchange(p,
                        g, dhkeySpec.getY(), hash);
            }
            send(serverKeyExchange);
        }
        certRequest: if (parameters.getWantClientAuth()
                || parameters.getNeedClientAuth()) {
            X509Certificate[] accepted;
            try {
                X509TrustManager tm = parameters.getTrustManager();
                accepted = tm.getAcceptedIssuers();
            } catch (ClassCastException e) {
                break certRequest;
            }
            byte[] requestedClientCertTypes = {1, 2}; 
            certificateRequest = new CertificateRequest(
                    requestedClientCertTypes, accepted);
            send(certificateRequest);
        }
        serverHelloDone = new ServerHelloDone();
        send(serverHelloDone);
        status = NEED_UNWRAP;
    }
    @Override
    protected void makeFinished() {
        byte[] verify_data;
        boolean isTLS = (serverHello.server_version[1] == 1); 
        if (isTLS) {
            verify_data = new byte[12];
            computerVerifyDataTLS("server finished", verify_data);
        } else { 
            verify_data = new byte[36];
            computerVerifyDataSSLv3(SSLv3Constants.server, verify_data);
        }
        serverFinished = new Finished(verify_data);
        send(serverFinished);
        if (isResuming) {
            if (isTLS) {
                computerReferenceVerifyDataTLS("client finished");
            } else {
                computerReferenceVerifyDataSSLv3(SSLv3Constants.client);                
            }
            status = NEED_UNWRAP;
        } else {
            session.lastAccessedTime = System.currentTimeMillis();
            status = FINISHED;
        }
    }
    private SSLSessionImpl findSessionToResume(byte[] session_id) {
        return (SSLSessionImpl)parameters.getServerSessionContext().getSession(session_id);
    }
    private CipherSuite selectSuite(CipherSuite[] client_suites) {
        for (int i = 0; i < client_suites.length; i++) {
            if (!client_suites[i].supported) {
                continue;
            }
            for (int j = 0; j < parameters.getEnabledCipherSuitesMember().length; j++) {
                if (client_suites[i].equals(parameters.getEnabledCipherSuitesMember()[j])) {
                    return client_suites[i];
                }
            }
        }
        return null;
    }
    @Override
    public void receiveChangeCipherSpec() {    
        if (isResuming) {
            if (serverFinished == null) {
                unexpectedMessage();
            } else {
                changeCipherSpecReceived = true;
            }
        } else {
            if ((parameters.getNeedClientAuth() && clientCert == null)
                    || clientKeyExchange == null
                    || (clientCert != null && !clientKeyExchange.isEmpty() && certificateVerify == null)) {
                unexpectedMessage();
            } else {
                changeCipherSpecReceived = true;
            }
            if (serverHello.server_version[1] == 1) {
                computerReferenceVerifyDataTLS("client finished");
            } else {
                computerReferenceVerifyDataSSLv3(SSLv3Constants.client);
            }
        }
    }
}
