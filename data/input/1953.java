final class ClientHandshaker extends Handshaker {
    private PublicKey serverKey;
    private PublicKey ephemeralServerKey;
    private BigInteger          serverDH;
    private DHCrypt             dh;
    private ECDHCrypt ecdh;
    private CertificateRequest  certRequest;
    private boolean serverKeyExchangeReceived;
    private ProtocolVersion maxProtocolVersion;
    private final static boolean enableSNIExtension =
            Debug.getBooleanProperty("jsse.enableSNIExtension", true);
    ClientHandshaker(SSLSocketImpl socket, SSLContextImpl context,
            ProtocolList enabledProtocols,
            ProtocolVersion activeProtocolVersion,
            boolean isInitialHandshake, boolean secureRenegotiation,
            byte[] clientVerifyData, byte[] serverVerifyData) {
        super(socket, context, enabledProtocols, true, true,
            activeProtocolVersion, isInitialHandshake, secureRenegotiation,
            clientVerifyData, serverVerifyData);
    }
    ClientHandshaker(SSLEngineImpl engine, SSLContextImpl context,
            ProtocolList enabledProtocols,
            ProtocolVersion activeProtocolVersion,
            boolean isInitialHandshake, boolean secureRenegotiation,
            byte[] clientVerifyData, byte[] serverVerifyData) {
        super(engine, context, enabledProtocols, true, true,
            activeProtocolVersion, isInitialHandshake, secureRenegotiation,
            clientVerifyData, serverVerifyData);
    }
    void processMessage(byte type, int messageLen) throws IOException {
        if (state > type
                && (type != HandshakeMessage.ht_hello_request
                    && state != HandshakeMessage.ht_client_hello)) {
            throw new SSLProtocolException(
                    "Handshake message sequence violation, " + type);
        }
        switch (type) {
        case HandshakeMessage.ht_hello_request:
            this.serverHelloRequest(new HelloRequest(input));
            break;
        case HandshakeMessage.ht_server_hello:
            this.serverHello(new ServerHello(input, messageLen));
            break;
        case HandshakeMessage.ht_certificate:
            if (keyExchange == K_DH_ANON || keyExchange == K_ECDH_ANON
                    || keyExchange == K_KRB5 || keyExchange == K_KRB5_EXPORT) {
                fatalSE(Alerts.alert_unexpected_message,
                    "unexpected server cert chain");
            }
            this.serverCertificate(new CertificateMsg(input));
            serverKey =
                session.getPeerCertificates()[0].getPublicKey();
            break;
        case HandshakeMessage.ht_server_key_exchange:
            serverKeyExchangeReceived = true;
            switch (keyExchange) {
            case K_RSA_EXPORT:
                if (serverKey == null) {
                    throw new SSLProtocolException
                        ("Server did not send certificate message");
                }
                if (!(serverKey instanceof RSAPublicKey)) {
                    throw new SSLProtocolException("Protocol violation:" +
                        " the certificate type must be appropriate for the" +
                        " selected cipher suite's key exchange algorithm");
                }
                if (JsseJce.getRSAKeyLength(serverKey) <= 512) {
                    throw new SSLProtocolException("Protocol violation:" +
                        " server sent a server key exchange message for" +
                        " key exchange " + keyExchange +
                        " when the public key in the server certificate" +
                        " is less than or equal to 512 bits in length");
                }
                try {
                    this.serverKeyExchange(new RSA_ServerKeyExchange(input));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_DH_ANON:
                this.serverKeyExchange(new DH_ServerKeyExchange(
                                                input, protocolVersion));
                break;
            case K_DHE_DSS:
            case K_DHE_RSA:
                try {
                    this.serverKeyExchange(new DH_ServerKeyExchange(
                        input, serverKey,
                        clnt_random.random_bytes, svr_random.random_bytes,
                        messageLen,
                        localSupportedSignAlgs, protocolVersion));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_ECDHE_ECDSA:
            case K_ECDHE_RSA:
            case K_ECDH_ANON:
                try {
                    this.serverKeyExchange(new ECDH_ServerKeyExchange
                        (input, serverKey, clnt_random.random_bytes,
                        svr_random.random_bytes,
                        localSupportedSignAlgs, protocolVersion));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_RSA:
            case K_DH_RSA:
            case K_DH_DSS:
            case K_ECDH_ECDSA:
            case K_ECDH_RSA:
                throw new SSLProtocolException(
                    "Protocol violation: server sent a server key exchange"
                    + "message for key exchange " + keyExchange);
            case K_KRB5:
            case K_KRB5_EXPORT:
                throw new SSLProtocolException(
                    "unexpected receipt of server key exchange algorithm");
            default:
                throw new SSLProtocolException(
                    "unsupported key exchange algorithm = "
                    + keyExchange);
            }
            break;
        case HandshakeMessage.ht_certificate_request:
            if ((keyExchange == K_DH_ANON) || (keyExchange == K_ECDH_ANON)) {
                throw new SSLHandshakeException(
                    "Client authentication requested for "+
                    "anonymous cipher suite.");
            } else if (keyExchange == K_KRB5 || keyExchange == K_KRB5_EXPORT) {
                throw new SSLHandshakeException(
                    "Client certificate requested for "+
                    "kerberos cipher suite.");
            }
            certRequest = new CertificateRequest(input, protocolVersion);
            if (debug != null && Debug.isOn("handshake")) {
                certRequest.print(System.out);
            }
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                Collection<SignatureAndHashAlgorithm> peerSignAlgs =
                                        certRequest.getSignAlgorithms();
                if (peerSignAlgs == null || peerSignAlgs.isEmpty()) {
                    throw new SSLHandshakeException(
                        "No peer supported signature algorithms");
                }
                Collection<SignatureAndHashAlgorithm> supportedPeerSignAlgs =
                    SignatureAndHashAlgorithm.getSupportedAlgorithms(
                                                            peerSignAlgs);
                if (supportedPeerSignAlgs.isEmpty()) {
                    throw new SSLHandshakeException(
                        "No supported signature and hash algorithm in common");
                }
                setPeerSupportedSignAlgs(supportedPeerSignAlgs);
                session.setPeerSupportedSignatureAlgorithms(
                                                supportedPeerSignAlgs);
            }
            break;
        case HandshakeMessage.ht_server_hello_done:
            this.serverHelloDone(new ServerHelloDone(input));
            break;
        case HandshakeMessage.ht_finished:
            this.serverFinished(
                new Finished(protocolVersion, input, cipherSuite));
            break;
        default:
            throw new SSLProtocolException(
                "Illegal client handshake msg, " + type);
        }
        if (state < type) {
            state = type;
        }
    }
    private void serverHelloRequest(HelloRequest mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (state < HandshakeMessage.ht_client_hello) {
            if (!secureRenegotiation && !allowUnsafeRenegotiation) {
                if (activeProtocolVersion.v >= ProtocolVersion.TLS10.v) {
                    warningSE(Alerts.alert_no_renegotiation);
                    invalidated = true;
                } else {
                    fatalSE(Alerts.alert_handshake_failure,
                        "Renegotiation is not allowed");
                }
            } else {
                if (!secureRenegotiation) {
                    if (debug != null && Debug.isOn("handshake")) {
                        System.out.println(
                            "Warning: continue with insecure renegotiation");
                    }
                }
                kickstart();
            }
        }
    }
    private void serverHello(ServerHello mesg) throws IOException {
        serverKeyExchangeReceived = false;
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        ProtocolVersion mesgVersion = mesg.protocolVersion;
        if (!isNegotiable(mesgVersion)) {
            throw new SSLHandshakeException(
                "Server chose " + mesgVersion +
                ", but that protocol version is not enabled or not supported " +
                "by the client.");
        }
        handshakeHash.protocolDetermined(mesgVersion);
        setVersion(mesgVersion);
        RenegotiationInfoExtension serverHelloRI = (RenegotiationInfoExtension)
                    mesg.extensions.get(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (serverHelloRI != null) {
            if (isInitialHandshake) {
                if (!serverHelloRI.isEmpty()) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "The renegotiation_info field is not empty");
                }
                secureRenegotiation = true;
            } else {
                if (!secureRenegotiation) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "Unexpected renegotiation indication extension");
                }
                byte[] verifyData =
                    new byte[clientVerifyData.length + serverVerifyData.length];
                System.arraycopy(clientVerifyData, 0, verifyData,
                        0, clientVerifyData.length);
                System.arraycopy(serverVerifyData, 0, verifyData,
                        clientVerifyData.length, serverVerifyData.length);
                if (!Arrays.equals(verifyData,
                                serverHelloRI.getRenegotiatedConnection())) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "Incorrect verify data in ServerHello " +
                        "renegotiation_info message");
                }
            }
        } else {
            if (isInitialHandshake) {
                if (!allowLegacyHelloMessages) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "Failed to negotiate the use of secure renegotiation");
                }
                secureRenegotiation = false;
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: No renegotiation " +
                                    "indication extension in ServerHello");
                }
            } else {
                if (secureRenegotiation) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "No renegotiation indication extension");
                }
            }
        }
        svr_random = mesg.svr_random;
        if (isNegotiable(mesg.cipherSuite) == false) {
            fatalSE(Alerts.alert_illegal_parameter,
                "Server selected improper ciphersuite " + mesg.cipherSuite);
        }
        setCipherSuite(mesg.cipherSuite);
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            handshakeHash.setFinishedAlg(cipherSuite.prfAlg.getPRFHashAlg());
        }
        if (mesg.compression_method != 0) {
            fatalSE(Alerts.alert_illegal_parameter,
                "compression type not supported, "
                + mesg.compression_method);
        }
        if (session != null) {
            if (session.getSessionId().equals(mesg.sessionId)) {
                CipherSuite sessionSuite = session.getSuite();
                if (cipherSuite != sessionSuite) {
                    throw new SSLProtocolException
                        ("Server returned wrong cipher suite for session");
                }
                ProtocolVersion sessionVersion = session.getProtocolVersion();
                if (protocolVersion != sessionVersion) {
                    throw new SSLProtocolException
                        ("Server resumed session with wrong protocol version");
                }
                if (sessionSuite.keyExchange == K_KRB5 ||
                    sessionSuite.keyExchange == K_KRB5_EXPORT) {
                    Principal localPrincipal = session.getLocalPrincipal();
                    Subject subject = null;
                    try {
                        subject = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Subject>() {
                            public Subject run() throws Exception {
                                return Krb5Helper.getClientSubject(getAccSE());
                            }});
                    } catch (PrivilegedActionException e) {
                        subject = null;
                        if (debug != null && Debug.isOn("session")) {
                            System.out.println("Attempt to obtain" +
                                        " subject failed!");
                        }
                    }
                    if (subject != null) {
                        Set<Principal> principals =
                            subject.getPrincipals(Principal.class);
                        if (!principals.contains(localPrincipal)) {
                            throw new SSLProtocolException("Server resumed" +
                                " session with wrong subject identity");
                        } else {
                            if (debug != null && Debug.isOn("session"))
                                System.out.println("Subject identity is same");
                        }
                    } else {
                        if (debug != null && Debug.isOn("session"))
                            System.out.println("Kerberos credentials are not" +
                                " present in the current Subject; check if " +
                                " javax.security.auth.useSubjectAsCreds" +
                                " system property has been set to false");
                        throw new SSLProtocolException
                            ("Server resumed session with no subject");
                    }
                }
                resumingSession = true;
                state = HandshakeMessage.ht_finished - 1;
                calculateConnectionKeys(session.getMasterSecret());
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% Server resumed " + session);
                }
            } else {
                session = null;
                if (!enableNewSession) {
                    throw new SSLException
                        ("New session creation is disabled");
                }
            }
        }
        if (resumingSession && session != null) {
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                handshakeHash.setCertificateVerifyAlg(null);
            }
            setHandshakeSessionSE(session);
            return;
        }
        for (HelloExtension ext : mesg.extensions.list()) {
            ExtensionType type = ext.type;
            if ((type != ExtensionType.EXT_ELLIPTIC_CURVES)
                    && (type != ExtensionType.EXT_EC_POINT_FORMATS)
                    && (type != ExtensionType.EXT_SERVER_NAME)
                    && (type != ExtensionType.EXT_RENEGOTIATION_INFO)) {
                fatalSE(Alerts.alert_unsupported_extension,
                    "Server sent an unsupported extension: " + type);
            }
        }
        session = new SSLSessionImpl(protocolVersion, cipherSuite,
                            getLocalSupportedSignAlgs(),
                            mesg.sessionId, getHostSE(), getPortSE());
        setHandshakeSessionSE(session);
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("** " + cipherSuite);
        }
    }
    private void serverKeyExchange(RSA_ServerKeyExchange mesg)
            throws IOException, GeneralSecurityException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (!mesg.verify(serverKey, clnt_random, svr_random)) {
            fatalSE(Alerts.alert_handshake_failure,
                "server key exchange invalid");
        }
        ephemeralServerKey = mesg.getPublicKey();
    }
    private void serverKeyExchange(DH_ServerKeyExchange mesg)
            throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        dh = new DHCrypt(mesg.getModulus(), mesg.getBase(),
                                            sslContext.getSecureRandom());
        serverDH = mesg.getServerPublicKey();
    }
    private void serverKeyExchange(ECDH_ServerKeyExchange mesg)
            throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        ECPublicKey key = mesg.getPublicKey();
        ecdh = new ECDHCrypt(key.getParams(), sslContext.getSecureRandom());
        ephemeralServerKey = key;
    }
    private void serverHelloDone(ServerHelloDone mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        input.digestNow();
        PrivateKey signingKey = null;
        if (certRequest != null) {
            X509ExtendedKeyManager km = sslContext.getX509KeyManager();
            ArrayList<String> keytypesTmp = new ArrayList<>(4);
            for (int i = 0; i < certRequest.types.length; i++) {
                String typeName;
                switch (certRequest.types[i]) {
                case CertificateRequest.cct_rsa_sign:
                    typeName = "RSA";
                    break;
                case CertificateRequest.cct_dss_sign:
                    typeName = "DSA";
                    break;
                case CertificateRequest.cct_ecdsa_sign:
                    typeName = JsseJce.isEcAvailable() ? "EC" : null;
                    break;
                case CertificateRequest.cct_rsa_fixed_dh:
                case CertificateRequest.cct_dss_fixed_dh:
                case CertificateRequest.cct_rsa_fixed_ecdh:
                case CertificateRequest.cct_ecdsa_fixed_ecdh:
                case CertificateRequest.cct_rsa_ephemeral_dh:
                case CertificateRequest.cct_dss_ephemeral_dh:
                default:
                    typeName = null;
                    break;
                }
                if ((typeName != null) && (!keytypesTmp.contains(typeName))) {
                    keytypesTmp.add(typeName);
                }
            }
            String alias = null;
            int keytypesTmpSize = keytypesTmp.size();
            if (keytypesTmpSize != 0) {
                String keytypes[] =
                        keytypesTmp.toArray(new String[keytypesTmpSize]);
                if (conn != null) {
                    alias = km.chooseClientAlias(keytypes,
                        certRequest.getAuthorities(), conn);
                } else {
                    alias = km.chooseEngineClientAlias(keytypes,
                        certRequest.getAuthorities(), engine);
                }
            }
            CertificateMsg m1 = null;
            if (alias != null) {
                X509Certificate[] certs = km.getCertificateChain(alias);
                if ((certs != null) && (certs.length != 0)) {
                    PublicKey publicKey = certs[0].getPublicKey();
                    if (publicKey instanceof ECPublicKey) {
                        ECParameterSpec params =
                            ((ECPublicKey)publicKey).getParams();
                        int index =
                            SupportedEllipticCurvesExtension.getCurveIndex(
                                params);
                        if (!SupportedEllipticCurvesExtension.isSupported(
                                index)) {
                            publicKey = null;
                        }
                    }
                    if (publicKey != null) {
                        m1 = new CertificateMsg(certs);
                        signingKey = km.getPrivateKey(alias);
                        session.setLocalPrivateKey(signingKey);
                        session.setLocalCertificates(certs);
                    }
                }
            }
            if (m1 == null) {
                if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
                    m1 = new CertificateMsg(new X509Certificate [0]);
                } else {
                    warningSE(Alerts.alert_no_certificate);
                }
            }
            if (m1 != null) {
                if (debug != null && Debug.isOn("handshake")) {
                    m1.print(System.out);
                }
                m1.write(output);
            }
        }
        HandshakeMessage m2;
        switch (keyExchange) {
        case K_RSA:
        case K_RSA_EXPORT:
            if (serverKey == null) {
                throw new SSLProtocolException
                        ("Server did not send certificate message");
            }
            if (!(serverKey instanceof RSAPublicKey)) {
                throw new SSLProtocolException
                        ("Server certificate does not include an RSA key");
            }
            PublicKey key;
            if (keyExchange == K_RSA) {
                key = serverKey;
            } else {    
                if (JsseJce.getRSAKeyLength(serverKey) <= 512) {
                    key = serverKey;
                } else {
                    if (ephemeralServerKey == null) {
                        throw new SSLProtocolException("Server did not send" +
                            " a RSA_EXPORT Server Key Exchange message");
                    }
                    key = ephemeralServerKey;
                }
            }
            m2 = new RSAClientKeyExchange(protocolVersion, maxProtocolVersion,
                                sslContext.getSecureRandom(), key);
            break;
        case K_DH_RSA:
        case K_DH_DSS:
            m2 = new DHClientKeyExchange();
            break;
        case K_DHE_RSA:
        case K_DHE_DSS:
        case K_DH_ANON:
            if (dh == null) {
                throw new SSLProtocolException
                    ("Server did not send a DH Server Key Exchange message");
            }
            m2 = new DHClientKeyExchange(dh.getPublicKey());
            break;
        case K_ECDHE_RSA:
        case K_ECDHE_ECDSA:
        case K_ECDH_ANON:
            if (ecdh == null) {
                throw new SSLProtocolException
                    ("Server did not send a ECDH Server Key Exchange message");
            }
            m2 = new ECDHClientKeyExchange(ecdh.getPublicKey());
            break;
        case K_ECDH_RSA:
        case K_ECDH_ECDSA:
            if (serverKey == null) {
                throw new SSLProtocolException
                        ("Server did not send certificate message");
            }
            if (serverKey instanceof ECPublicKey == false) {
                throw new SSLProtocolException
                        ("Server certificate does not include an EC key");
            }
            ECParameterSpec params = ((ECPublicKey)serverKey).getParams();
            ecdh = new ECDHCrypt(params, sslContext.getSecureRandom());
            m2 = new ECDHClientKeyExchange(ecdh.getPublicKey());
            break;
        case K_KRB5:
        case K_KRB5_EXPORT:
            String hostname = getHostSE();
            if (hostname == null) {
                throw new IOException("Hostname is required" +
                                " to use Kerberos cipher suites");
            }
            KerberosClientKeyExchange kerberosMsg =
                new KerberosClientKeyExchange(
                    hostname, isLoopbackSE(), getAccSE(), protocolVersion,
                sslContext.getSecureRandom());
            session.setPeerPrincipal(kerberosMsg.getPeerPrincipal());
            session.setLocalPrincipal(kerberosMsg.getLocalPrincipal());
            m2 = kerberosMsg;
            break;
        default:
            throw new RuntimeException
                                ("Unsupported key exchange: " + keyExchange);
        }
        if (debug != null && Debug.isOn("handshake")) {
            m2.print(System.out);
        }
        m2.write(output);
        output.doHashes();
        output.flush();
        SecretKey preMasterSecret;
        switch (keyExchange) {
        case K_RSA:
        case K_RSA_EXPORT:
            preMasterSecret = ((RSAClientKeyExchange)m2).preMaster;
            break;
        case K_KRB5:
        case K_KRB5_EXPORT:
            byte[] secretBytes =
                ((KerberosClientKeyExchange)m2).getUnencryptedPreMasterSecret();
            preMasterSecret = new SecretKeySpec(secretBytes,
                "TlsPremasterSecret");
            break;
        case K_DHE_RSA:
        case K_DHE_DSS:
        case K_DH_ANON:
            preMasterSecret = dh.getAgreedSecret(serverDH);
            break;
        case K_ECDHE_RSA:
        case K_ECDHE_ECDSA:
        case K_ECDH_ANON:
            preMasterSecret = ecdh.getAgreedSecret(ephemeralServerKey);
            break;
        case K_ECDH_RSA:
        case K_ECDH_ECDSA:
            preMasterSecret = ecdh.getAgreedSecret(serverKey);
            break;
        default:
            throw new IOException("Internal error: unknown key exchange "
                + keyExchange);
        }
        calculateKeys(preMasterSecret, null);
        if (signingKey != null) {
            CertificateVerify m3;
            try {
                SignatureAndHashAlgorithm preferableSignatureAlgorithm = null;
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    preferableSignatureAlgorithm =
                        SignatureAndHashAlgorithm.getPreferableAlgorithm(
                            peerSupportedSignAlgs, signingKey.getAlgorithm());
                    if (preferableSignatureAlgorithm == null) {
                        throw new SSLHandshakeException(
                            "No supported signature algorithm");
                    }
                    String hashAlg =
                        SignatureAndHashAlgorithm.getHashAlgorithmName(
                                preferableSignatureAlgorithm);
                    if (hashAlg == null || hashAlg.length() == 0) {
                        throw new SSLHandshakeException(
                                "No supported hash algorithm");
                    }
                    handshakeHash.setCertificateVerifyAlg(hashAlg);
                }
                m3 = new CertificateVerify(protocolVersion, handshakeHash,
                    signingKey, session.getMasterSecret(),
                    sslContext.getSecureRandom(),
                    preferableSignatureAlgorithm);
            } catch (GeneralSecurityException e) {
                fatalSE(Alerts.alert_handshake_failure,
                    "Error signing certificate verify", e);
                m3 = null;
            }
            if (debug != null && Debug.isOn("handshake")) {
                m3.print(System.out);
            }
            m3.write(output);
            output.doHashes();
        } else {
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                handshakeHash.setCertificateVerifyAlg(null);
            }
        }
        sendChangeCipherAndFinish(false);
    }
    private void serverFinished(Finished mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        boolean verified = mesg.verify(handshakeHash, Finished.SERVER,
            session.getMasterSecret());
        if (!verified) {
            fatalSE(Alerts.alert_illegal_parameter,
                       "server 'finished' message doesn't verify");
        }
        if (secureRenegotiation) {
            serverVerifyData = mesg.getVerifyData();
        }
        if (resumingSession) {
            input.digestNow();
            sendChangeCipherAndFinish(true);
        }
        session.setLastAccessedTime(System.currentTimeMillis());
        if (!resumingSession) {
            if (session.isRejoinable()) {
                ((SSLSessionContextImpl) sslContext
                        .engineGetClientSessionContext())
                        .put(session);
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% Cached client session: " + session);
                }
            } else if (debug != null && Debug.isOn("session")) {
                System.out.println(
                    "%% Didn't cache non-resumable client session: "
                    + session);
            }
        }
    }
    private void sendChangeCipherAndFinish(boolean finishedTag)
            throws IOException {
        Finished mesg = new Finished(protocolVersion, handshakeHash,
            Finished.CLIENT, session.getMasterSecret(), cipherSuite);
        sendChangeCipherSpec(mesg, finishedTag);
        if (secureRenegotiation) {
            clientVerifyData = mesg.getVerifyData();
        }
        state = HandshakeMessage.ht_finished - 1;
    }
    HandshakeMessage getKickstartMessage() throws SSLException {
        SessionId sessionId = SSLSessionImpl.nullSession.getSessionId();
        CipherSuiteList cipherSuites = getActiveCipherSuites();
        maxProtocolVersion = protocolVersion;
        session = ((SSLSessionContextImpl)sslContext
                        .engineGetClientSessionContext())
                        .get(getHostSE(), getPortSE());
        if (debug != null && Debug.isOn("session")) {
            if (session != null) {
                System.out.println("%% Client cached "
                    + session
                    + (session.isRejoinable() ? "" : " (not rejoinable)"));
            } else {
                System.out.println("%% No cached client session");
            }
        }
        if ((session != null) && (session.isRejoinable() == false)) {
            session = null;
        }
        if (session != null) {
            CipherSuite sessionSuite = session.getSuite();
            ProtocolVersion sessionVersion = session.getProtocolVersion();
            if (isNegotiable(sessionSuite) == false) {
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% can't resume, unavailable cipher");
                }
                session = null;
            }
            if ((session != null) && !isNegotiable(sessionVersion)) {
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% can't resume, protocol disabled");
                }
                session = null;
            }
            if (session != null) {
                if (debug != null) {
                    if (Debug.isOn("handshake") || Debug.isOn("session")) {
                        System.out.println("%% Try resuming " + session
                            + " from port " + getLocalPortSE());
                    }
                }
                sessionId = session.getSessionId();
                maxProtocolVersion = sessionVersion;
                setVersion(sessionVersion);
            }
            if (!enableNewSession) {
                if (session == null) {
                    throw new SSLHandshakeException(
                        "Can't reuse existing SSL client session");
                }
                Collection<CipherSuite> cipherList = new ArrayList<>(2);
                cipherList.add(sessionSuite);
                if (!secureRenegotiation &&
                        cipherSuites.contains(CipherSuite.C_SCSV)) {
                    cipherList.add(CipherSuite.C_SCSV);
                }   
                cipherSuites = new CipherSuiteList(cipherList);
            }
        }
        if (session == null && !enableNewSession) {
            throw new SSLHandshakeException("No existing session to resume");
        }
        if (secureRenegotiation && cipherSuites.contains(CipherSuite.C_SCSV)) {
            Collection<CipherSuite> cipherList =
                        new ArrayList<>(cipherSuites.size() - 1);
            for (CipherSuite suite : cipherSuites.collection()) {
                if (suite != CipherSuite.C_SCSV) {
                    cipherList.add(suite);
                }
            }
            cipherSuites = new CipherSuiteList(cipherList);
        }
        boolean negotiable = false;
        for (CipherSuite suite : cipherSuites.collection()) {
            if (isNegotiable(suite)) {
                negotiable = true;
                break;
            }
        }
        if (!negotiable) {
            throw new SSLHandshakeException("No negotiable cipher suite");
        }
        ClientHello clientHelloMessage = new ClientHello(
                sslContext.getSecureRandom(), maxProtocolVersion,
                sessionId, cipherSuites);
        if (maxProtocolVersion.v >= ProtocolVersion.TLS12.v) {
            Collection<SignatureAndHashAlgorithm> localSignAlgs =
                                                getLocalSupportedSignAlgs();
            if (localSignAlgs.isEmpty()) {
                throw new SSLHandshakeException(
                            "No supported signature algorithm");
            }
            clientHelloMessage.addSignatureAlgorithmsExtension(localSignAlgs);
        }
        if (enableSNIExtension) {
            String hostname = getRawHostnameSE();
            if (hostname != null && hostname.indexOf('.') > 0 &&
                    !IPAddressUtil.isIPv4LiteralAddress(hostname) &&
                    !IPAddressUtil.isIPv6LiteralAddress(hostname)) {
                clientHelloMessage.addServerNameIndicationExtension(hostname);
            }
        }
        clnt_random = clientHelloMessage.clnt_random;
        if (secureRenegotiation ||
                !cipherSuites.contains(CipherSuite.C_SCSV)) {
            clientHelloMessage.addRenegotiationInfoExtension(clientVerifyData);
        }
        return clientHelloMessage;
    }
    void handshakeAlert(byte description) throws SSLProtocolException {
        String message = Alerts.alertDescription(description);
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("SSL - handshake alert: " + message);
        }
        throw new SSLProtocolException("handshake alert:  " + message);
    }
    private void serverCertificate(CertificateMsg mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        X509Certificate[] peerCerts = mesg.getCertificateChain();
        if (peerCerts.length == 0) {
            fatalSE(Alerts.alert_bad_certificate,
                "empty certificate chain");
        }
        X509TrustManager tm = sslContext.getX509TrustManager();
        try {
            String keyExchangeString;
            if (keyExchange == K_RSA_EXPORT && !serverKeyExchangeReceived) {
                keyExchangeString = K_RSA.name;
            } else {
                keyExchangeString = keyExchange.name;
            }
            if (tm instanceof X509ExtendedTrustManager) {
                if (conn != null) {
                    ((X509ExtendedTrustManager)tm).checkServerTrusted(
                        peerCerts.clone(),
                        keyExchangeString,
                        conn);
                } else {
                    ((X509ExtendedTrustManager)tm).checkServerTrusted(
                        peerCerts.clone(),
                        keyExchangeString,
                        engine);
                }
            } else {
                throw new CertificateException(
                    "Improper X509TrustManager implementation");
            }
        } catch (CertificateException e) {
            fatalSE(Alerts.alert_certificate_unknown, e);
        }
        session.setPeerCertificates(peerCerts);
    }
}
