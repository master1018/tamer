public final class ClientSession extends ObexSession {
    private boolean mOpen;
    private boolean mObexConnected;
    private byte[] mConnectionId = null;
    private int maxPacketSize = 256;
    private boolean mRequestActive;
    private final InputStream mInput;
    private final OutputStream mOutput;
    public ClientSession(final ObexTransport trans) throws IOException {
        mInput = trans.openInputStream();
        mOutput = trans.openOutputStream();
        mOpen = true;
        mRequestActive = false;
    }
    public HeaderSet connect(final HeaderSet header) throws IOException {
        ensureOpen();
        if (mObexConnected) {
            throw new IOException("Already connected to server");
        }
        setRequestActive();
        int totalLength = 4;
        byte[] head = null;
        if (header != null) {
            if (header.nonce != null) {
                mChallengeDigest = new byte[16];
                System.arraycopy(header.nonce, 0, mChallengeDigest, 0, 16);
            }
            head = ObexHelper.createHeader(header, false);
            totalLength += head.length;
        }
        byte[] requestPacket = new byte[totalLength];
        requestPacket[0] = (byte)0x10;
        requestPacket[1] = (byte)0x00;
        requestPacket[2] = (byte)(ObexHelper.MAX_PACKET_SIZE_INT >> 8);
        requestPacket[3] = (byte)(ObexHelper.MAX_PACKET_SIZE_INT & 0xFF);
        if (head != null) {
            System.arraycopy(head, 0, requestPacket, 4, head.length);
        }
        if ((requestPacket.length + 3) > ObexHelper.MAX_PACKET_SIZE_INT) {
            throw new IOException("Packet size exceeds max packet size");
        }
        HeaderSet returnHeaderSet = new HeaderSet();
        sendRequest(ObexHelper.OBEX_OPCODE_CONNECT, requestPacket, returnHeaderSet, null);
        if (returnHeaderSet.responseCode == ResponseCodes.OBEX_HTTP_OK) {
            mObexConnected = true;
        }
        setRequestInactive();
        return returnHeaderSet;
    }
    public Operation get(HeaderSet header) throws IOException {
        if (!mObexConnected) {
            throw new IOException("Not connected to the server");
        }
        setRequestActive();
        ensureOpen();
        HeaderSet head;
        if (header == null) {
            head = new HeaderSet();
        } else {
            head = header;
            if (head.nonce != null) {
                mChallengeDigest = new byte[16];
                System.arraycopy(head.nonce, 0, mChallengeDigest, 0, 16);
            }
        }
        if (mConnectionId != null) {
            head.mConnectionID = new byte[4];
            System.arraycopy(mConnectionId, 0, head.mConnectionID, 0, 4);
        }
        return new ClientOperation(maxPacketSize, this, head, true);
    }
    public void setConnectionID(long id) {
        if ((id < 0) || (id > 0xFFFFFFFFL)) {
            throw new IllegalArgumentException("Connection ID is not in a valid range");
        }
        mConnectionId = ObexHelper.convertToByteArray(id);
    }
    public HeaderSet delete(HeaderSet header) throws IOException {
        Operation op = put(header);
        op.getResponseCode();
        HeaderSet returnValue = op.getReceivedHeader();
        op.close();
        return returnValue;
    }
    public HeaderSet disconnect(HeaderSet header) throws IOException {
        if (!mObexConnected) {
            throw new IOException("Not connected to the server");
        }
        setRequestActive();
        ensureOpen();
        byte[] head = null;
        if (header != null) {
            if (header.nonce != null) {
                mChallengeDigest = new byte[16];
                System.arraycopy(header.nonce, 0, mChallengeDigest, 0, 16);
            }
            if (mConnectionId != null) {
                header.mConnectionID = new byte[4];
                System.arraycopy(mConnectionId, 0, header.mConnectionID, 0, 4);
            }
            head = ObexHelper.createHeader(header, false);
            if ((head.length + 3) > maxPacketSize) {
                throw new IOException("Packet size exceeds max packet size");
            }
        } else {
            if (mConnectionId != null) {
                head = new byte[5];
                head[0] = (byte)HeaderSet.CONNECTION_ID;
                System.arraycopy(mConnectionId, 0, head, 1, 4);
            }
        }
        HeaderSet returnHeaderSet = new HeaderSet();
        sendRequest(ObexHelper.OBEX_OPCODE_DISCONNECT, head, returnHeaderSet, null);
        synchronized (this) {
            mObexConnected = false;
            setRequestInactive();
        }
        return returnHeaderSet;
    }
    public long getConnectionID() {
        if (mConnectionId == null) {
            return -1;
        }
        return ObexHelper.convertToLong(mConnectionId);
    }
    public Operation put(HeaderSet header) throws IOException {
        if (!mObexConnected) {
            throw new IOException("Not connected to the server");
        }
        setRequestActive();
        ensureOpen();
        HeaderSet head;
        if (header == null) {
            head = new HeaderSet();
        } else {
            head = header;
            if (head.nonce != null) {
                mChallengeDigest = new byte[16];
                System.arraycopy(head.nonce, 0, mChallengeDigest, 0, 16);
            }
        }
        if (mConnectionId != null) {
            head.mConnectionID = new byte[4];
            System.arraycopy(mConnectionId, 0, head.mConnectionID, 0, 4);
        }
        return new ClientOperation(maxPacketSize, this, head, false);
    }
    public void setAuthenticator(Authenticator auth) throws IOException {
        if (auth == null) {
            throw new IOException("Authenticator may not be null");
        }
        mAuthenticator = auth;
    }
    public HeaderSet setPath(HeaderSet header, boolean backup, boolean create) throws IOException {
        if (!mObexConnected) {
            throw new IOException("Not connected to the server");
        }
        setRequestActive();
        ensureOpen();
        int totalLength = 2;
        byte[] head = null;
        HeaderSet headset;
        if (header == null) {
            headset = new HeaderSet();
        } else {
            headset = header;
            if (headset.nonce != null) {
                mChallengeDigest = new byte[16];
                System.arraycopy(headset.nonce, 0, mChallengeDigest, 0, 16);
            }
        }
        if (headset.nonce != null) {
            mChallengeDigest = new byte[16];
            System.arraycopy(headset.nonce, 0, mChallengeDigest, 0, 16);
        }
        if (mConnectionId != null) {
            headset.mConnectionID = new byte[4];
            System.arraycopy(mConnectionId, 0, headset.mConnectionID, 0, 4);
        }
        head = ObexHelper.createHeader(headset, false);
        totalLength += head.length;
        if (totalLength > maxPacketSize) {
            throw new IOException("Packet size exceeds max packet size");
        }
        int flags = 0;
        if (backup) {
            flags++;
        }
        if (!create) {
            flags |= 2;
        }
        byte[] packet = new byte[totalLength];
        packet[0] = (byte)flags;
        packet[1] = (byte)0x00;
        if (headset != null) {
            System.arraycopy(head, 0, packet, 2, head.length);
        }
        HeaderSet returnHeaderSet = new HeaderSet();
        sendRequest(ObexHelper.OBEX_OPCODE_SETPATH, packet, returnHeaderSet, null);
        setRequestInactive();
        return returnHeaderSet;
    }
    public synchronized void ensureOpen() throws IOException {
        if (!mOpen) {
            throw new IOException("Connection closed");
        }
    }
    synchronized void setRequestInactive() {
        mRequestActive = false;
    }
    private synchronized void setRequestActive() throws IOException {
        if (mRequestActive) {
            throw new IOException("OBEX request is already being performed");
        }
        mRequestActive = true;
    }
    public boolean sendRequest(int opCode, byte[] head, HeaderSet header,
            PrivateInputStream privateInput) throws IOException {
        if (head != null) {
            if ((head.length + 3) > ObexHelper.MAX_PACKET_SIZE_INT) {
                throw new IOException("header too large ");
            }
        }
        int bytesReceived;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((byte)opCode);
        if (head == null) {
            out.write(0x00);
            out.write(0x03);
        } else {
            out.write((byte)((head.length + 3) >> 8));
            out.write((byte)(head.length + 3));
            out.write(head);
        }
        mOutput.write(out.toByteArray());
        mOutput.flush();
        header.responseCode = mInput.read();
        int length = ((mInput.read() << 8) | (mInput.read()));
        if (length > ObexHelper.MAX_PACKET_SIZE_INT) {
            throw new IOException("Packet received exceeds packet size limit");
        }
        if (length > ObexHelper.BASE_PACKET_LENGTH) {
            byte[] data = null;
            if (opCode == ObexHelper.OBEX_OPCODE_CONNECT) {
                @SuppressWarnings("unused")
                int version = mInput.read();
                @SuppressWarnings("unused")
                int flags = mInput.read();
                maxPacketSize = (mInput.read() << 8) + mInput.read();
                if (maxPacketSize > ObexHelper.MAX_PACKET_SIZE_INT) {
                    maxPacketSize = ObexHelper.MAX_PACKET_SIZE_INT;
                }
                if (length > 7) {
                    data = new byte[length - 7];
                    bytesReceived = mInput.read(data);
                    while (bytesReceived != (length - 7)) {
                        bytesReceived += mInput.read(data, bytesReceived, data.length
                                - bytesReceived);
                    }
                } else {
                    return true;
                }
            } else {
                data = new byte[length - 3];
                bytesReceived = mInput.read(data);
                while (bytesReceived != (length - 3)) {
                    bytesReceived += mInput.read(data, bytesReceived, data.length - bytesReceived);
                }
                if (opCode == ObexHelper.OBEX_OPCODE_ABORT) {
                    return true;
                }
            }
            byte[] body = ObexHelper.updateHeaderSet(header, data);
            if ((privateInput != null) && (body != null)) {
                privateInput.writeBytes(body, 1);
            }
            if (header.mConnectionID != null) {
                mConnectionId = new byte[4];
                System.arraycopy(header.mConnectionID, 0, mConnectionId, 0, 4);
            }
            if (header.mAuthResp != null) {
                if (!handleAuthResp(header.mAuthResp)) {
                    setRequestInactive();
                    throw new IOException("Authentication Failed");
                }
            }
            if ((header.responseCode == ResponseCodes.OBEX_HTTP_UNAUTHORIZED)
                    && (header.mAuthChall != null)) {
                if (handleAuthChall(header)) {
                    out.write((byte)HeaderSet.AUTH_RESPONSE);
                    out.write((byte)((header.mAuthResp.length + 3) >> 8));
                    out.write((byte)(header.mAuthResp.length + 3));
                    out.write(header.mAuthResp);
                    header.mAuthChall = null;
                    header.mAuthResp = null;
                    byte[] sendHeaders = new byte[out.size() - 3];
                    System.arraycopy(out.toByteArray(), 3, sendHeaders, 0, sendHeaders.length);
                    return sendRequest(opCode, sendHeaders, header, privateInput);
                }
            }
        }
        return true;
    }
    public void close() throws IOException {
        mOpen = false;
        mInput.close();
        mOutput.close();
    }
}
