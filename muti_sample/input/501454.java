public final class ServerOperation implements Operation, BaseStream {
    public boolean isAborted;
    public HeaderSet requestHeader;
    public HeaderSet replyHeader;
    public boolean finalBitSet;
    private InputStream mInput;
    private ServerSession mParent;
    private int mMaxPacketLength;
    private int mResponseSize;
    private boolean mClosed;
    private boolean mGetOperation;
    private PrivateInputStream mPrivateInput;
    private PrivateOutputStream mPrivateOutput;
    private boolean mPrivateOutputOpen;
    private String mExceptionString;
    private ServerRequestHandler mListener;
    private boolean mRequestFinished;
    private boolean mHasBody;
    public ServerOperation(ServerSession p, InputStream in, int request, int maxSize,
            ServerRequestHandler listen) throws IOException {
        isAborted = false;
        mParent = p;
        mInput = in;
        mMaxPacketLength = maxSize;
        mClosed = false;
        requestHeader = new HeaderSet();
        replyHeader = new HeaderSet();
        mPrivateInput = new PrivateInputStream(this);
        mResponseSize = 3;
        mListener = listen;
        mRequestFinished = false;
        mPrivateOutputOpen = false;
        mHasBody = false;
        int bytesReceived;
        if ((request == 0x02) || (request == 0x82)) {
            mGetOperation = false;
            if ((request & 0x80) == 0) {
                finalBitSet = false;
            } else {
                finalBitSet = true;
                mRequestFinished = true;
            }
        } else if ((request == 0x03) || (request == 0x83)) {
            mGetOperation = true;
            finalBitSet = false;
            if (request == 0x83) {
                mRequestFinished = true;
            }
        } else {
            throw new IOException("ServerOperation can not handle such request");
        }
        int length = in.read();
        length = (length << 8) + in.read();
        if (length > ObexHelper.MAX_PACKET_SIZE_INT) {
            mParent.sendResponse(ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE, null);
            throw new IOException("Packet received was too large");
        }
        if (length > 3) {
            byte[] data = new byte[length - 3];
            bytesReceived = in.read(data);
            while (bytesReceived != data.length) {
                bytesReceived += in.read(data, bytesReceived, data.length - bytesReceived);
            }
            byte[] body = ObexHelper.updateHeaderSet(requestHeader, data);
            if (body != null) {
                mHasBody = true;
            }
            if (mListener.getConnectionId() != -1 && requestHeader.mConnectionID != null) {
                mListener.setConnectionId(ObexHelper.convertToLong(requestHeader.mConnectionID));
            } else {
                mListener.setConnectionId(1);
            }
            if (requestHeader.mAuthResp != null) {
                if (!mParent.handleAuthResp(requestHeader.mAuthResp)) {
                    mExceptionString = "Authentication Failed";
                    mParent.sendResponse(ResponseCodes.OBEX_HTTP_UNAUTHORIZED, null);
                    mClosed = true;
                    requestHeader.mAuthResp = null;
                    return;
                }
            }
            if (requestHeader.mAuthChall != null) {
                mParent.handleAuthChall(requestHeader);
                replyHeader.mAuthResp = new byte[requestHeader.mAuthResp.length];
                System.arraycopy(requestHeader.mAuthResp, 0, replyHeader.mAuthResp, 0,
                        replyHeader.mAuthResp.length);
                requestHeader.mAuthResp = null;
                requestHeader.mAuthChall = null;
            }
            if (body != null) {
                mPrivateInput.writeBytes(body, 1);
            } else {
                while ((!mGetOperation) && (!finalBitSet)) {
                    sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                    if (mPrivateInput.available() > 0) {
                        break;
                    }
                }
            }
        }
        while ((!mGetOperation) && (!finalBitSet) && (mPrivateInput.available() == 0)) {
            sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
            if (mPrivateInput.available() > 0) {
                break;
            }
        }
        while (mGetOperation && !mRequestFinished) {
            sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
        }
    }
    public boolean isValidBody() {
        return mHasBody;
    }
    public synchronized boolean continueOperation(boolean sendEmpty, boolean inStream)
            throws IOException {
        if (!mGetOperation) {
            if (!finalBitSet) {
                if (sendEmpty) {
                    sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                    return true;
                } else {
                    if ((mResponseSize > 3) || (mPrivateOutput.size() > 0)) {
                        sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
            return true;
        }
    }
    public synchronized boolean sendReply(int type) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesReceived;
        long id = mListener.getConnectionId();
        if (id == -1) {
            replyHeader.mConnectionID = null;
        } else {
            replyHeader.mConnectionID = ObexHelper.convertToByteArray(id);
        }
        byte[] headerArray = ObexHelper.createHeader(replyHeader, true);
        int bodyLength = -1;
        int orginalBodyLength = -1;
        if (mPrivateOutput != null) {
            bodyLength = mPrivateOutput.size();
            orginalBodyLength = bodyLength;
        }
        if ((ObexHelper.BASE_PACKET_LENGTH + headerArray.length) > mMaxPacketLength) {
            int end = 0;
            int start = 0;
            while (end != headerArray.length) {
                end = ObexHelper.findHeaderEnd(headerArray, start, mMaxPacketLength
                        - ObexHelper.BASE_PACKET_LENGTH);
                if (end == -1) {
                    mClosed = true;
                    if (mPrivateInput != null) {
                        mPrivateInput.close();
                    }
                    if (mPrivateOutput != null) {
                        mPrivateOutput.close();
                    }
                    mParent.sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    throw new IOException("OBEX Packet exceeds max packet size");
                }
                byte[] sendHeader = new byte[end - start];
                System.arraycopy(headerArray, start, sendHeader, 0, sendHeader.length);
                mParent.sendResponse(type, sendHeader);
                start = end;
            }
            if (bodyLength > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            out.write(headerArray);
        }
        if (mGetOperation && type == ResponseCodes.OBEX_HTTP_OK) {
            finalBitSet = true;
        }
        if ((finalBitSet) || (headerArray.length < (mMaxPacketLength - 20))) {
            if (bodyLength > 0) {
                if (bodyLength > (mMaxPacketLength - headerArray.length - 6)) {
                    bodyLength = mMaxPacketLength - headerArray.length - 6;
                }
                byte[] body = mPrivateOutput.readBytes(bodyLength);
                if ((finalBitSet) || (mPrivateOutput.isClosed())) {
                    out.write(0x49);
                } else {
                    out.write(0x48);
                }
                bodyLength += 3;
                out.write((byte)(bodyLength >> 8));
                out.write((byte)bodyLength);
                out.write(body);
            }
        }
        if ((finalBitSet) && (type == ResponseCodes.OBEX_HTTP_OK) && (orginalBodyLength <= 0)) {
            out.write(0x49);
            orginalBodyLength = 3;
            out.write((byte)(orginalBodyLength >> 8));
            out.write((byte)orginalBodyLength);
        }
        mResponseSize = 3;
        mParent.sendResponse(type, out.toByteArray());
        if (type == ResponseCodes.OBEX_HTTP_CONTINUE) {
            int headerID = mInput.read();
            int length = mInput.read();
            length = (length << 8) + mInput.read();
            if ((headerID != ObexHelper.OBEX_OPCODE_PUT)
                    && (headerID != ObexHelper.OBEX_OPCODE_PUT_FINAL)
                    && (headerID != ObexHelper.OBEX_OPCODE_GET)
                    && (headerID != ObexHelper.OBEX_OPCODE_GET_FINAL)) {
                if (length > 3) {
                    byte[] temp = new byte[length];
                    bytesReceived = mInput.read(temp);
                    while (bytesReceived != length) {
                        bytesReceived += mInput.read(temp, bytesReceived, length - bytesReceived);
                    }
                }
                if (headerID == ObexHelper.OBEX_OPCODE_ABORT) {
                    mParent.sendResponse(ResponseCodes.OBEX_HTTP_OK, null);
                    mClosed = true;
                    isAborted = true;
                    mExceptionString = "Abort Received";
                    throw new IOException("Abort Received");
                } else {
                    mParent.sendResponse(ResponseCodes.OBEX_HTTP_BAD_REQUEST, null);
                    mClosed = true;
                    mExceptionString = "Bad Request Received";
                    throw new IOException("Bad Request Received");
                }
            } else {
                if ((headerID == ObexHelper.OBEX_OPCODE_PUT_FINAL)) {
                    finalBitSet = true;
                } else if (headerID == ObexHelper.OBEX_OPCODE_GET_FINAL) {
                    mRequestFinished = true;
                }
                if (length > ObexHelper.MAX_PACKET_SIZE_INT) {
                    mParent.sendResponse(ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE, null);
                    throw new IOException("Packet received was too large");
                }
                if (length > 3) {
                    byte[] data = new byte[length - 3];
                    bytesReceived = mInput.read(data);
                    while (bytesReceived != data.length) {
                        bytesReceived += mInput.read(data, bytesReceived, data.length
                                - bytesReceived);
                    }
                    byte[] body = ObexHelper.updateHeaderSet(requestHeader, data);
                    if (body != null) {
                        mHasBody = true;
                    }
                    if (mListener.getConnectionId() != -1 && requestHeader.mConnectionID != null) {
                        mListener.setConnectionId(ObexHelper
                                .convertToLong(requestHeader.mConnectionID));
                    } else {
                        mListener.setConnectionId(1);
                    }
                    if (requestHeader.mAuthResp != null) {
                        if (!mParent.handleAuthResp(requestHeader.mAuthResp)) {
                            mExceptionString = "Authentication Failed";
                            mParent.sendResponse(ResponseCodes.OBEX_HTTP_UNAUTHORIZED, null);
                            mClosed = true;
                            requestHeader.mAuthResp = null;
                            return false;
                        }
                        requestHeader.mAuthResp = null;
                    }
                    if (requestHeader.mAuthChall != null) {
                        mParent.handleAuthChall(requestHeader);
                        replyHeader.mAuthResp = new byte[requestHeader.mAuthResp.length];
                        System.arraycopy(requestHeader.mAuthResp, 0, replyHeader.mAuthResp, 0,
                                replyHeader.mAuthResp.length);
                        requestHeader.mAuthResp = null;
                        requestHeader.mAuthChall = null;
                    }
                    if (body != null) {
                        mPrivateInput.writeBytes(body, 1);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
    public void abort() throws IOException {
        throw new IOException("Called from a server");
    }
    public HeaderSet getReceivedHeader() throws IOException {
        ensureOpen();
        return requestHeader;
    }
    public void sendHeaders(HeaderSet headers) throws IOException {
        ensureOpen();
        if (headers == null) {
            throw new IOException("Headers may not be null");
        }
        int[] headerList = headers.getHeaderList();
        if (headerList != null) {
            for (int i = 0; i < headerList.length; i++) {
                replyHeader.setHeader(headerList[i], headers.getHeader(headerList[i]));
            }
        }
    }
    public int getResponseCode() throws IOException {
        throw new IOException("Called from a server");
    }
    public String getEncoding() {
        return null;
    }
    public String getType() {
        try {
            return (String)requestHeader.getHeader(HeaderSet.TYPE);
        } catch (IOException e) {
            return null;
        }
    }
    public long getLength() {
        try {
            Long temp = (Long)requestHeader.getHeader(HeaderSet.LENGTH);
            if (temp == null) {
                return -1;
            } else {
                return temp.longValue();
            }
        } catch (IOException e) {
            return -1;
        }
    }
    public int getMaxPacketSize() {
        return mMaxPacketLength - 6 - getHeaderLength();
    }
    public int getHeaderLength() {
        long id = mListener.getConnectionId();
        if (id == -1) {
            replyHeader.mConnectionID = null;
        } else {
            replyHeader.mConnectionID = ObexHelper.convertToByteArray(id);
        }
        byte[] headerArray = ObexHelper.createHeader(replyHeader, false);
        return headerArray.length;
    }
    public InputStream openInputStream() throws IOException {
        ensureOpen();
        return mPrivateInput;
    }
    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }
    public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        if (mPrivateOutputOpen) {
            throw new IOException("no more input streams available, stream already opened");
        }
        if (!mRequestFinished) {
            throw new IOException("no  output streams available ,request not finished");
        }
        if (mPrivateOutput == null) {
            mPrivateOutput = new PrivateOutputStream(this, getMaxPacketSize());
        }
        mPrivateOutputOpen = true;
        return mPrivateOutput;
    }
    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }
    public void close() throws IOException {
        ensureOpen();
        mClosed = true;
    }
    public void ensureOpen() throws IOException {
        if (mExceptionString != null) {
            throw new IOException(mExceptionString);
        }
        if (mClosed) {
            throw new IOException("Operation has already ended");
        }
    }
    public void ensureNotDone() throws IOException {
    }
    public void streamClosed(boolean inStream) throws IOException {
    }
}
