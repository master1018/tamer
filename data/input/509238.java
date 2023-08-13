public final class ClientOperation implements Operation, BaseStream {
    private ClientSession mParent;
    private boolean mInputOpen;
    private PrivateInputStream mPrivateInput;
    private boolean mPrivateInputOpen;
    private PrivateOutputStream mPrivateOutput;
    private boolean mPrivateOutputOpen;
    private String mExceptionMessage;
    private int mMaxPacketSize;
    private boolean mOperationDone;
    private boolean mGetOperation;
    private HeaderSet mRequestHeader;
    private HeaderSet mReplyHeader;
    private boolean mEndOfBodySent;
    public ClientOperation(int maxSize, ClientSession p, HeaderSet header, boolean type)
            throws IOException {
        mParent = p;
        mEndOfBodySent = false;
        mInputOpen = true;
        mOperationDone = false;
        mMaxPacketSize = maxSize;
        mGetOperation = type;
        mPrivateInputOpen = false;
        mPrivateOutputOpen = false;
        mPrivateInput = null;
        mPrivateOutput = null;
        mReplyHeader = new HeaderSet();
        mRequestHeader = new HeaderSet();
        int[] headerList = header.getHeaderList();
        if (headerList != null) {
            for (int i = 0; i < headerList.length; i++) {
                mRequestHeader.setHeader(headerList[i], header.getHeader(headerList[i]));
            }
        }
        if ((header).mAuthChall != null) {
            mRequestHeader.mAuthChall = new byte[(header).mAuthChall.length];
            System.arraycopy((header).mAuthChall, 0, mRequestHeader.mAuthChall, 0,
                    (header).mAuthChall.length);
        }
        if ((header).mAuthResp != null) {
            mRequestHeader.mAuthResp = new byte[(header).mAuthResp.length];
            System.arraycopy((header).mAuthResp, 0, mRequestHeader.mAuthResp, 0,
                    (header).mAuthResp.length);
        }
    }
    public synchronized void abort() throws IOException {
        ensureOpen();
        if ((mOperationDone) && (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_CONTINUE)) {
            throw new IOException("Operation has already ended");
        }
        mExceptionMessage = "Operation aborted";
        if ((!mOperationDone) && (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE)) {
            mOperationDone = true;
            mParent.sendRequest(ObexHelper.OBEX_OPCODE_ABORT, null, mReplyHeader, null);
            if (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_OK) {
                throw new IOException("Invalid response code from server");
            }
            mExceptionMessage = null;
        }
        close();
    }
    public synchronized int getResponseCode() throws IOException {
        if ((mReplyHeader.responseCode == -1)
                || (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE)) {
            validateConnection();
        }
        return mReplyHeader.responseCode;
    }
    public String getEncoding() {
        return null;
    }
    public String getType() {
        try {
            return (String)mReplyHeader.getHeader(HeaderSet.TYPE);
        } catch (IOException e) {
            return null;
        }
    }
    public long getLength() {
        try {
            Long temp = (Long)mReplyHeader.getHeader(HeaderSet.LENGTH);
            if (temp == null) {
                return -1;
            } else {
                return temp.longValue();
            }
        } catch (IOException e) {
            return -1;
        }
    }
    public InputStream openInputStream() throws IOException {
        ensureOpen();
        if (mPrivateInputOpen)
            throw new IOException("no more input streams available");
        if (mGetOperation) {
            validateConnection();
        } else {
            if (mPrivateInput == null) {
                mPrivateInput = new PrivateInputStream(this);
            }
        }
        mPrivateInputOpen = true;
        return mPrivateInput;
    }
    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }
    public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        ensureNotDone();
        if (mPrivateOutputOpen)
            throw new IOException("no more output streams available");
        if (mPrivateOutput == null) {
            mPrivateOutput = new PrivateOutputStream(this, getMaxPacketSize());
        }
        mPrivateOutputOpen = true;
        return mPrivateOutput;
    }
    public int getMaxPacketSize() {
        return mMaxPacketSize - 6 - getHeaderLength();
    }
    public int getHeaderLength() {
        byte[] headerArray = ObexHelper.createHeader(mRequestHeader, false);
        return headerArray.length;
    }
    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }
    public void close() throws IOException {
        mInputOpen = false;
        mPrivateInputOpen = false;
        mPrivateOutputOpen = false;
        mParent.setRequestInactive();
    }
    public HeaderSet getReceivedHeader() throws IOException {
        ensureOpen();
        return mReplyHeader;
    }
    public void sendHeaders(HeaderSet headers) throws IOException {
        ensureOpen();
        if (mOperationDone) {
            throw new IOException("Operation has already exchanged all data");
        }
        if (headers == null) {
            throw new IOException("Headers may not be null");
        }
        int[] headerList = headers.getHeaderList();
        if (headerList != null) {
            for (int i = 0; i < headerList.length; i++) {
                mRequestHeader.setHeader(headerList[i], headers.getHeader(headerList[i]));
            }
        }
    }
    public void ensureNotDone() throws IOException {
        if (mOperationDone) {
            throw new IOException("Operation has completed");
        }
    }
    public void ensureOpen() throws IOException {
        mParent.ensureOpen();
        if (mExceptionMessage != null) {
            throw new IOException(mExceptionMessage);
        }
        if (!mInputOpen) {
            throw new IOException("Operation has already ended");
        }
    }
    private void validateConnection() throws IOException {
        ensureOpen();
        if (mPrivateInput == null) {
            startProcessing();
        }
    }
    private boolean sendRequest(int opCode) throws IOException {
        boolean returnValue = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bodyLength = -1;
        byte[] headerArray = ObexHelper.createHeader(mRequestHeader, true);
        if (mPrivateOutput != null) {
            bodyLength = mPrivateOutput.size();
        }
        if ((ObexHelper.BASE_PACKET_LENGTH + headerArray.length) > mMaxPacketSize) {
            int end = 0;
            int start = 0;
            while (end != headerArray.length) {
                end = ObexHelper.findHeaderEnd(headerArray, start, mMaxPacketSize
                        - ObexHelper.BASE_PACKET_LENGTH);
                if (end == -1) {
                    mOperationDone = true;
                    abort();
                    mExceptionMessage = "Header larger then can be sent in a packet";
                    mInputOpen = false;
                    if (mPrivateInput != null) {
                        mPrivateInput.close();
                    }
                    if (mPrivateOutput != null) {
                        mPrivateOutput.close();
                    }
                    throw new IOException("OBEX Packet exceeds max packet size");
                }
                byte[] sendHeader = new byte[end - start];
                System.arraycopy(headerArray, start, sendHeader, 0, sendHeader.length);
                if (!mParent.sendRequest(opCode, sendHeader, mReplyHeader, mPrivateInput)) {
                    return false;
                }
                if (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_CONTINUE) {
                    return false;
                }
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
        if (bodyLength > 0) {
            if (bodyLength > (mMaxPacketSize - headerArray.length - 6)) {
                returnValue = true;
                bodyLength = mMaxPacketSize - headerArray.length - 6;
            }
            byte[] body = mPrivateOutput.readBytes(bodyLength);
            if ((mPrivateOutput.isClosed()) && (!returnValue) && (!mEndOfBodySent)
                    && ((opCode & 0x80) != 0)) {
                out.write(0x49);
                mEndOfBodySent = true;
            } else {
                out.write(0x48);
            }
            bodyLength += 3;
            out.write((byte)(bodyLength >> 8));
            out.write((byte)bodyLength);
            if (body != null) {
                out.write(body);
            }
        }
        if (mPrivateOutputOpen && bodyLength <= 0 && !mEndOfBodySent) {
            if ((opCode & 0x80) == 0) {
                out.write(0x48);
            } else {
                out.write(0x49);
                mEndOfBodySent = true;
            }
            bodyLength = 3;
            out.write((byte)(bodyLength >> 8));
            out.write((byte)bodyLength);
        }
        if (out.size() == 0) {
            if (!mParent.sendRequest(opCode, null, mReplyHeader, mPrivateInput)) {
                return false;
            }
            return returnValue;
        }
        if ((out.size() > 0)
                && (!mParent.sendRequest(opCode, out.toByteArray(), mReplyHeader, mPrivateInput))) {
            return false;
        }
        if ((mPrivateOutput != null) && (mPrivateOutput.size() > 0))
            returnValue = true;
        return returnValue;
    }
    private synchronized void startProcessing() throws IOException {
        if (mPrivateInput == null) {
            mPrivateInput = new PrivateInputStream(this);
        }
        boolean more = true;
        if (mGetOperation) {
            if (!mOperationDone) {
                mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                while ((more) && (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE)) {
                    more = sendRequest(0x03);
                }
                if (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE) {
                    mParent.sendRequest(0x83, null, mReplyHeader, mPrivateInput);
                }
                if (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_CONTINUE) {
                    mOperationDone = true;
                }
            }
        } else {
            if (!mOperationDone) {
                mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                while ((more) && (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE)) {
                    more = sendRequest(0x02);
                }
            }
            if (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE) {
                mParent.sendRequest(0x82, null, mReplyHeader, mPrivateInput);
            }
            if (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_CONTINUE) {
                mOperationDone = true;
            }
        }
    }
    public synchronized boolean continueOperation(boolean sendEmpty, boolean inStream)
            throws IOException {
        if (mGetOperation) {
            if ((inStream) && (!mOperationDone)) {
                mParent.sendRequest(0x83, null, mReplyHeader, mPrivateInput);
                if (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_CONTINUE) {
                    mOperationDone = true;
                }
                return true;
            } else if ((!inStream) && (!mOperationDone)) {
                if (mPrivateInput == null) {
                    mPrivateInput = new PrivateInputStream(this);
                }
                sendRequest(0x03);
                return true;
            } else if (mOperationDone) {
                return false;
            }
        } else {
            if ((!inStream) && (!mOperationDone)) {
                if (mReplyHeader.responseCode == -1) {
                    mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                }
                sendRequest(0x02);
                return true;
            } else if ((inStream) && (!mOperationDone)) {
                return false;
            } else if (mOperationDone) {
                return false;
            }
        }
        return false;
    }
    public void streamClosed(boolean inStream) throws IOException {
        if (!mGetOperation) {
            if ((!inStream) && (!mOperationDone)) {
                boolean more = true;
                if ((mPrivateOutput != null) && (mPrivateOutput.size() <= 0)) {
                    byte[] headerArray = ObexHelper.createHeader(mRequestHeader, false);
                    if (headerArray.length <= 0)
                        more = false;
                }
                if (mReplyHeader.responseCode == -1) {
                    mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                }
                while ((more) && (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE)) {
                    more = sendRequest(0x02);
                }
                while (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE) {
                    sendRequest(0x82);
                }
                mOperationDone = true;
            } else if ((inStream) && (mOperationDone)) {
                mOperationDone = true;
            }
        } else {
            if ((inStream) && (!mOperationDone)) {
                if (mReplyHeader.responseCode == -1) {
                    mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                }
                while (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE) {
                    if (!sendRequest(0x83)) {
                        break;
                    }
                }
                while (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE) {
                    mParent.sendRequest(0x83, null, mReplyHeader, mPrivateInput);
                }
                mOperationDone = true;
            } else if ((!inStream) && (!mOperationDone)) {
                boolean more = true;
                if ((mPrivateOutput != null) && (mPrivateOutput.size() <= 0)) {
                    byte[] headerArray = ObexHelper.createHeader(mRequestHeader, false);
                    if (headerArray.length <= 0)
                        more = false;
                }
                if (mPrivateInput == null) {
                    mPrivateInput = new PrivateInputStream(this);
                }
                if ((mPrivateOutput != null) && (mPrivateOutput.size() <= 0))
                    more = false;
                mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                while ((more) && (mReplyHeader.responseCode == ResponseCodes.OBEX_HTTP_CONTINUE)) {
                    more = sendRequest(0x03);
                }
                sendRequest(0x83);
                if (mReplyHeader.responseCode != ResponseCodes.OBEX_HTTP_CONTINUE) {
                    mOperationDone = true;
                }
            }
        }
    }
}
