public final class ServerSession extends ObexSession implements Runnable {
    private static final String TAG = "Obex ServerSession";
    private ObexTransport mTransport;
    private InputStream mInput;
    private OutputStream mOutput;
    private ServerRequestHandler mListener;
    private Thread mProcessThread;
    private int mMaxPacketLength;
    private boolean mClosed;
    public ServerSession(ObexTransport trans, ServerRequestHandler handler, Authenticator auth)
            throws IOException {
        mAuthenticator = auth;
        mTransport = trans;
        mInput = mTransport.openInputStream();
        mOutput = mTransport.openOutputStream();
        mListener = handler;
        mMaxPacketLength = 256;
        mClosed = false;
        mProcessThread = new Thread(this);
        mProcessThread.start();
    }
    public void run() {
        try {
            boolean done = false;
            while (!done && !mClosed) {
                int requestType = mInput.read();
                switch (requestType) {
                    case ObexHelper.OBEX_OPCODE_CONNECT:
                        handleConnectRequest();
                        break;
                    case ObexHelper.OBEX_OPCODE_DISCONNECT:
                        handleDisconnectRequest();
                        done = true;
                        break;
                    case ObexHelper.OBEX_OPCODE_GET:
                    case ObexHelper.OBEX_OPCODE_GET_FINAL:
                        handleGetRequest(requestType);
                        break;
                    case ObexHelper.OBEX_OPCODE_PUT:
                    case ObexHelper.OBEX_OPCODE_PUT_FINAL:
                        handlePutRequest(requestType);
                        break;
                    case ObexHelper.OBEX_OPCODE_SETPATH:
                        handleSetPathRequest();
                        break;
                    case ObexHelper.OBEX_OPCODE_ABORT:
                        handleAbortRequest();
                        break;
                    case -1:
                        done = true;
                        break;
                    default:
                        int length = mInput.read();
                        length = (length << 8) + mInput.read();
                        for (int i = 3; i < length; i++) {
                            mInput.read();
                        }
                        sendResponse(ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED, null);
                }
            }
        } catch (NullPointerException e) {
            Log.d(TAG, e.toString());
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        close();
    }
    private void handleAbortRequest() throws IOException {
        int code = ResponseCodes.OBEX_HTTP_OK;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = mInput.read();
        length = (length << 8) + mInput.read();
        if (length > ObexHelper.MAX_PACKET_SIZE_INT) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
        } else {
            for (int i = 3; i < length; i++) {
                mInput.read();
            }
            code = mListener.onAbort(request, reply);
            Log.v(TAG, "onAbort request handler return value- " + code);
            code = validateResponseCode(code);
        }
        sendResponse(code, null);
    }
    private void handlePutRequest(int type) throws IOException {
        ServerOperation op = new ServerOperation(this, mInput, type, mMaxPacketLength, mListener);
        try {
            int response = -1;
            if ((op.finalBitSet) && !op.isValidBody()) {
                response = validateResponseCode(mListener
                        .onDelete(op.requestHeader, op.replyHeader));
            } else {
                response = validateResponseCode(mListener.onPut(op));
            }
            if (response != ResponseCodes.OBEX_HTTP_OK && !op.isAborted) {
                op.sendReply(response);
            } else if (!op.isAborted) {
                while (!op.finalBitSet) {
                    op.sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                }
                op.sendReply(response);
            }
        } catch (Exception e) {
            if (!op.isAborted) {
                sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
            }
        }
    }
    private void handleGetRequest(int type) throws IOException {
        ServerOperation op = new ServerOperation(this, mInput, type, mMaxPacketLength, mListener);
        try {
            int response = validateResponseCode(mListener.onGet(op));
            if (!op.isAborted) {
                op.sendReply(response);
            }
        } catch (Exception e) {
            sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
        }
    }
    public void sendResponse(int code, byte[] header) throws IOException {
        int totalLength = 3;
        byte[] data = null;
        if (header != null) {
            totalLength += header.length;
            data = new byte[totalLength];
            data[0] = (byte)code;
            data[1] = (byte)(totalLength >> 8);
            data[2] = (byte)totalLength;
            System.arraycopy(header, 0, data, 3, header.length);
        } else {
            data = new byte[totalLength];
            data[0] = (byte)code;
            data[1] = (byte)0x00;
            data[2] = (byte)totalLength;
        }
        mOutput.write(data);
        mOutput.flush();
    }
    private void handleSetPathRequest() throws IOException {
        int length;
        int flags;
        @SuppressWarnings("unused")
        int constants;
        int totalLength = 3;
        byte[] head = null;
        int code = -1;
        int bytesReceived;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        length = mInput.read();
        length = (length << 8) + mInput.read();
        flags = mInput.read();
        constants = mInput.read();
        if (length > ObexHelper.MAX_PACKET_SIZE_INT) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 3;
        } else {
            if (length > 5) {
                byte[] headers = new byte[length - 5];
                bytesReceived = mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += mInput.read(headers, bytesReceived, headers.length
                            - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
                if (mListener.getConnectionId() != -1 && request.mConnectionID != null) {
                    mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
                } else {
                    mListener.setConnectionId(1);
                }
                if (request.mAuthResp != null) {
                    if (!handleAuthResp(request.mAuthResp)) {
                        code = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                        mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte)0x01,
                                request.mAuthResp));
                    }
                    request.mAuthResp = null;
                }
            }
            if (code != ResponseCodes.OBEX_HTTP_UNAUTHORIZED) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    reply.mAuthResp = new byte[request.mAuthResp.length];
                    System.arraycopy(request.mAuthResp, 0, reply.mAuthResp, 0,
                            reply.mAuthResp.length);
                    request.mAuthChall = null;
                    request.mAuthResp = null;
                }
                boolean backup = false;
                boolean create = true;
                if (!((flags & 1) == 0)) {
                    backup = true;
                }
                if (!((flags & 2) == 0)) {
                    create = false;
                }
                try {
                    code = mListener.onSetPath(request, reply, backup, create);
                } catch (Exception e) {
                    sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    return;
                }
                code = validateResponseCode(code);
                if (reply.nonce != null) {
                    mChallengeDigest = new byte[16];
                    System.arraycopy(reply.nonce, 0, mChallengeDigest, 0, 16);
                } else {
                    mChallengeDigest = null;
                }
                long id = mListener.getConnectionId();
                if (id == -1) {
                    reply.mConnectionID = null;
                } else {
                    reply.mConnectionID = ObexHelper.convertToByteArray(id);
                }
                head = ObexHelper.createHeader(reply, false);
                totalLength += head.length;
                if (totalLength > mMaxPacketLength) {
                    totalLength = 3;
                    head = null;
                    code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                }
            }
        }
        byte[] replyData = new byte[totalLength];
        replyData[0] = (byte)code;
        replyData[1] = (byte)(totalLength >> 8);
        replyData[2] = (byte)totalLength;
        if (head != null) {
            System.arraycopy(head, 0, replyData, 3, head.length);
        }
        mOutput.write(replyData);
        mOutput.flush();
    }
    private void handleDisconnectRequest() throws IOException {
        int length;
        int code = ResponseCodes.OBEX_HTTP_OK;
        int totalLength = 3;
        byte[] head = null;
        int bytesReceived;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        length = mInput.read();
        length = (length << 8) + mInput.read();
        if (length > ObexHelper.MAX_PACKET_SIZE_INT) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 3;
        } else {
            if (length > 3) {
                byte[] headers = new byte[length - 3];
                bytesReceived = mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += mInput.read(headers, bytesReceived, headers.length
                            - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
            }
            if (mListener.getConnectionId() != -1 && request.mConnectionID != null) {
                mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
            } else {
                mListener.setConnectionId(1);
            }
            if (request.mAuthResp != null) {
                if (!handleAuthResp(request.mAuthResp)) {
                    code = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                    mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte)0x01,
                            request.mAuthResp));
                }
                request.mAuthResp = null;
            }
            if (code != ResponseCodes.OBEX_HTTP_UNAUTHORIZED) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    request.mAuthChall = null;
                }
                try {
                    mListener.onDisconnect(request, reply);
                } catch (Exception e) {
                    sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    return;
                }
                long id = mListener.getConnectionId();
                if (id == -1) {
                    reply.mConnectionID = null;
                } else {
                    reply.mConnectionID = ObexHelper.convertToByteArray(id);
                }
                head = ObexHelper.createHeader(reply, false);
                totalLength += head.length;
                if (totalLength > mMaxPacketLength) {
                    totalLength = 3;
                    head = null;
                    code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                }
            }
        }
        byte[] replyData;
        if (head != null) {
            replyData = new byte[3 + head.length];
        } else {
            replyData = new byte[3];
        }
        replyData[0] = (byte)code;
        replyData[1] = (byte)(totalLength >> 8);
        replyData[2] = (byte)totalLength;
        if (head != null) {
            System.arraycopy(head, 0, replyData, 3, head.length);
        }
        mOutput.write(replyData);
        mOutput.flush();
    }
    private void handleConnectRequest() throws IOException {
        int packetLength;
        @SuppressWarnings("unused")
        int version;
        @SuppressWarnings("unused")
        int flags;
        int totalLength = 7;
        byte[] head = null;
        int code = -1;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int bytesReceived;
        packetLength = mInput.read();
        packetLength = (packetLength << 8) + mInput.read();
        version = mInput.read();
        flags = mInput.read();
        mMaxPacketLength = mInput.read();
        mMaxPacketLength = (mMaxPacketLength << 8) + mInput.read();
        if (mMaxPacketLength > ObexHelper.MAX_PACKET_SIZE_INT) {
            mMaxPacketLength = ObexHelper.MAX_PACKET_SIZE_INT;
        }
        if (packetLength > ObexHelper.MAX_PACKET_SIZE_INT) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 7;
        } else {
            if (packetLength > 7) {
                byte[] headers = new byte[packetLength - 7];
                bytesReceived = mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += mInput.read(headers, bytesReceived, headers.length
                            - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
            }
            if (mListener.getConnectionId() != -1 && request.mConnectionID != null) {
                mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
            } else {
                mListener.setConnectionId(1);
            }
            if (request.mAuthResp != null) {
                if (!handleAuthResp(request.mAuthResp)) {
                    code = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                    mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte)0x01,
                            request.mAuthResp));
                }
                request.mAuthResp = null;
            }
            if (code != ResponseCodes.OBEX_HTTP_UNAUTHORIZED) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    reply.mAuthResp = new byte[request.mAuthResp.length];
                    System.arraycopy(request.mAuthResp, 0, reply.mAuthResp, 0,
                            reply.mAuthResp.length);
                    request.mAuthChall = null;
                    request.mAuthResp = null;
                }
                try {
                    code = mListener.onConnect(request, reply);
                    code = validateResponseCode(code);
                    if (reply.nonce != null) {
                        mChallengeDigest = new byte[16];
                        System.arraycopy(reply.nonce, 0, mChallengeDigest, 0, 16);
                    } else {
                        mChallengeDigest = null;
                    }
                    long id = mListener.getConnectionId();
                    if (id == -1) {
                        reply.mConnectionID = null;
                    } else {
                        reply.mConnectionID = ObexHelper.convertToByteArray(id);
                    }
                    head = ObexHelper.createHeader(reply, false);
                    totalLength += head.length;
                    if (totalLength > mMaxPacketLength) {
                        totalLength = 7;
                        head = null;
                        code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    totalLength = 7;
                    head = null;
                    code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                }
            }
        }
        byte[] length = ObexHelper.convertToByteArray(totalLength);
        byte[] sendData = new byte[totalLength];
        sendData[0] = (byte)code;
        sendData[1] = length[2];
        sendData[2] = length[3];
        sendData[3] = (byte)0x10;
        sendData[4] = (byte)0x00;
        sendData[5] = (byte)(ObexHelper.MAX_PACKET_SIZE_INT >> 8);
        sendData[6] = (byte)(ObexHelper.MAX_PACKET_SIZE_INT & 0xFF);
        if (head != null) {
            System.arraycopy(head, 0, sendData, 7, head.length);
        }
        mOutput.write(sendData);
        mOutput.flush();
    }
    public synchronized void close() {
        if (mListener != null) {
            mListener.onClose();
        }
        try {
            mInput.close();
            mOutput.close();
            mTransport.close();
            mClosed = true;
        } catch (Exception e) {
        }
        mTransport = null;
        mInput = null;
        mOutput = null;
        mListener = null;
    }
    private int validateResponseCode(int code) {
        if ((code >= ResponseCodes.OBEX_HTTP_OK) && (code <= ResponseCodes.OBEX_HTTP_PARTIAL)) {
            return code;
        }
        if ((code >= ResponseCodes.OBEX_HTTP_MULT_CHOICE)
                && (code <= ResponseCodes.OBEX_HTTP_USE_PROXY)) {
            return code;
        }
        if ((code >= ResponseCodes.OBEX_HTTP_BAD_REQUEST)
                && (code <= ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE)) {
            return code;
        }
        if ((code >= ResponseCodes.OBEX_HTTP_INTERNAL_ERROR)
                && (code <= ResponseCodes.OBEX_HTTP_VERSION)) {
            return code;
        }
        if ((code >= ResponseCodes.OBEX_DATABASE_FULL)
                && (code <= ResponseCodes.OBEX_DATABASE_LOCKED)) {
            return code;
        }
        return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
    }
}
