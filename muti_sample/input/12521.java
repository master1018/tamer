final public class SSLSocketImpl extends BaseSSLSocketImpl {
    private static final int    cs_START = 0;
    private static final int    cs_HANDSHAKE = 1;
    private static final int    cs_DATA = 2;
    private static final int    cs_RENEGOTIATE = 3;
    private static final int    cs_ERROR = 4;
    private static final int   cs_SENT_CLOSE = 5;
    private static final int    cs_CLOSED = 6;
    private static final int    cs_APP_CLOSED = 7;
    private int                 connectionState;
    private boolean             expectingFinished;
    private SSLException        closeReason;
    private byte                doClientAuth;
    private boolean             roleIsServer;
    private boolean             enableSessionCreation = true;
    private String              host;
    private boolean             autoClose = true;
    private AccessControlContext acc;
    private String              rawHostname;
    private CipherSuiteList     enabledCipherSuites;
    private String              identificationProtocol = null;
    private AlgorithmConstraints    algorithmConstraints = null;
    final private Object        handshakeLock = new Object();
    final ReentrantLock         writeLock = new ReentrantLock();
    final private Object        readLock = new Object();
    private InputRecord         inrec;
    private MAC                 readMAC, writeMAC;
    private CipherBox           readCipher, writeCipher;
    private boolean             secureRenegotiation;
    private byte[]              clientVerifyData;
    private byte[]              serverVerifyData;
    private SSLContextImpl      sslContext;
    private Handshaker                  handshaker;
    private SSLSessionImpl              sess;
    private volatile SSLSessionImpl     handshakeSession;
    private HashMap<HandshakeCompletedListener, AccessControlContext>
                                                        handshakeListeners;
    private InputStream         sockInput;
    private OutputStream        sockOutput;
    private AppInputStream      input;
    private AppOutputStream     output;
    private ProtocolList enabledProtocols;
    private ProtocolVersion     protocolVersion = ProtocolVersion.DEFAULT;
    private static final Debug debug = Debug.getInstance("ssl");
    SSLSocketImpl(SSLContextImpl context, String host, int port)
            throws IOException, UnknownHostException {
        super();
        this.host = host;
        this.rawHostname = host;
        init(context, false);
        SocketAddress socketAddress =
               host != null ? new InetSocketAddress(host, port) :
               new InetSocketAddress(InetAddress.getByName(null), port);
        connect(socketAddress, 0);
    }
    SSLSocketImpl(SSLContextImpl context, InetAddress host, int port)
            throws IOException {
        super();
        init(context, false);
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        connect(socketAddress, 0);
    }
    SSLSocketImpl(SSLContextImpl context, String host, int port,
            InetAddress localAddr, int localPort)
            throws IOException, UnknownHostException {
        super();
        this.host = host;
        this.rawHostname = host;
        init(context, false);
        bind(new InetSocketAddress(localAddr, localPort));
        SocketAddress socketAddress =
               host != null ? new InetSocketAddress(host, port) :
               new InetSocketAddress(InetAddress.getByName(null), port);
        connect(socketAddress, 0);
    }
    SSLSocketImpl(SSLContextImpl context, InetAddress host, int port,
            InetAddress localAddr, int localPort)
            throws IOException {
        super();
        init(context, false);
        bind(new InetSocketAddress(localAddr, localPort));
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        connect(socketAddress, 0);
    }
    SSLSocketImpl(SSLContextImpl context, boolean serverMode,
            CipherSuiteList suites, byte clientAuth,
            boolean sessionCreation, ProtocolList protocols,
            String identificationProtocol,
            AlgorithmConstraints algorithmConstraints) throws IOException {
        super();
        doClientAuth = clientAuth;
        enableSessionCreation = sessionCreation;
        this.identificationProtocol = identificationProtocol;
        this.algorithmConstraints = algorithmConstraints;
        init(context, serverMode);
        enabledCipherSuites = suites;
        enabledProtocols = protocols;
    }
    SSLSocketImpl(SSLContextImpl context) {
        super();
        init(context, false);
    }
    SSLSocketImpl(SSLContextImpl context, Socket sock, String host,
            int port, boolean autoClose) throws IOException {
        super(sock);
        if (!sock.isConnected()) {
            throw new SocketException("Underlying socket is not connected");
        }
        this.host = host;
        this.rawHostname = host;
        init(context, false);
        this.autoClose = autoClose;
        doneConnect();
    }
    private void init(SSLContextImpl context, boolean isServer) {
        sslContext = context;
        sess = SSLSessionImpl.nullSession;
        handshakeSession = null;
        roleIsServer = isServer;
        connectionState = cs_START;
        readCipher = CipherBox.NULL;
        readMAC = MAC.NULL;
        writeCipher = CipherBox.NULL;
        writeMAC = MAC.NULL;
        secureRenegotiation = false;
        clientVerifyData = new byte[0];
        serverVerifyData = new byte[0];
        enabledCipherSuites =
                sslContext.getDefaultCipherSuiteList(roleIsServer);
        enabledProtocols =
                sslContext.getDefaultProtocolList(roleIsServer);
        inrec = null;
        acc = AccessController.getContext();
        input = new AppInputStream(this);
        output = new AppOutputStream(this);
    }
    public void connect(SocketAddress endpoint, int timeout)
            throws IOException {
        if (self != this) {
            throw new SocketException("Already connected");
        }
        if (!(endpoint instanceof InetSocketAddress)) {
            throw new SocketException(
                                  "Cannot handle non-Inet socket addresses.");
        }
        super.connect(endpoint, timeout);
        doneConnect();
    }
    void doneConnect() throws IOException {
        if (self == this) {
            sockInput = super.getInputStream();
            sockOutput = super.getOutputStream();
        } else {
            sockInput = self.getInputStream();
            sockOutput = self.getOutputStream();
        }
        initHandshaker();
    }
    synchronized private int getConnectionState() {
        return connectionState;
    }
    synchronized private void setConnectionState(int state) {
        connectionState = state;
    }
    AccessControlContext getAcc() {
        return acc;
    }
    void writeRecord(OutputRecord r) throws IOException {
    loop:
        while (r.contentType() == Record.ct_application_data) {
            switch (getConnectionState()) {
            case cs_HANDSHAKE:
                performInitialHandshake();
                break;
            case cs_DATA:
            case cs_RENEGOTIATE:
                break loop;
            case cs_ERROR:
                fatal(Alerts.alert_close_notify,
                    "error while writing to socket");
                break; 
            case cs_SENT_CLOSE:
            case cs_CLOSED:
            case cs_APP_CLOSED:
                if (closeReason != null) {
                    throw closeReason;
                } else {
                    throw new SocketException("Socket closed");
                }
            default:
                throw new SSLProtocolException("State error, send app data");
            }
        }
        if (!r.isEmpty()) {
            if (r.isAlert(Alerts.alert_close_notify) && getSoLinger() >= 0) {
                boolean interrupted = Thread.interrupted();
                try {
                    if (writeLock.tryLock(getSoLinger(), TimeUnit.SECONDS)) {
                        try {
                            writeRecordInternal(r);
                        } finally {
                            writeLock.unlock();
                        }
                    } else {
                        SSLException ssle = new SSLException(
                                "SO_LINGER timeout," +
                                " close_notify message cannot be sent.");
                        if (self != this && !autoClose) {
                            fatal((byte)(-1), ssle);
                        } else if ((debug != null) && Debug.isOn("ssl")) {
                            System.out.println(threadName() +
                                ", received Exception: " + ssle);
                        }
                        sess.invalidate();
                    }
                } catch (InterruptedException ie) {
                    interrupted = true;
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            } else {
                writeLock.lock();
                try {
                    writeRecordInternal(r);
                } finally {
                    writeLock.unlock();
                }
            }
        }
    }
    private void writeRecordInternal(OutputRecord r) throws IOException {
        r.addMAC(writeMAC);
        r.encrypt(writeCipher);
        r.write(sockOutput);
        if (connectionState < cs_ERROR) {
            checkSequenceNumber(writeMAC, r.contentType());
        }
    }
    void readDataRecord(InputRecord r) throws IOException {
        if (getConnectionState() == cs_HANDSHAKE) {
            performInitialHandshake();
        }
        readRecord(r, true);
    }
    private void readRecord(InputRecord r, boolean needAppData)
            throws IOException {
        int state;
      synchronized (readLock) {
        while (((state = getConnectionState()) != cs_CLOSED) &&
                (state != cs_ERROR) && (state != cs_APP_CLOSED)) {
            try {
                r.setAppDataValid(false);
                r.read(sockInput, sockOutput);
            } catch (SSLProtocolException e) {
                try {
                    fatal(Alerts.alert_unexpected_message, e);
                } catch (IOException x) {
                }
                throw e;
            } catch (EOFException eof) {
                boolean handshaking = (getConnectionState() <= cs_HANDSHAKE);
                boolean rethrow = requireCloseNotify || handshaking;
                if ((debug != null) && Debug.isOn("ssl")) {
                    System.out.println(threadName() +
                        ", received EOFException: "
                        + (rethrow ? "error" : "ignored"));
                }
                if (rethrow) {
                    SSLException e;
                    if (handshaking) {
                        e = new SSLHandshakeException
                            ("Remote host closed connection during handshake");
                    } else {
                        e = new SSLProtocolException
                            ("Remote host closed connection incorrectly");
                    }
                    e.initCause(eof);
                    throw e;
                } else {
                    closeInternal(false);
                    continue;
                }
            }
            try {
                r.decrypt(readCipher);
            } catch (BadPaddingException e) {
                r.checkMAC(readMAC);
                byte alertType = (r.contentType() == Record.ct_handshake)
                                        ? Alerts.alert_handshake_failure
                                        : Alerts.alert_bad_record_mac;
                fatal(alertType, "Invalid padding", e);
            }
            if (!r.checkMAC(readMAC)) {
                if (r.contentType() == Record.ct_handshake) {
                    fatal(Alerts.alert_handshake_failure,
                        "bad handshake record MAC");
                } else {
                    fatal(Alerts.alert_bad_record_mac, "bad record MAC");
                }
            }
            synchronized (this) {
              switch (r.contentType()) {
                case Record.ct_handshake:
                    initHandshaker();
                    if (!handshaker.activated()) {
                        if (connectionState == cs_RENEGOTIATE) {
                            handshaker.activate(protocolVersion);
                        } else {
                            handshaker.activate(null);
                        }
                    }
                    handshaker.process_record(r, expectingFinished);
                    expectingFinished = false;
                    if (handshaker.invalidated) {
                        handshaker = null;
                        if (connectionState == cs_RENEGOTIATE) {
                            connectionState = cs_DATA;
                        }
                    } else if (handshaker.isDone()) {
                        secureRenegotiation =
                                        handshaker.isSecureRenegotiation();
                        clientVerifyData = handshaker.getClientVerifyData();
                        serverVerifyData = handshaker.getServerVerifyData();
                        sess = handshaker.getSession();
                        handshakeSession = null;
                        handshaker = null;
                        connectionState = cs_DATA;
                        if (handshakeListeners != null) {
                            HandshakeCompletedEvent event =
                                new HandshakeCompletedEvent(this, sess);
                            Thread t = new NotifyHandshakeThread(
                                handshakeListeners.entrySet(), event);
                            t.start();
                        }
                    }
                    if (needAppData || connectionState != cs_DATA) {
                        continue;
                    }
                    break;
                case Record.ct_application_data:
                    if (connectionState != cs_DATA
                            && connectionState != cs_RENEGOTIATE
                            && connectionState != cs_SENT_CLOSE) {
                        throw new SSLProtocolException(
                            "Data received in non-data state: " +
                            connectionState);
                    }
                    if (expectingFinished) {
                        throw new SSLProtocolException
                                ("Expecting finished message, received data");
                    }
                    if (!needAppData) {
                        throw new SSLException("Discarding app data");
                    }
                    r.setAppDataValid(true);
                    break;
                case Record.ct_alert:
                    recvAlert(r);
                    continue;
                case Record.ct_change_cipher_spec:
                    if ((connectionState != cs_HANDSHAKE
                                && connectionState != cs_RENEGOTIATE)
                            || r.available() != 1
                            || r.read() != 1) {
                        fatal(Alerts.alert_unexpected_message,
                            "illegal change cipher spec msg, state = "
                            + connectionState);
                    }
                    changeReadCiphers();
                    expectingFinished = true;
                    continue;
                default:
                    if (debug != null && Debug.isOn("ssl")) {
                        System.out.println(threadName() +
                            ", Received record type: "
                            + r.contentType());
                    }
                    continue;
              } 
              if (connectionState < cs_ERROR) {
                  checkSequenceNumber(readMAC, r.contentType());
              }
              return;
            } 
        }
        r.close();
        return;
      }  
    }
    private void checkSequenceNumber(MAC mac, byte type)
            throws IOException {
        if (connectionState >= cs_ERROR || mac == MAC.NULL) {
            return;
        }
        if (mac.seqNumOverflow()) {
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() +
                    ", sequence number extremely close to overflow " +
                    "(2^64-1 packets). Closing connection.");
            }
            fatal(Alerts.alert_handshake_failure, "sequence number overflow");
        }
        if ((type != Record.ct_handshake) && mac.seqNumIsHuge()) {
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() + ", request renegotiation " +
                        "to avoid sequence number overflow");
            }
            startHandshake();
        }
    }
    AppInputStream getAppInputStream() {
        return input;
    }
    AppOutputStream getAppOutputStream() {
        return output;
    }
    private void initHandshaker() {
        switch (connectionState) {
        case cs_START:
        case cs_DATA:
            break;
        case cs_HANDSHAKE:
        case cs_RENEGOTIATE:
            return;
        default:
            throw new IllegalStateException("Internal error");
        }
        if (connectionState == cs_START) {
            connectionState = cs_HANDSHAKE;
        } else { 
            connectionState = cs_RENEGOTIATE;
        }
        if (roleIsServer) {
            handshaker = new ServerHandshaker(this, sslContext,
                    enabledProtocols, doClientAuth,
                    protocolVersion, connectionState == cs_HANDSHAKE,
                    secureRenegotiation, clientVerifyData, serverVerifyData);
        } else {
            handshaker = new ClientHandshaker(this, sslContext,
                    enabledProtocols,
                    protocolVersion, connectionState == cs_HANDSHAKE,
                    secureRenegotiation, clientVerifyData, serverVerifyData);
        }
        handshaker.setEnabledCipherSuites(enabledCipherSuites);
        handshaker.setEnableSessionCreation(enableSessionCreation);
    }
    private void performInitialHandshake() throws IOException {
        synchronized (handshakeLock) {
            if (getConnectionState() == cs_HANDSHAKE) {
                kickstartHandshake();
                if (inrec == null) {
                    inrec = new InputRecord();
                    inrec.setHandshakeHash(input.r.getHandshakeHash());
                    inrec.setHelloVersion(input.r.getHelloVersion());
                    inrec.enableFormatChecks();
                }
                readRecord(inrec, false);
                inrec = null;
            }
        }
    }
    public void startHandshake() throws IOException {
        startHandshake(true);
    }
    private void startHandshake(boolean resumable) throws IOException {
        checkWrite();
        try {
            if (getConnectionState() == cs_HANDSHAKE) {
                performInitialHandshake();
            } else {
                kickstartHandshake();
            }
        } catch (Exception e) {
            handleException(e, resumable);
        }
    }
    private synchronized void kickstartHandshake() throws IOException {
        switch (connectionState) {
        case cs_HANDSHAKE:
            break;
        case cs_DATA:
            if (!secureRenegotiation && !Handshaker.allowUnsafeRenegotiation) {
                throw new SSLHandshakeException(
                        "Insecure renegotiation is not allowed");
            }
            if (!secureRenegotiation) {
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println(
                        "Warning: Using insecure renegotiation");
                }
            }
            initHandshaker();
            break;
        case cs_RENEGOTIATE:
            return;
        case cs_START:
            throw new SocketException(
                "handshaking attempted on unconnected socket");
        default:
            throw new SocketException("connection is closed");
        }
        if (!handshaker.activated()) {
            if (connectionState == cs_RENEGOTIATE) {
                handshaker.activate(protocolVersion);
            } else {
                handshaker.activate(null);
            }
            if (handshaker instanceof ClientHandshaker) {
                handshaker.kickstart();
            } else {
                if (connectionState == cs_HANDSHAKE) {
                } else {
                    handshaker.kickstart();
                    handshaker.handshakeHash.reset();
                }
            }
        }
    }
    public boolean isClosed() {
        return getConnectionState() == cs_APP_CLOSED;
    }
    boolean checkEOF() throws IOException {
        switch (getConnectionState()) {
        case cs_START:
            throw new SocketException("Socket is not connected");
        case cs_HANDSHAKE:
        case cs_DATA:
        case cs_RENEGOTIATE:
        case cs_SENT_CLOSE:
            return false;
        case cs_APP_CLOSED:
            throw new SocketException("Socket is closed");
        case cs_ERROR:
        case cs_CLOSED:
        default:
            if (closeReason == null) {
                return true;
            }
            IOException e = new SSLException
                        ("Connection has been shutdown: " + closeReason);
            e.initCause(closeReason);
            throw e;
        }
    }
    void checkWrite() throws IOException {
        if (checkEOF() || (getConnectionState() == cs_SENT_CLOSE)) {
            throw new SocketException("Connection closed by remote host");
        }
    }
    protected void closeSocket() throws IOException {
        if ((debug != null) && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeSocket()");
        }
        if (self == this) {
            super.close();
        } else {
            self.close();
        }
    }
    private void closeSocket(boolean selfInitiated) throws IOException {
        if ((debug != null) && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeSocket(selfInitiated)");
        }
        if (self == this) {
            super.close();
        } else if (autoClose) {
            self.close();
        } else if (selfInitiated) {
            waitForClose(false);
        }
    }
    public void close() throws IOException {
        if ((debug != null) && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called close()");
        }
        closeInternal(true);  
        setConnectionState(cs_APP_CLOSED);
    }
    private void closeInternal(boolean selfInitiated) throws IOException {
        if ((debug != null) && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeInternal("
                + selfInitiated + ")");
        }
        int state = getConnectionState();
        boolean closeSocketCalled = false;
        Throwable cachedThrowable = null;
        try {
            switch (state) {
            case cs_START:
                break;
            case cs_ERROR:
                closeSocket();
                break;
            case cs_CLOSED:
            case cs_APP_CLOSED:
                 break;
            default:
                synchronized (this) {
                    if (((state = getConnectionState()) == cs_CLOSED) ||
                       (state == cs_ERROR) || (state == cs_APP_CLOSED)) {
                        return;  
                    }
                    if (state != cs_SENT_CLOSE) {
                        try {
                            warning(Alerts.alert_close_notify);
                            connectionState = cs_SENT_CLOSE;
                        } catch (Throwable th) {
                            connectionState = cs_ERROR;
                            cachedThrowable = th;
                            closeSocketCalled = true;
                            closeSocket(selfInitiated);
                        }
                    }
                }
                if (state == cs_SENT_CLOSE) {
                    if (debug != null && Debug.isOn("ssl")) {
                        System.out.println(threadName() +
                            ", close invoked again; state = " +
                            getConnectionState());
                    }
                    if (selfInitiated == false) {
                        return;
                    }
                    synchronized (this) {
                        while (connectionState < cs_CLOSED) {
                            try {
                                this.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    if ((debug != null) && Debug.isOn("ssl")) {
                        System.out.println(threadName() +
                            ", after primary close; state = " +
                            getConnectionState());
                    }
                    return;
                }
                if (!closeSocketCalled)  {
                    closeSocketCalled = true;
                    closeSocket(selfInitiated);
                }
                break;
            }
        } finally {
            synchronized (this) {
                connectionState = (connectionState == cs_APP_CLOSED)
                                ? cs_APP_CLOSED : cs_CLOSED;
                this.notifyAll();
            }
            if (closeSocketCalled) {
                disposeCiphers();
            }
            if (cachedThrowable != null) {
                if (cachedThrowable instanceof Error)
                    throw (Error) cachedThrowable;
                if (cachedThrowable instanceof RuntimeException)
                    throw (RuntimeException) cachedThrowable;
            }
        }
    }
    void waitForClose(boolean rethrow) throws IOException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() +
                ", waiting for close_notify or alert: state "
                + getConnectionState());
        }
        try {
            int state;
            while (((state = getConnectionState()) != cs_CLOSED) &&
                   (state != cs_ERROR) && (state != cs_APP_CLOSED)) {
                if (inrec == null) {
                    inrec = new InputRecord();
                }
                try {
                    readRecord(inrec, true);
                } catch (SocketTimeoutException e) {
                }
            }
            inrec = null;
        } catch (IOException e) {
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() +
                    ", Exception while waiting for close " +e);
            }
            if (rethrow) {
                throw e; 
            }
        }
    }
    private void disposeCiphers() {
        synchronized (readLock) {
            readCipher.dispose();
        }
        writeLock.lock();
        try {
            writeCipher.dispose();
        } finally {
            writeLock.unlock();
        }
    }
    void handleException(Exception e) throws IOException {
        handleException(e, true);
    }
    synchronized private void handleException(Exception e, boolean resumable)
        throws IOException {
        if ((debug != null) && Debug.isOn("ssl")) {
            System.out.println(threadName()
                        + ", handling exception: " + e.toString());
        }
        if (e instanceof InterruptedIOException && resumable) {
            throw (IOException)e;
        }
        if (closeReason != null) {
            if (e instanceof IOException) { 
                throw (IOException)e;
            } else {
                throw Alerts.getSSLException(Alerts.alert_internal_error, e,
                                      "Unexpected exception");
            }
        }
        boolean isSSLException = (e instanceof SSLException);
        if ((isSSLException == false) && (e instanceof IOException)) {
            try {
                fatal(Alerts.alert_unexpected_message, e);
            } catch (IOException ee) {
            }
            throw (IOException)e;
        }
        byte alertType;
        if (isSSLException) {
            if (e instanceof SSLHandshakeException) {
                alertType = Alerts.alert_handshake_failure;
            } else {
                alertType = Alerts.alert_unexpected_message;
            }
        } else {
            alertType = Alerts.alert_internal_error;
        }
        fatal(alertType, e);
    }
    void warning(byte description) {
        sendAlert(Alerts.alert_warning, description);
    }
    synchronized void fatal(byte description, String diagnostic)
            throws IOException {
        fatal(description, diagnostic, null);
    }
    synchronized void fatal(byte description, Throwable cause)
            throws IOException {
        fatal(description, null, cause);
    }
    synchronized void fatal(byte description, String diagnostic,
            Throwable cause) throws IOException {
        if ((input != null) && (input.r != null)) {
            input.r.close();
        }
        sess.invalidate();
        if (handshakeSession != null) {
            handshakeSession.invalidate();
        }
        int oldState = connectionState;
        if (connectionState < cs_ERROR) {
            connectionState = cs_ERROR;
        }
        if (closeReason == null) {
            if (oldState == cs_HANDSHAKE) {
                sockInput.skip(sockInput.available());
            }
            if (description != -1) {
                sendAlert(Alerts.alert_fatal, description);
            }
            if (cause instanceof SSLException) { 
                closeReason = (SSLException)cause;
            } else {
                closeReason =
                    Alerts.getSSLException(description, cause, diagnostic);
            }
        }
        closeSocket();
        if (connectionState < cs_CLOSED) {
            connectionState = (oldState == cs_APP_CLOSED) ? cs_APP_CLOSED
                                                              : cs_CLOSED;
            readCipher.dispose();
            writeCipher.dispose();
        }
        throw closeReason;
    }
    private void recvAlert(InputRecord r) throws IOException {
        byte level = (byte)r.read();
        byte description = (byte)r.read();
        if (description == -1) { 
            fatal(Alerts.alert_illegal_parameter, "Short alert message");
        }
        if (debug != null && (Debug.isOn("record") ||
                Debug.isOn("handshake"))) {
            synchronized (System.out) {
                System.out.print(threadName());
                System.out.print(", RECV " + protocolVersion + " ALERT:  ");
                if (level == Alerts.alert_fatal) {
                    System.out.print("fatal, ");
                } else if (level == Alerts.alert_warning) {
                    System.out.print("warning, ");
                } else {
                    System.out.print("<level " + (0x0ff & level) + ">, ");
                }
                System.out.println(Alerts.alertDescription(description));
            }
        }
        if (level == Alerts.alert_warning) {
            if (description == Alerts.alert_close_notify) {
                if (connectionState == cs_HANDSHAKE) {
                    fatal(Alerts.alert_unexpected_message,
                                "Received close_notify during handshake");
                } else {
                    closeInternal(false);  
                }
            } else {
                if (handshaker != null) {
                    handshaker.handshakeAlert(description);
                }
            }
        } else { 
            String reason = "Received fatal alert: "
                + Alerts.alertDescription(description);
            if (closeReason == null) {
                closeReason = Alerts.getSSLException(description, reason);
            }
            fatal(Alerts.alert_unexpected_message, reason);
        }
    }
    private void sendAlert(byte level, byte description) {
        if (connectionState >= cs_SENT_CLOSE) {
            return;
        }
        if (connectionState == cs_HANDSHAKE &&
            (handshaker == null || !handshaker.started())) {
            return;
        }
        OutputRecord r = new OutputRecord(Record.ct_alert);
        r.setVersion(protocolVersion);
        boolean useDebug = debug != null && Debug.isOn("ssl");
        if (useDebug) {
            synchronized (System.out) {
                System.out.print(threadName());
                System.out.print(", SEND " + protocolVersion + " ALERT:  ");
                if (level == Alerts.alert_fatal) {
                    System.out.print("fatal, ");
                } else if (level == Alerts.alert_warning) {
                    System.out.print("warning, ");
                } else {
                    System.out.print("<level = " + (0x0ff & level) + ">, ");
                }
                System.out.println("description = "
                        + Alerts.alertDescription(description));
            }
        }
        r.write(level);
        r.write(description);
        try {
            writeRecord(r);
        } catch (IOException e) {
            if (useDebug) {
                System.out.println(threadName() +
                    ", Exception sending alert: " + e);
            }
        }
    }
    private void changeReadCiphers() throws SSLException {
        if (connectionState != cs_HANDSHAKE
                && connectionState != cs_RENEGOTIATE) {
            throw new SSLProtocolException(
                "State error, change cipher specs");
        }
        CipherBox oldCipher = readCipher;
        try {
            readCipher = handshaker.newReadCipher();
            readMAC = handshaker.newReadMAC();
        } catch (GeneralSecurityException e) {
            throw (SSLException)new SSLException
                                ("Algorithm missing:  ").initCause(e);
        }
        oldCipher.dispose();
    }
    void changeWriteCiphers() throws SSLException {
        if (connectionState != cs_HANDSHAKE
                && connectionState != cs_RENEGOTIATE) {
            throw new SSLProtocolException(
                "State error, change cipher specs");
        }
        CipherBox oldCipher = writeCipher;
        try {
            writeCipher = handshaker.newWriteCipher();
            writeMAC = handshaker.newWriteMAC();
        } catch (GeneralSecurityException e) {
            throw (SSLException)new SSLException
                                ("Algorithm missing:  ").initCause(e);
        }
        oldCipher.dispose();
    }
    synchronized void setVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        output.r.setVersion(protocolVersion);
    }
    synchronized String getHost() {
        if (host == null || host.length() == 0) {
            host = getInetAddress().getHostName();
        }
        return host;
    }
    synchronized String getRawHostname() {
        return rawHostname;
    }
    synchronized public void setHost(String host) {
        this.host = host;
        this.rawHostname = host;
    }
    synchronized public InputStream getInputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (connectionState == cs_START) {
            throw new SocketException("Socket is not connected");
        }
        return input;
    }
    synchronized public OutputStream getOutputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (connectionState == cs_START) {
            throw new SocketException("Socket is not connected");
        }
        return output;
    }
    public SSLSession getSession() {
        if (getConnectionState() == cs_HANDSHAKE) {
            try {
                startHandshake(false);
            } catch (IOException e) {
                if (debug != null && Debug.isOn("handshake")) {
                      System.out.println(threadName() +
                          ", IOException in getSession():  " + e);
                }
            }
        }
        synchronized (this) {
            return sess;
        }
    }
    @Override
    synchronized public SSLSession getHandshakeSession() {
        return handshakeSession;
    }
    synchronized void setHandshakeSession(SSLSessionImpl session) {
        handshakeSession = session;
    }
    synchronized public void setEnableSessionCreation(boolean flag) {
        enableSessionCreation = flag;
        if ((handshaker != null) && !handshaker.activated()) {
            handshaker.setEnableSessionCreation(enableSessionCreation);
        }
    }
    synchronized public boolean getEnableSessionCreation() {
        return enableSessionCreation;
    }
    synchronized public void setNeedClientAuth(boolean flag) {
        doClientAuth = (flag ?
            SSLEngineImpl.clauth_required : SSLEngineImpl.clauth_none);
        if ((handshaker != null) &&
                (handshaker instanceof ServerHandshaker) &&
                !handshaker.activated()) {
            ((ServerHandshaker) handshaker).setClientAuth(doClientAuth);
        }
    }
    synchronized public boolean getNeedClientAuth() {
        return (doClientAuth == SSLEngineImpl.clauth_required);
    }
    synchronized public void setWantClientAuth(boolean flag) {
        doClientAuth = (flag ?
            SSLEngineImpl.clauth_requested : SSLEngineImpl.clauth_none);
        if ((handshaker != null) &&
                (handshaker instanceof ServerHandshaker) &&
                !handshaker.activated()) {
            ((ServerHandshaker) handshaker).setClientAuth(doClientAuth);
        }
    }
    synchronized public boolean getWantClientAuth() {
        return (doClientAuth == SSLEngineImpl.clauth_requested);
    }
    synchronized public void setUseClientMode(boolean flag) {
        switch (connectionState) {
        case cs_START:
            if (roleIsServer != (!flag) &&
                    sslContext.isDefaultProtocolList(enabledProtocols)) {
                enabledProtocols = sslContext.getDefaultProtocolList(!flag);
            }
            roleIsServer = !flag;
            break;
        case cs_HANDSHAKE:
            assert(handshaker != null);
            if (!handshaker.activated()) {
                if (roleIsServer != (!flag) &&
                        sslContext.isDefaultProtocolList(enabledProtocols)) {
                    enabledProtocols = sslContext.getDefaultProtocolList(!flag);
                }
                roleIsServer = !flag;
                connectionState = cs_START;
                initHandshaker();
                break;
            }
        default:
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() +
                    ", setUseClientMode() invoked in state = " +
                    connectionState);
            }
            throw new IllegalArgumentException(
                "Cannot change mode after SSL traffic has started");
        }
    }
    synchronized public boolean getUseClientMode() {
        return !roleIsServer;
    }
    public String[] getSupportedCipherSuites() {
        return sslContext.getSuportedCipherSuiteList().toStringArray();
    }
    synchronized public void setEnabledCipherSuites(String[] suites) {
        enabledCipherSuites = new CipherSuiteList(suites);
        if ((handshaker != null) && !handshaker.activated()) {
            handshaker.setEnabledCipherSuites(enabledCipherSuites);
        }
    }
    synchronized public String[] getEnabledCipherSuites() {
        return enabledCipherSuites.toStringArray();
    }
    public String[] getSupportedProtocols() {
        return sslContext.getSuportedProtocolList().toStringArray();
    }
    synchronized public void setEnabledProtocols(String[] protocols) {
        enabledProtocols = new ProtocolList(protocols);
        if ((handshaker != null) && !handshaker.activated()) {
            handshaker.setEnabledProtocols(enabledProtocols);
        }
    }
    synchronized public String[] getEnabledProtocols() {
        return enabledProtocols.toStringArray();
    }
    public void setSoTimeout(int timeout) throws SocketException {
        if ((debug != null) && Debug.isOn("ssl")) {
            System.out.println(threadName() +
                ", setSoTimeout(" + timeout + ") called");
        }
        if (self == this) {
            super.setSoTimeout(timeout);
        } else {
            self.setSoTimeout(timeout);
        }
    }
    public synchronized void addHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        if (handshakeListeners == null) {
            handshakeListeners = new
                HashMap<HandshakeCompletedListener, AccessControlContext>(4);
        }
        handshakeListeners.put(listener, AccessController.getContext());
    }
    public synchronized void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        if (handshakeListeners == null) {
            throw new IllegalArgumentException("no listeners");
        }
        if (handshakeListeners.remove(listener) == null) {
            throw new IllegalArgumentException("listener not registered");
        }
        if (handshakeListeners.isEmpty()) {
            handshakeListeners = null;
        }
    }
    synchronized public SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();
        params.setEndpointIdentificationAlgorithm(identificationProtocol);
        params.setAlgorithmConstraints(algorithmConstraints);
        return params;
    }
    synchronized public void setSSLParameters(SSLParameters params) {
        super.setSSLParameters(params);
        identificationProtocol = params.getEndpointIdentificationAlgorithm();
        algorithmConstraints = params.getAlgorithmConstraints();
        if ((handshaker != null) && !handshaker.started()) {
            handshaker.setIdentificationProtocol(identificationProtocol);
            handshaker.setAlgorithmConstraints(algorithmConstraints);
        }
    }
    private static class NotifyHandshakeThread extends Thread {
        private Set<Map.Entry<HandshakeCompletedListener,AccessControlContext>>
                targets;        
        private HandshakeCompletedEvent event;          
        NotifyHandshakeThread(
            Set<Map.Entry<HandshakeCompletedListener,AccessControlContext>>
            entrySet, HandshakeCompletedEvent e) {
            super("HandshakeCompletedNotify-Thread");
            targets = entrySet;
            event = e;
        }
        public void run() {
            for (Map.Entry<HandshakeCompletedListener,AccessControlContext>
                entry : targets) {
                final HandshakeCompletedListener l = entry.getKey();
                AccessControlContext acc = entry.getValue();
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        l.handshakeCompleted(event);
                        return null;
                    }
                }, acc);
            }
        }
    }
    private static String threadName() {
        return Thread.currentThread().getName();
    }
    public String toString() {
        StringBuffer retval = new StringBuffer(80);
        retval.append(Integer.toHexString(hashCode()));
        retval.append("[");
        retval.append(sess.getCipherSuite());
        retval.append(": ");
        if (self == this) {
            retval.append(super.toString());
        } else {
            retval.append(self.toString());
        }
        retval.append("]");
        return retval.toString();
    }
}
