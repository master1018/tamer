public class SSLRecordProtocol {
    protected static int MAX_DATA_LENGTH = 16384; 
    protected static int MAX_COMPRESSED_DATA_LENGTH
                                    = MAX_DATA_LENGTH + 1024;
    protected static int MAX_CIPHERED_DATA_LENGTH
                                    = MAX_COMPRESSED_DATA_LENGTH + 1024;
    protected static int MAX_SSL_PACKET_SIZE
                                    = MAX_CIPHERED_DATA_LENGTH + 5;
    private SSLSessionImpl session;
    private byte[] version;
    private SSLInputStream in;
    private HandshakeProtocol handshakeProtocol;
    private AlertProtocol alertProtocol;
    private org.apache.harmony.xnet.provider.jsse.Appendable appData;
    private ConnectionState 
        activeReadState, activeWriteState, pendingConnectionState;
    private Logger.Stream logger = Logger.getStream("record");
    private boolean sessionWasChanged = false;
    private static final byte[] change_cipher_spec_byte = new byte[] {1};
    protected SSLRecordProtocol(HandshakeProtocol handshakeProtocol,
            AlertProtocol alertProtocol,
            SSLInputStream in,
            Appendable appData) {
        this.handshakeProtocol = handshakeProtocol;
        this.handshakeProtocol.setRecordProtocol(this);
        this.alertProtocol = alertProtocol;
        this.alertProtocol.setRecordProtocol(this);
        this.in = in;
        this.appData = appData;
    }
    protected SSLSessionImpl getSession() {
        return session;
    }
    protected int getMinRecordSize() {
        return (activeReadState == null)
            ? 6 
            : 5 + activeReadState.getMinFragmentSize();
    }
    protected int getRecordSize(int data_size) {
        if (activeWriteState == null) {
            return 5+data_size; 
        } else {
            int res = 5 + activeWriteState.getFragmentSize(data_size);
            return (res > MAX_CIPHERED_DATA_LENGTH)
                ? MAX_CIPHERED_DATA_LENGTH 
                : res;
        }
    }
    protected int getDataSize(int record_size) {
        record_size -= 5; 
        if (record_size > MAX_CIPHERED_DATA_LENGTH) {
            return MAX_DATA_LENGTH;
        }
        if (activeReadState == null) {
            return record_size;
        }
        return activeReadState.getContentSize(record_size);
    }
    protected byte[] wrap(byte content_type, DataStream dataStream) {
        byte[] fragment = dataStream.getData(MAX_DATA_LENGTH);
        return wrap(content_type, fragment, 0, fragment.length);
    }
    protected byte[] wrap(byte content_type,
                       byte[] fragment, int offset, int len) {
        if (logger != null) {
            logger.println("SSLRecordProtocol.wrap: TLSPlaintext.fragment["
                    +len+"]:");
            logger.print(fragment, offset, len);
        }
        if (len > MAX_DATA_LENGTH) {
            throw new AlertException(
                AlertProtocol.INTERNAL_ERROR,
                new SSLProtocolException(
                    "The provided chunk of data is too big: " + len
                    + " > MAX_DATA_LENGTH == "+MAX_DATA_LENGTH));
        }
        byte[] ciphered_fragment = fragment;
        if (activeWriteState != null) {
            ciphered_fragment =
                activeWriteState.encrypt(content_type, fragment, offset, len);
            if (ciphered_fragment.length > MAX_CIPHERED_DATA_LENGTH) {
                throw new AlertException(
                    AlertProtocol.INTERNAL_ERROR,
                    new SSLProtocolException(
                        "The ciphered data increased more than on 1024 bytes"));
            }
            if (logger != null) {
                logger.println("SSLRecordProtocol.wrap: TLSCiphertext.fragment["
                        +ciphered_fragment.length+"]:");
                logger.print(ciphered_fragment);
            }
        }
        return packetize(content_type, version, ciphered_fragment);
    }
    private byte[] packetize(byte type, byte[] version, byte[] fragment) {
        byte[] buff = new byte[5+fragment.length];
        buff[0] = type;
        if (version != null) {
            buff[1] = version[0];
            buff[2] = version[1];
        } else {
            buff[1] = 3;
            buff[2] = 1;
        }
        buff[3] = (byte) ((0x00FF00 & fragment.length) >> 8);
        buff[4] = (byte) (0x0000FF & fragment.length);
        System.arraycopy(fragment, 0, buff, 5, fragment.length);
        return buff;
    }
    private void setSession(SSLSessionImpl session) {
        if (!sessionWasChanged) {
            if (logger != null) {
                logger.println("SSLRecordProtocol.setSession: Set pending session");
                logger.println("  cipher name: " + session.getCipherSuite());
            }
            this.session = session;
            pendingConnectionState = ((version == null) || (version[1] == 1))
                ? (ConnectionState) new ConnectionStateTLS(getSession())
                : (ConnectionState) new ConnectionStateSSLv3(getSession());
            sessionWasChanged = true;
        } else {
            sessionWasChanged = false;
        }
    }
    protected byte[] getChangeCipherSpecMesage(SSLSessionImpl session) {
        byte[] change_cipher_spec_message;
        if (activeWriteState == null) {
            change_cipher_spec_message = new byte[] {
                    ContentType.CHANGE_CIPHER_SPEC, version[0],
                        version[1], 0, 1, 1
                };
        } else {
            change_cipher_spec_message =
                packetize(ContentType.CHANGE_CIPHER_SPEC, version,
                        activeWriteState.encrypt(ContentType.CHANGE_CIPHER_SPEC,
                            change_cipher_spec_byte, 0, 1));
        }
        setSession(session);
        activeWriteState = pendingConnectionState;
        if (logger != null) {
            logger.println("SSLRecordProtocol.getChangeCipherSpecMesage");
            logger.println("activeWriteState = pendingConnectionState");
            logger.print(change_cipher_spec_message);
        }
        return change_cipher_spec_message;
    }
    protected int unwrap() throws IOException {
        if (logger != null) {
            logger.println("SSLRecordProtocol.unwrap: BEGIN [");
        }
        int type = in.readUint8();
        if ((type < ContentType.CHANGE_CIPHER_SPEC)
                || (type > ContentType.APPLICATION_DATA)) {
            if (logger != null) {
                logger.println("Non v3.1 message type:" + type);
            }
            if (type >= 0x80) {
                int length = (type & 0x7f) << 8 | in.read();
                byte[] fragment = in.read(length);
                handshakeProtocol.unwrapSSLv2(fragment);
                if (logger != null) {
                    logger.println(
                            "SSLRecordProtocol:unwrap ] END, SSLv2 type");
                }
                return ContentType.HANDSHAKE;
            }
            throw new AlertException(AlertProtocol.UNEXPECTED_MESSAGE,
                    new SSLProtocolException(
                        "Unexpected message type has been received: "+type));
        }
        if (logger != null) {
            logger.println("Got the message of type: " + type);
        }
        if (version != null) {
            if ((in.read() != version[0])
                    || (in.read() != version[1])) {
                throw new AlertException(AlertProtocol.UNEXPECTED_MESSAGE,
                        new SSLProtocolException(
                            "Unexpected message type has been received: " +
                            type));
            }
        } else {
            in.skip(2); 
        }
        int length = in.readUint16();
        if (logger != null) {
            logger.println("TLSCiphertext.fragment["+length+"]: ...");
        }
        if (length > MAX_CIPHERED_DATA_LENGTH) {
            throw new AlertException(AlertProtocol.RECORD_OVERFLOW,
                    new SSLProtocolException(
                        "Received message is too big."));
        }
        byte[] fragment = in.read(length);
        if (logger != null) {
            logger.print(fragment);
        }
        if (activeReadState != null) {
            fragment = activeReadState.decrypt((byte) type, fragment);
            if (logger != null) {
                logger.println("TLSPlaintext.fragment:");
                logger.print(fragment);
            }
        }
        if (fragment.length > MAX_DATA_LENGTH) {
            throw new AlertException(AlertProtocol.DECOMPRESSION_FAILURE,
                    new SSLProtocolException(
                        "Decompressed plain data is too big."));
        }
        switch (type) {
            case ContentType.CHANGE_CIPHER_SPEC:
                handshakeProtocol.receiveChangeCipherSpec();
                setSession(handshakeProtocol.getSession());
                if (logger != null) {
                    logger.println("activeReadState = pendingConnectionState");
                }
                activeReadState = pendingConnectionState;
                break;
            case ContentType.ALERT:
                alert(fragment[0], fragment[1]);
                break;
            case ContentType.HANDSHAKE:
                handshakeProtocol.unwrap(fragment);
                break;
            case ContentType.APPLICATION_DATA:
                if (logger != null) {
                    logger.println(
                            "TLSCiphertext.unwrap: APP DATA["+length+"]:");
                    logger.println(new String(fragment));
                }
                appData.append(fragment);
                break;
            default:
                throw new AlertException(AlertProtocol.UNEXPECTED_MESSAGE,
                        new SSLProtocolException(
                            "Unexpected message type has been received: " +
                            type));
        }
        if (logger != null) {
            logger.println("SSLRecordProtocol:unwrap ] END, type: " + type);
        }
        return type;
    }
    protected void alert(byte level, byte description) {
        if (logger != null) {
            logger.println("SSLRecordProtocol.allert: "+level+" "+description);
        }
        alertProtocol.alert(level, description);
    }
    protected void setVersion(byte[] ver) {
        this.version = ver;
    }
    protected void shutdown() {
        session = null;
        version = null;
        in = null;
        handshakeProtocol = null;
        alertProtocol = null;
        appData = null;
        if (pendingConnectionState != null) {
            pendingConnectionState.shutdown();
        }
        pendingConnectionState = null;
        if (activeReadState != null) {
            activeReadState.shutdown();
        }
        activeReadState = null;
        if (activeReadState != null) {
            activeReadState.shutdown();
        }
        activeWriteState = null;
    }
}
