final public class StartTlsResponseImpl extends StartTlsResponse {
    private static final boolean debug = false;
    private static final int DNSNAME_TYPE = 2;
    private transient String hostname = null;
    private transient Connection ldapConnection = null;
    private transient InputStream originalInputStream = null;
    private transient OutputStream originalOutputStream = null;
    private transient SSLSocket sslSocket = null;
    private transient SSLSocketFactory defaultFactory = null;
    private transient SSLSocketFactory currentFactory = null;
    private transient String[] suites = null;
    private transient HostnameVerifier verifier = null;
    private transient boolean isClosed = true;
    private static final long serialVersionUID = -1126624615143411328L;
    public StartTlsResponseImpl() {}
    public void setEnabledCipherSuites(String[] suites) {
        this.suites = suites;
    }
    public void setHostnameVerifier(HostnameVerifier verifier) {
        this.verifier = verifier;
    }
    public SSLSession negotiate() throws IOException {
        return negotiate(null);
    }
    public SSLSession negotiate(SSLSocketFactory factory) throws IOException {
        if (isClosed && sslSocket != null) {
            throw new IOException("TLS connection is closed.");
        }
        if (factory == null) {
            factory = getDefaultFactory();
        }
        if (debug) {
            System.out.println("StartTLS: About to start handshake");
        }
        SSLSession sslSession = startHandshake(factory).getSession();
        if (debug) {
            System.out.println("StartTLS: Completed handshake");
        }
        SSLPeerUnverifiedException verifExcep = null;
        try {
            if (verify(hostname, sslSession)) {
                isClosed = false;
                return sslSession;
            }
        } catch (SSLPeerUnverifiedException e) {
            verifExcep = e;
        }
        if ((verifier != null) &&
                verifier.verify(hostname, sslSession)) {
            isClosed = false;
            return sslSession;
        }
        close();
        sslSession.invalidate();
        if (verifExcep == null) {
            verifExcep = new SSLPeerUnverifiedException(
                        "hostname of the server '" + hostname +
                        "' does not match the hostname in the " +
                        "server's certificate.");
        }
        throw verifExcep;
    }
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        if (debug) {
            System.out.println("StartTLS: replacing SSL " +
                                "streams with originals");
        }
        ldapConnection.replaceStreams(
                        originalInputStream, originalOutputStream);
        if (debug) {
            System.out.println("StartTLS: closing SSL Socket");
        }
        sslSocket.close();
        isClosed = true;
    }
    public void setConnection(Connection ldapConnection, String hostname) {
        this.ldapConnection = ldapConnection;
        this.hostname = (hostname != null) ? hostname : ldapConnection.host;
        originalInputStream = ldapConnection.inStream;
        originalOutputStream = ldapConnection.outStream;
    }
    private SSLSocketFactory getDefaultFactory() throws IOException {
        if (defaultFactory != null) {
            return defaultFactory;
        }
        return (defaultFactory =
            (SSLSocketFactory) SSLSocketFactory.getDefault());
    }
    private SSLSocket startHandshake(SSLSocketFactory factory)
        throws IOException {
        if (ldapConnection == null) {
            throw new IllegalStateException("LDAP connection has not been set."
                + " TLS requires an existing LDAP connection.");
        }
        if (factory != currentFactory) {
            sslSocket = (SSLSocket) factory.createSocket(ldapConnection.sock,
                ldapConnection.host, ldapConnection.port, false);
            currentFactory = factory;
            if (debug) {
                System.out.println("StartTLS: Created socket : " + sslSocket);
            }
        }
        if (suites != null) {
            sslSocket.setEnabledCipherSuites(suites);
            if (debug) {
                System.out.println("StartTLS: Enabled cipher suites");
            }
        }
        try {
            if (debug) {
                System.out.println(
                        "StartTLS: Calling sslSocket.startHandshake");
            }
            sslSocket.startHandshake();
            if (debug) {
                System.out.println(
                        "StartTLS: + Finished sslSocket.startHandshake");
            }
            ldapConnection.replaceStreams(sslSocket.getInputStream(),
                sslSocket.getOutputStream());
            if (debug) {
                System.out.println("StartTLS: Replaced IO Streams");
            }
        } catch (IOException e) {
            if (debug) {
                System.out.println("StartTLS: Got IO error during handshake");
                e.printStackTrace();
            }
            sslSocket.close();
            isClosed = true;
            throw e;   
        }
        return sslSocket;
    }
    private boolean verify(String hostname, SSLSession session)
        throws SSLPeerUnverifiedException {
        java.security.cert.Certificate[] certs = null;
        if (hostname != null && hostname.startsWith("[") &&
                hostname.endsWith("]")) {
            hostname = hostname.substring(1, hostname.length() - 1);
        }
        try {
            HostnameChecker checker = HostnameChecker.getInstance(
                                                HostnameChecker.TYPE_LDAP);
            if (session.getCipherSuite().startsWith("TLS_KRB5")) {
                Principal principal = getPeerPrincipal(session);
                if (!checker.match(hostname, principal)) {
                    throw new SSLPeerUnverifiedException(
                        "hostname of the kerberos principal:" + principal +
                        " does not match the hostname:" + hostname);
                }
            } else { 
                certs = session.getPeerCertificates();
                X509Certificate peerCert;
                if (certs[0] instanceof java.security.cert.X509Certificate) {
                    peerCert = (java.security.cert.X509Certificate) certs[0];
                } else {
                    throw new SSLPeerUnverifiedException(
                            "Received a non X509Certificate from the server");
                }
                checker.match(hostname, peerCert);
            }
            return true;
        } catch (SSLPeerUnverifiedException e) {
            String cipher = session.getCipherSuite();
            if (cipher != null && (cipher.indexOf("_anon_") != -1)) {
                return true;
            }
            throw e;
        } catch (CertificateException e) {
            throw(SSLPeerUnverifiedException)
                new SSLPeerUnverifiedException("hostname of the server '" +
                                hostname +
                                "' does not match the hostname in the " +
                                "server's certificate.").initCause(e);
        }
    }
    private static Principal getPeerPrincipal(SSLSession session)
            throws SSLPeerUnverifiedException {
        Principal principal;
        try {
            principal = session.getPeerPrincipal();
        } catch (AbstractMethodError e) {
            principal = null;
        }
        return principal;
    }
}
