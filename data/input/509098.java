public class SSLEngineImpl extends SSLEngine {
    private boolean peer_mode_was_set = false;
    private boolean handshake_started = false;
    private boolean isInboundDone = false;
    private boolean isOutboundDone = false;
    private boolean close_notify_was_sent = false;
    private boolean close_notify_was_received = false;
    private boolean engine_was_closed = false;
    private boolean engine_was_shutteddown = false;
    protected SSLRecordProtocol recordProtocol;
    private SSLBufferedInput recProtIS;
    private HandshakeProtocol handshakeProtocol;
    private AlertProtocol alertProtocol;
    private SSLEngineAppData appData;
    private SSLEngineDataStream dataStream = new SSLEngineDataStream();
    private SSLSessionImpl session;
    protected SSLParameters sslParameters;
    private byte[] remaining_wrapped_data = null;
    private byte[] remaining_hsh_data = null;
    private Logger.Stream logger = Logger.getStream("engine");
    protected SSLEngineImpl(SSLParameters sslParameters) {
        super();
        this.sslParameters = sslParameters;
    }
    protected SSLEngineImpl(String host, int port, SSLParameters sslParameters) {
        super(host, port);
        this.sslParameters = sslParameters;
    }
    @Override
    public void beginHandshake() throws SSLException {
        if (engine_was_closed) {
            throw new SSLException("Engine has already been closed.");
        }
        if (!peer_mode_was_set) {
            throw new IllegalStateException("Client/Server mode was not set");
        }
        if (!handshake_started) {
            handshake_started = true;
            if (getUseClientMode()) {
                handshakeProtocol = new ClientHandshakeImpl(this);
            } else {
                handshakeProtocol = new ServerHandshakeImpl(this);
            }
            appData = new SSLEngineAppData();
            alertProtocol = new AlertProtocol();
            recProtIS = new SSLBufferedInput();
            recordProtocol = new SSLRecordProtocol(handshakeProtocol,
                    alertProtocol, recProtIS, appData);
        }
        handshakeProtocol.start();
    }
    @Override
    public void closeInbound() throws SSLException {
        if (logger != null) {
            logger.println("closeInbound() "+isInboundDone);
        }
        if (isInboundDone) {
            return;
        }
        isInboundDone = true;
        engine_was_closed = true;
        if (handshake_started) {
            if (!close_notify_was_received) {
                if (session != null) {
                    session.invalidate();
                }
                alertProtocol.alert(AlertProtocol.FATAL,
                        AlertProtocol.INTERNAL_ERROR);
                throw new SSLException("Inbound is closed before close_notify "
                        + "alert has been received.");
            }
        } else {
            shutdown();
        }
    }
    @Override
    public void closeOutbound() {
        if (logger != null) {
            logger.println("closeOutbound() "+isOutboundDone);
        }
        if (isOutboundDone) {
            return;
        }
        isOutboundDone = true;
        if (handshake_started) {
            alertProtocol.alert(AlertProtocol.WARNING,
                    AlertProtocol.CLOSE_NOTIFY);
            close_notify_was_sent = true;
        } else {
            shutdown();
        }
        engine_was_closed = true;
    }
    @Override
    public Runnable getDelegatedTask() {
        return handshakeProtocol.getTask();
    }
    @Override
    public String[] getSupportedCipherSuites() {
        return CipherSuite.getSupportedCipherSuiteNames();
    }
    @Override
    public String[] getEnabledCipherSuites() {
        return sslParameters.getEnabledCipherSuites();
    }
    @Override
    public void setEnabledCipherSuites(String[] suites) {
        sslParameters.setEnabledCipherSuites(suites);
    }
    @Override
    public String[] getSupportedProtocols() {
        return ProtocolVersion.supportedProtocols.clone();
    }
    @Override
    public String[] getEnabledProtocols() {
        return sslParameters.getEnabledProtocols();
    }
    @Override
    public void setEnabledProtocols(String[] protocols) {
        sslParameters.setEnabledProtocols(protocols);
    }
    @Override
    public void setUseClientMode(boolean mode) {
        if (handshake_started) {
            throw new IllegalArgumentException(
            "Could not change the mode after the initial handshake has begun.");
        }
        sslParameters.setUseClientMode(mode);
        peer_mode_was_set = true;
    }
    @Override
    public boolean getUseClientMode() {
        return sslParameters.getUseClientMode();
    }
    @Override
    public void setNeedClientAuth(boolean need) {
        sslParameters.setNeedClientAuth(need);
    }
    @Override
    public boolean getNeedClientAuth() {
        return sslParameters.getNeedClientAuth();
    }
    @Override
    public void setWantClientAuth(boolean want) {
        sslParameters.setWantClientAuth(want);
    }
    @Override
    public boolean getWantClientAuth() {
        return sslParameters.getWantClientAuth();
    }
    @Override
    public void setEnableSessionCreation(boolean flag) {
        sslParameters.setEnableSessionCreation(flag);
    }
    @Override
    public boolean getEnableSessionCreation() {
        return sslParameters.getEnableSessionCreation();
    }
    @Override
    public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        if (!handshake_started || engine_was_shutteddown) {
            return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }
        if (alertProtocol.hasAlert()) {
            return SSLEngineResult.HandshakeStatus.NEED_WRAP;
        }
        if (close_notify_was_sent && !close_notify_was_received) {
            return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
        }
        return handshakeProtocol.getStatus();
    }
    @Override
    public SSLSession getSession() {
        if (session != null) {
            return session;
        }
        return SSLSessionImpl.NULL_SESSION;
    }
    @Override
    public boolean isInboundDone() {
        return isInboundDone || engine_was_closed;
    }
    @Override
    public boolean isOutboundDone() {
        return isOutboundDone;
    }
    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts,
                                int offset, int length) throws SSLException {
        if (engine_was_shutteddown) {
            return new SSLEngineResult(SSLEngineResult.Status.CLOSED,
                    SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
        }
        if ((src == null) || (dsts == null)) {
            throw new IllegalStateException(
                    "Some of the input parameters are null");
        }
        if (!handshake_started) {
            beginHandshake();
        }
        SSLEngineResult.HandshakeStatus handshakeStatus = getHandshakeStatus();
        if ((session == null || engine_was_closed) && (
                    handshakeStatus.equals(
                        SSLEngineResult.HandshakeStatus.NEED_WRAP) ||
                    handshakeStatus.equals(
                        SSLEngineResult.HandshakeStatus.NEED_TASK))) {
            return new SSLEngineResult(
                    getEngineStatus(), handshakeStatus, 0, 0);
        }
        if (src.remaining() < recordProtocol.getMinRecordSize()) {
            return new SSLEngineResult(
                    SSLEngineResult.Status.BUFFER_UNDERFLOW,
                    getHandshakeStatus(), 0, 0);
        }
        try {
            src.mark();
            int capacity = 0;
            for (int i=offset; i<offset+length; i++) {
                if (dsts[i] == null) {
                    throw new IllegalStateException(
                            "Some of the input parameters are null");
                }
                if (dsts[i].isReadOnly()) {
                    throw new ReadOnlyBufferException();
                }
                capacity += dsts[i].remaining();
            }
            if (capacity < recordProtocol.getDataSize(src.remaining())) {
                return new SSLEngineResult(
                        SSLEngineResult.Status.BUFFER_OVERFLOW,
                        getHandshakeStatus(), 0, 0);
            }
            recProtIS.setSourceBuffer(src);
            int type = recordProtocol.unwrap();
            switch (type) {
                case ContentType.HANDSHAKE:
                case ContentType.CHANGE_CIPHER_SPEC:
                    if (handshakeProtocol.getStatus().equals(
                            SSLEngineResult.HandshakeStatus.FINISHED)) {
                        session = recordProtocol.getSession();
                    }
                    break;
                case ContentType.APPLICATION_DATA:
                    break;
                case ContentType.ALERT:
                    if (alertProtocol.isFatalAlert()) {
                        alertProtocol.setProcessed();
                        if (session != null) {
                            session.invalidate();
                        }
                        String description = "Fatal alert received "
                            + alertProtocol.getAlertDescription();
                        shutdown();
                        throw new SSLException(description);
                    } else {
                        if (logger != null) {
                            logger.println("Warning allert has been received: "
                                + alertProtocol.getAlertDescription());
                        }
                        switch(alertProtocol.getDescriptionCode()) {
                            case AlertProtocol.CLOSE_NOTIFY:
                                alertProtocol.setProcessed();
                                close_notify_was_received = true;
                                if (!close_notify_was_sent) {
                                    closeOutbound();
                                    closeInbound();
                                } else {
                                    closeInbound();
                                    shutdown();
                                }
                                break;
                            case AlertProtocol.NO_RENEGOTIATION:
                                alertProtocol.setProcessed();
                                if (session == null) {
                                    throw new AlertException(
                                        AlertProtocol.HANDSHAKE_FAILURE,
                                        new SSLHandshakeException(
                                            "Received no_renegotiation "
                                            + "during the initial handshake"));
                                } else {
                                    handshakeProtocol.stop();
                                }
                                break;
                            default:
                                alertProtocol.setProcessed();
                        }
                    }
                    break;
            }
            return new SSLEngineResult(getEngineStatus(), getHandshakeStatus(),
                    recProtIS.consumed(), 
                    appData.placeTo(dsts, offset, length));
        } catch (BufferUnderflowException e) {
            src.reset();
            return new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW,
                    getHandshakeStatus(), 0, 0);
        } catch (AlertException e) {
            alertProtocol.alert(AlertProtocol.FATAL, e.getDescriptionCode());
            engine_was_closed = true;
            src.reset();
            if (session != null) {
                session.invalidate();
            }
            throw e.getReason();
        } catch (SSLException e) {
            throw e;
        } catch (IOException e) {
            alertProtocol.alert(AlertProtocol.FATAL,
                    AlertProtocol.INTERNAL_ERROR);
            engine_was_closed = true;
            throw new SSLException(e.getMessage());
        }
    }
    @Override
    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset,
                            int len, ByteBuffer dst) throws SSLException {
        if (engine_was_shutteddown) {
            return new SSLEngineResult(SSLEngineResult.Status.CLOSED,
                    SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
        }
        if ((srcs == null) || (dst == null)) {
            throw new IllegalStateException(
                    "Some of the input parameters are null");
        }
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (!handshake_started) {
            beginHandshake();
        }
        SSLEngineResult.HandshakeStatus handshakeStatus = getHandshakeStatus();
        if ((session == null || engine_was_closed) && (
                handshakeStatus.equals(
                        SSLEngineResult.HandshakeStatus.NEED_UNWRAP) ||
                handshakeStatus.equals(
                        SSLEngineResult.HandshakeStatus.NEED_TASK))) {
            return new SSLEngineResult(
                    getEngineStatus(), handshakeStatus, 0, 0);
        }
        int capacity = dst.remaining();
        int produced = 0;
        if (alertProtocol.hasAlert()) {
            if (capacity < recordProtocol.getRecordSize(2)) {
                return new SSLEngineResult(
                        SSLEngineResult.Status.BUFFER_OVERFLOW,
                        handshakeStatus, 0, 0);
            }
            byte[] alert_data = alertProtocol.wrap();
            dst.put(alert_data);
            if (alertProtocol.isFatalAlert()) {
                alertProtocol.setProcessed();
                if (session != null) {
                    session.invalidate();
                }
                shutdown();
                return new SSLEngineResult(
                        SSLEngineResult.Status.CLOSED,
                        SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                        0, alert_data.length);
            } else {
                alertProtocol.setProcessed();
                if (close_notify_was_sent && close_notify_was_received) {
                    shutdown();
                    return new SSLEngineResult(SSLEngineResult.Status.CLOSED,
                            SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                            0, alert_data.length);
                }
                return new SSLEngineResult(
                        getEngineStatus(),
                        getHandshakeStatus(),
                        0, alert_data.length);
            }
        }
        if (capacity < recordProtocol.getMinRecordSize()) {
            if (logger != null) {
                logger.println("Capacity of the destination("
                        +capacity+") < MIN_PACKET_SIZE("
                        +recordProtocol.getMinRecordSize()+")");
            }
            return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW,
                        handshakeStatus, 0, 0);
        }
        try {
            if (!handshakeStatus.equals(
                        SSLEngineResult.HandshakeStatus.NEED_WRAP)) {
                dataStream.setSourceBuffers(srcs, offset, len);
                if ((capacity < SSLRecordProtocol.MAX_SSL_PACKET_SIZE) &&
                    (capacity < recordProtocol.getRecordSize(
                                                 dataStream.available()))) {
                    if (logger != null) {
                        logger.println("The destination buffer("
                                +capacity+") can not take the resulting packet("
                                + recordProtocol.getRecordSize(
                                    dataStream.available())+")");
                    }
                    return new SSLEngineResult(
                            SSLEngineResult.Status.BUFFER_OVERFLOW,
                            handshakeStatus, 0, 0);
                }
                if (remaining_wrapped_data == null) {
                    remaining_wrapped_data =
                        recordProtocol.wrap(ContentType.APPLICATION_DATA,
                                dataStream);
                }
                if (capacity < remaining_wrapped_data.length) {
                    return new SSLEngineResult(
                            SSLEngineResult.Status.BUFFER_OVERFLOW,
                            handshakeStatus, dataStream.consumed(), 0);
                } else {
                    dst.put(remaining_wrapped_data);
                    produced = remaining_wrapped_data.length;
                    remaining_wrapped_data = null;
                    return new SSLEngineResult(getEngineStatus(), 
                            handshakeStatus, dataStream.consumed(), produced);
                }
            } else {
                if (remaining_hsh_data == null) {
                    remaining_hsh_data = handshakeProtocol.wrap();
                }
                if (capacity < remaining_hsh_data.length) {
                    return new SSLEngineResult(
                            SSLEngineResult.Status.BUFFER_OVERFLOW,
                            handshakeStatus, 0, 0);
                } else {
                    dst.put(remaining_hsh_data);
                    produced = remaining_hsh_data.length;
                    remaining_hsh_data = null;
                    handshakeStatus = handshakeProtocol.getStatus();
                    if (handshakeStatus.equals(
                            SSLEngineResult.HandshakeStatus.FINISHED)) {
                        session = recordProtocol.getSession();
                    }
                }
                return new SSLEngineResult(
                        getEngineStatus(), getHandshakeStatus(), 0, produced);
            }
        } catch (AlertException e) {
            alertProtocol.alert(AlertProtocol.FATAL, e.getDescriptionCode());
            engine_was_closed = true;
            if (session != null) {
                session.invalidate();
            }
            throw e.getReason();
        }
    }
    private void shutdown() {
        engine_was_closed = true;
        engine_was_shutteddown = true;
        isOutboundDone = true;
        isInboundDone = true;
        if (handshake_started) {
            alertProtocol.shutdown();
            alertProtocol = null;
            handshakeProtocol.shutdown();
            handshakeProtocol = null;
            recordProtocol.shutdown();
            recordProtocol = null;
        }
    }
    private SSLEngineResult.Status getEngineStatus() {
        return (engine_was_closed)
            ? SSLEngineResult.Status.CLOSED
            : SSLEngineResult.Status.OK;
    }
}
