public class ClientHandshakeImpl extends HandshakeProtocol {  
    ClientHandshakeImpl(Object owner) {
        super(owner);
    }
    @Override
    public void start() {
        if (session == null) { 
            session = findSessionToResume();
        } else { 
            if (clientHello != null && this.status != FINISHED) {
                return; 
            }
            if (!session.isValid()) {
                session = null;
            }
        }
        if (session != null) {
            isResuming = true;
        } else if (parameters.getEnableSessionCreation()){    
            isResuming = false;
            session = new SSLSessionImpl(parameters.getSecureRandom());
            session.setPeer(engineOwner.getPeerHost(), engineOwner.getPeerPort());
            session.protocol = ProtocolVersion.getLatestVersion(parameters
                    .getEnabledProtocols());
            recordProtocol.setVersion(session.protocol.version);
        } else {
            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE, "SSL Session may not be created ");
        }
        startSession();
    }
    private void renegotiateNewSession() {
        if (parameters.getEnableSessionCreation()){    
            isResuming = false;
            session = new SSLSessionImpl(parameters.getSecureRandom());
            session.setPeer(engineOwner.getPeerHost(), engineOwner.getPeerPort());
            session.protocol = ProtocolVersion.getLatestVersion(parameters
                    .getEnabledProtocols());
            recordProtocol.setVersion(session.protocol.version);
            startSession();
        } else {
            status = NOT_HANDSHAKING;
            sendWarningAlert(AlertProtocol.NO_RENEGOTIATION);
        }        
    }
    private void startSession() {
        CipherSuite[] cipher_suites;
        if (isResuming) {
            cipher_suites = new CipherSuite[] { session.cipherSuite };
        } else {
            cipher_suites = parameters.getEnabledCipherSuitesMember();
        }
        clientHello = new ClientHello(parameters.getSecureRandom(),
                session.protocol.version, session.id, cipher_suites);
        session.clientRandom = clientHello.random;
        send(clientHello);
        status = NEED_UNWRAP;
    }
    @Override
    public void unwrap(byte[] bytes) {
        if (this.delegatedTaskErr != null) {
            Exception e = this.delegatedTaskErr;
            this.delegatedTaskErr = null;
            this.fatalAlert(AlertProtocol.HANDSHAKE_FAILURE, "Error in delegated task", e);
        }
        int handshakeType;
        io_stream.append(bytes);
        while (io_stream.available() > 0) {
            io_stream.mark();
            int length;
            try {
                handshakeType = io_stream.read();
                length = io_stream.readUint24();
                if (io_stream.available() < length) {
                    io_stream.reset();
                    return;
                }
                switch (handshakeType) {
                case 0: 
                    io_stream.removeFromMarkedPosition();
                    if (clientHello != null
                            && (clientFinished == null || serverFinished == null)) {
                        break;
                    }
                    if (session.isValid()) {
                        session = (SSLSessionImpl) session.clone();
                        isResuming = true;
                        startSession();
                    } else {
                        renegotiateNewSession();
                    }
                    break;
                case 2: 
                    if (clientHello == null || serverHello != null) {
                        unexpectedMessage();
                        return;
                    }
                    serverHello = new ServerHello(io_stream, length);
                    ProtocolVersion servProt = ProtocolVersion
                            .getByVersion(serverHello.server_version);
                    String[] enabled = parameters.getEnabledProtocols();        
                    find: {
                        for (int i = 0; i < enabled.length; i++) {
                            if (servProt.equals(ProtocolVersion
                                    .getByName(enabled[i]))) {
                                break find;
                            }
                        }
                        fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                                "Bad server hello protocol version");
                    }
                    if (serverHello.compression_method != 0) {
                        fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                                "Bad server hello compression method");
                    }
                    CipherSuite[] enabledSuites = parameters.getEnabledCipherSuitesMember();
                    find: {
                        for (int i = 0; i < enabledSuites.length; i++) {
                            if (serverHello.cipher_suite
                                    .equals(enabledSuites[i])) {
                                break find;
                            }
                        }
                        fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                                "Bad server hello cipher suite");
                    }                
                    if (isResuming) {
                        if (serverHello.session_id.length == 0) {
                            isResuming = false;
                        } else if (!Arrays.equals(serverHello.session_id, clientHello.session_id)) {
                            isResuming = false;
                        } else if (!session.protocol.equals(servProt)) {
                            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                                    "Bad server hello protocol version");                            
                        } else if (!session.cipherSuite
                                .equals(serverHello.cipher_suite)) {
                            fatalAlert(AlertProtocol.HANDSHAKE_FAILURE,
                                    "Bad server hello cipher suite");
                        }
                        if (serverHello.server_version[1] == 1) {
                            computerReferenceVerifyDataTLS("server finished");
                        } else {
                            computerReferenceVerifyDataSSLv3(SSLv3Constants.server);
                        }
                    }
                    session.protocol = servProt;
                    recordProtocol.setVersion(session.protocol.version);
                    session.cipherSuite = serverHello.cipher_suite;
                    session.id = serverHello.session_id.clone();
                    session.serverRandom = serverHello.random;
                    break;
                case 11: 
                    if (serverHello == null || serverKeyExchange != null
                            || serverCert != null || isResuming) {
                        unexpectedMessage();
                        return;
                    }
                    serverCert = new CertificateMessage(io_stream, length);
                    break;
                case 12: 
                    if (serverHello == null || serverKeyExchange != null
                            || isResuming) {
                        unexpectedMessage();
                        return;
                    }
                    serverKeyExchange = new ServerKeyExchange(io_stream,
                            length, session.cipherSuite.keyExchange);
                    break;
                case 13: 
                    if (serverCert == null || certificateRequest != null
                            || session.cipherSuite.isAnonymous() || isResuming) {
                        unexpectedMessage();
                        return;
                    }
                    certificateRequest = new CertificateRequest(io_stream,
                            length);
                    break;
                case 14: 
                    if (serverHello == null || serverHelloDone != null
                            || isResuming) {
                        unexpectedMessage();
                        return;
                    }
                    serverHelloDone = new ServerHelloDone(io_stream, length);
                    if (this.nonBlocking) {
                        delegatedTasks.add(new DelegatedTask(new PrivilegedExceptionAction<Void>() {
                            public Void run() throws Exception {
                                processServerHelloDone();
                                return null;
                            }
                        }, this, AccessController.getContext()));
                        return;
                    }
                    processServerHelloDone();
                    break;
                case 20: 
                    if (!changeCipherSpecReceived) {
                        unexpectedMessage();
                        return;
                    }
                    serverFinished = new Finished(io_stream, length);
                    verifyFinished(serverFinished.getData());
                    session.lastAccessedTime = System.currentTimeMillis();
                    session.context = parameters.getClientSessionContext();
                    parameters.getClientSessionContext().putSession(session);
                    if (isResuming) {
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
        unexpectedMessage();
    }
    @Override
    protected void makeFinished() {
        byte[] verify_data;
        if (serverHello.server_version[1] == 1) {
            verify_data = new byte[12];
            computerVerifyDataTLS("client finished", verify_data);
        } else {
            verify_data = new byte[36];
            computerVerifyDataSSLv3(SSLv3Constants.client, verify_data);
        }
        clientFinished = new Finished(verify_data);
        send(clientFinished);
        if (isResuming) {
            session.lastAccessedTime = System.currentTimeMillis();
            status = FINISHED;
        } else {
            if (serverHello.server_version[1] == 1) {
                computerReferenceVerifyDataTLS("server finished");
            } else {
                computerReferenceVerifyDataSSLv3(SSLv3Constants.server);
            }
            status = NEED_UNWRAP;
        }
    }
    void processServerHelloDone() {
        PrivateKey clientKey = null;
        if (serverCert != null) {
            if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DH_anon
                    || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DH_anon_EXPORT) {
                unexpectedMessage();
                return;
            }
            verifyServerCert();
        } else {
            if (session.cipherSuite.keyExchange != CipherSuite.KeyExchange_DH_anon
                    && session.cipherSuite.keyExchange != CipherSuite.KeyExchange_DH_anon_EXPORT) {
                unexpectedMessage();
                return;
            }
        }
        if (certificateRequest != null) {
            X509Certificate[] certs = null;
            String clientAlias = ((X509ExtendedKeyManager) parameters
                    .getKeyManager()).chooseClientAlias(certificateRequest
                    .getTypesAsString(),
                    certificateRequest.certificate_authorities, null);
            if (clientAlias != null) {
                X509ExtendedKeyManager km = (X509ExtendedKeyManager) parameters
                        .getKeyManager();
                certs = km.getCertificateChain((clientAlias));
                clientKey = km.getPrivateKey(clientAlias);
            }
            session.localCertificates = certs;
            clientCert = new CertificateMessage(certs);
            send(clientCert);
        }
        if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA
                || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT) {
            Cipher c;
            try {
                c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                if (serverKeyExchange != null) {
                    c.init(Cipher.ENCRYPT_MODE, serverKeyExchange
                            .getRSAPublicKey());
                } else {
                    c.init(Cipher.ENCRYPT_MODE, serverCert.certs[0]);
                }
            } catch (Exception e) {
                fatalAlert(AlertProtocol.INTERNAL_ERROR,
                        "Unexpected exception", e);
                return;
            }
            preMasterSecret = new byte[48];
            parameters.getSecureRandom().nextBytes(preMasterSecret);
            System.arraycopy(clientHello.client_version, 0, preMasterSecret, 0,
                    2);
            try {
                clientKeyExchange = new ClientKeyExchange(c
                        .doFinal(preMasterSecret),
                        serverHello.server_version[1] == 1);
            } catch (Exception e) {
                fatalAlert(AlertProtocol.INTERNAL_ERROR,
                        "Unexpected exception", e);
                return;
            }
        } else {
            PublicKey serverPublic;
            KeyAgreement agreement = null;
            DHParameterSpec spec;
            try {
                KeyFactory kf = null;
                try {
                    kf = KeyFactory.getInstance("DH");
                } catch (NoSuchAlgorithmException e) {
                    kf = KeyFactory.getInstance("DiffieHellman");
                }
                try {
                    agreement = KeyAgreement.getInstance("DH");
                } catch (NoSuchAlgorithmException ee) {
                    agreement = KeyAgreement.getInstance("DiffieHellman");
                }
                KeyPairGenerator kpg = null;
                try {
                    kpg = KeyPairGenerator.getInstance("DH");
                } catch (NoSuchAlgorithmException e) {
                    kpg = KeyPairGenerator.getInstance("DiffieHellman");
                }
                if (serverKeyExchange != null) {
                    serverPublic = kf.generatePublic(new DHPublicKeySpec(
                            serverKeyExchange.par3, serverKeyExchange.par1,
                            serverKeyExchange.par2));
                    spec = new DHParameterSpec(serverKeyExchange.par1,
                            serverKeyExchange.par2);
                } else {
                    serverPublic = serverCert.certs[0].getPublicKey();
                    spec = ((DHPublicKey) serverPublic).getParams();
                }
                kpg.initialize(spec);
                KeyPair kp = kpg.generateKeyPair();
                Key key = kp.getPublic();
                if (clientCert != null
                        && serverCert != null
                        && (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_RSA
                                || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_DSS)) {
                    PublicKey client_pk = clientCert.certs[0].getPublicKey();
                    PublicKey server_pk = serverCert.certs[0].getPublicKey();
                    if (client_pk instanceof DHKey
                            && server_pk instanceof DHKey) {
                        if (((DHKey) client_pk).getParams().getG().equals(
                                ((DHKey) server_pk).getParams().getG())
                                && ((DHKey) client_pk).getParams().getP()
                                    .equals(((DHKey) server_pk).getParams().getG())) {
                            clientKeyExchange = new ClientKeyExchange(); 
                        }
                    }
                } else {
                    clientKeyExchange = new ClientKeyExchange(
                            ((DHPublicKey) key).getY());
                }
                key = kp.getPrivate();
                agreement.init(key);
                agreement.doPhase(serverPublic, true);
                preMasterSecret = agreement.generateSecret();
            } catch (Exception e) {
                fatalAlert(AlertProtocol.INTERNAL_ERROR,
                        "Unexpected exception", e);
                return;
            }
        }
        if (clientKeyExchange != null) {
            send(clientKeyExchange);
        }
        computerMasterSecret();
        if (clientCert != null && !clientKeyExchange.isEmpty()) {
            DigitalSignature ds = new DigitalSignature(
                    session.cipherSuite.keyExchange);
            ds.init(clientKey);
            if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA_EXPORT
                    || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_RSA
                    || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_RSA
                    || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_RSA_EXPORT) { 
                ds.setMD5(io_stream.getDigestMD5());
                ds.setSHA(io_stream.getDigestSHA());
            } else if (session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_DSS
                    || session.cipherSuite.keyExchange == CipherSuite.KeyExchange_DHE_DSS_EXPORT) {
                ds.setSHA(io_stream.getDigestSHA());
            }
            certificateVerify = new CertificateVerify(ds.sign());
            send(certificateVerify);
        }
        sendChangeCipherSpec();
    }
    private void verifyServerCert() {
        String authType = null;
        switch (session.cipherSuite.keyExchange) {
        case 1: 
            authType = "RSA";
            break;
        case 2: 
            if (serverKeyExchange != null ) {
                authType = "RSA_EXPORT";
            } else {
                authType = "RSA";
            }
            break;
        case 3: 
        case 4: 
            authType = "DHE_DSS";
            break;
        case 5: 
        case 6: 
            authType = "DHE_RSA";
            break;
        case 7: 
        case 11: 
            authType = "DH_DSS";
            break;
        case 8: 
        case 12: 
            authType = "DH_RSA";
            break;
        case 9: 
        case 10: 
            return;
        }
        try {
            parameters.getTrustManager().checkServerTrusted(serverCert.certs,
                    authType);
        } catch (CertificateException e) {
            fatalAlert(AlertProtocol.BAD_CERTIFICATE, "Not trusted server certificate", e);
            return;
        }
        session.peerCertificates = serverCert.certs;
    }
    @Override
    public void receiveChangeCipherSpec() {
        if (isResuming) {
            if (serverHello == null) {
                unexpectedMessage();
            }
        } else if (clientFinished == null) {
            unexpectedMessage();
        } 
        changeCipherSpecReceived = true;
    }
    private SSLSessionImpl findSessionToResume() {
        String host = null;
        int port = -1;
        if (engineOwner != null) {
            host = engineOwner.getPeerHost();
            port = engineOwner.getPeerPort();
        }
        if (host == null || port == -1) {
            return null; 
        }
        ClientSessionContext context = parameters.getClientSessionContext();
        SSLSessionImpl session
                = (SSLSessionImpl) context.getSession(host, port);
        if (session != null) {
            session = (SSLSessionImpl) session.clone();
        }
        return session;
    }
}
