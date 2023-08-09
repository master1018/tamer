class InputRecord extends ByteArrayInputStream implements Record {
    private HandshakeHash       handshakeHash;
    private int                 lastHashed;
    boolean                     formatVerified = true;  
    private boolean             isClosed;
    private boolean             appDataValid;
    private ProtocolVersion     helloVersion;
    static final Debug debug = Debug.getInstance("ssl");
    private int exlen;
    private byte v2Buf[];
    InputRecord() {
        super(new byte[maxRecordSize]);
        setHelloVersion(ProtocolVersion.DEFAULT_HELLO);
        pos = headerSize;
        count = headerSize;
        lastHashed = count;
        exlen = 0;
        v2Buf = null;
    }
    void setHelloVersion(ProtocolVersion helloVersion) {
        this.helloVersion = helloVersion;
    }
    ProtocolVersion getHelloVersion() {
        return helloVersion;
    }
    void enableFormatChecks() {
        formatVerified = false;
    }
    boolean isAppDataValid() {
        return appDataValid;
    }
    void setAppDataValid(boolean value) {
        appDataValid = value;
    }
    byte contentType() {
        return buf[0];
    }
    void setHandshakeHash(HandshakeHash handshakeHash) {
        this.handshakeHash = handshakeHash;
    }
    HandshakeHash getHandshakeHash() {
        return handshakeHash;
    }
    boolean checkMAC(MAC signer) {
        int len = signer.MAClen();
        if (len == 0) { 
            return true;
        }
        int offset = count - len;
        if (offset < headerSize) {
            return false;
        }
        byte[] mac = signer.compute(contentType(), buf,
            headerSize, offset - headerSize);
        if (len != mac.length) {
            throw new RuntimeException("Internal MAC error");
        }
        for (int i = 0; i < len; i++) {
            if (buf[offset + i] != mac[i]) {
                return false;
            }
        }
        count -= len;
        return true;
    }
    void decrypt(CipherBox box) throws BadPaddingException {
        int len = count - headerSize;
        count = headerSize + box.decrypt(buf, headerSize, len);
    }
    void ignore(int bytes) {
        if (bytes > 0) {
            pos += bytes;
            lastHashed = pos;
        }
    }
    void doHashes() {
        int len = pos - lastHashed;
        if (len > 0) {
            hashInternal(buf, lastHashed, len);
            lastHashed = pos;
        }
    }
    private void hashInternal(byte databuf [], int offset, int len) {
        if (debug != null && Debug.isOn("data")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                System.out.println("[read] MD5 and SHA1 hashes:  len = "
                    + len);
                hd.encodeBuffer(new ByteArrayInputStream(databuf, offset, len),
                    System.out);
            } catch (IOException e) { }
        }
        handshakeHash.update(databuf, offset, len);
    }
    void queueHandshake(InputRecord r) throws IOException {
        int len;
        doHashes();
        if (pos > headerSize) {
            len = count - pos;
            if (len != 0) {
                System.arraycopy(buf, pos, buf, headerSize, len);
            }
            pos = headerSize;
            lastHashed = pos;
            count = headerSize + len;
        }
        len = r.available() + count;
        if (buf.length < len) {
            byte        newbuf [];
            newbuf = new byte [len];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(r.buf, r.pos, buf, count, len - count);
        count = len;
        len = r.lastHashed - r.pos;
        if (pos == headerSize) {
            lastHashed += len;
        } else {
            throw new SSLProtocolException("?? confused buffer hashing ??");
        }
        r.pos = r.count;
    }
    public void close() {
        appDataValid = false;
        isClosed = true;
        mark = 0;
        pos = 0;
        count = 0;
    }
    private static final byte[] v2NoCipher = {
        (byte)0x80, (byte)0x03, 
        (byte)0x00,             
        (byte)0x00, (byte)0x01  
    };
    private int readFully(InputStream s, byte b[], int off, int len)
            throws IOException {
        int n = 0;
        while (n < len) {
            int readLen = s.read(b, off + n, len - n);
            if (readLen < 0) {
                return readLen;
            }
            if (debug != null && Debug.isOn("packet")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    ByteBuffer bb = ByteBuffer.wrap(b, off + n, readLen);
                    System.out.println("[Raw read]: length = " +
                        bb.remaining());
                    hd.encodeBuffer(bb, System.out);
                } catch (IOException e) { }
            }
            n += readLen;
            exlen += readLen;
        }
        return n;
    }
    void read(InputStream s, OutputStream o) throws IOException {
        if (isClosed) {
            return;
        }
        if(exlen < headerSize) {
            int really = readFully(s, buf, exlen, headerSize - exlen);
            if (really < 0) {
                throw new EOFException("SSL peer shut down incorrectly");
            }
            pos = headerSize;
            count = headerSize;
            lastHashed = pos;
        }
        if (!formatVerified) {
            formatVerified = true;
            if (buf[0] != ct_handshake && buf[0] != ct_alert) {
                handleUnknownRecord(s, o);
            } else {
                readV3Record(s, o);
            }
        } else { 
            readV3Record(s, o);
        }
    }
    private void readV3Record(InputStream s, OutputStream o)
            throws IOException {
        ProtocolVersion recordVersion = ProtocolVersion.valueOf(buf[1], buf[2]);
        if ((recordVersion.v < ProtocolVersion.MIN.v)
                || (recordVersion.major > ProtocolVersion.MAX.major)) {
            throw new SSLException(
                "Unsupported record version " + recordVersion);
        }
        int contentLen = ((buf[3] & 0x0ff) << 8) + (buf[4] & 0xff);
        if (contentLen < 0 || contentLen > maxLargeRecordSize - headerSize) {
            throw new SSLProtocolException("Bad InputRecord size"
                + ", count = " + contentLen
                + ", buf.length = " + buf.length);
        }
        if (contentLen > buf.length - headerSize) {
            byte[] newbuf = new byte[contentLen + headerSize];
            System.arraycopy(buf, 0, newbuf, 0, headerSize);
            buf = newbuf;
        }
        if (exlen < contentLen + headerSize) {
            int really = readFully(
                s, buf, exlen, contentLen + headerSize - exlen);
            if (really < 0) {
                throw new SSLException("SSL peer shut down incorrectly");
            }
        }
        count = contentLen + headerSize;
        exlen = 0;
        if (debug != null && Debug.isOn("record")) {
            if (count < 0 || count > (maxRecordSize - headerSize)) {
                System.out.println(Thread.currentThread().getName()
                    + ", Bad InputRecord size" + ", count = " + count);
            }
            System.out.println(Thread.currentThread().getName()
                + ", READ: " + recordVersion + " "
                + contentName(contentType()) + ", length = " + available());
        }
    }
    private void handleUnknownRecord(InputStream s, OutputStream o)
            throws IOException {
        if (((buf[0] & 0x080) != 0) && buf[2] == 1) {
            if (helloVersion != ProtocolVersion.SSL20Hello) {
                throw new SSLHandshakeException("SSLv2Hello is disabled");
            }
            ProtocolVersion recordVersion =
                                ProtocolVersion.valueOf(buf[3], buf[4]);
            if (recordVersion == ProtocolVersion.SSL20Hello) {
                try {
                    writeBuffer(o, v2NoCipher, 0, v2NoCipher.length);
                } catch (Exception e) {
                }
                throw new SSLException("Unsupported SSL v2.0 ClientHello");
            }
            int len = ((buf[0] & 0x7f) << 8) +
                (buf[1] & 0xff) - 3;
            if (v2Buf == null) {
                v2Buf = new byte[len];
            }
            if (exlen < len + headerSize) {
                int really = readFully(
                        s, v2Buf, exlen - headerSize, len + headerSize - exlen);
                if (really < 0) {
                    throw new EOFException("SSL peer shut down incorrectly");
                }
            }
            exlen = 0;
            hashInternal(buf, 2, 3);
            hashInternal(v2Buf, 0, len);
            V2toV3ClientHello(v2Buf);
            v2Buf = null;
            lastHashed = count;
            if (debug != null && Debug.isOn("record"))  {
                System.out.println(
                    Thread.currentThread().getName()
                    + ", READ:  SSL v2, contentType = "
                    + contentName(contentType())
                    + ", translated length = " + available());
            }
            return;
        } else {
            if (((buf [0] & 0x080) != 0) && buf [2] == 4) {
                throw new SSLException(
                    "SSL V2.0 servers are not supported.");
            }
            for (int i = 0; i < v2NoCipher.length; i++) {
                if (buf[i] != v2NoCipher[i]) {
                    throw new SSLException(
                        "Unrecognized SSL message, plaintext connection?");
                }
            }
            throw new SSLException("SSL V2.0 servers are not supported.");
        }
    }
    void writeBuffer(OutputStream s, byte [] buf, int off, int len)
            throws IOException {
        s.write(buf, 0, len);
        s.flush();
    }
    private void V2toV3ClientHello(byte v2Msg []) throws SSLException
    {
        int i;
        buf [0] = ct_handshake;
        buf [1] = buf [3];      
        buf[2] = buf[4];
        buf [5] = 1;    
        buf [9] = buf [1];
        buf [10] = buf [2];
        count = 11;
        int      cipherSpecLen, sessionIdLen, nonceLen;
        cipherSpecLen = ((v2Msg [0] & 0xff) << 8) + (v2Msg [1] & 0xff);
        sessionIdLen  = ((v2Msg [2] & 0xff) << 8) + (v2Msg [3] & 0xff);
        nonceLen   = ((v2Msg [4] & 0xff) << 8) + (v2Msg [5] & 0xff);
        int      offset = 6 + cipherSpecLen + sessionIdLen;
        if (nonceLen < 32) {
            for (i = 0; i < (32 - nonceLen); i++)
                buf [count++] = 0;
            System.arraycopy(v2Msg, offset, buf, count, nonceLen);
            count += nonceLen;
        } else {
            System.arraycopy(v2Msg, offset + (nonceLen - 32),
                    buf, count, 32);
            count += 32;
        }
        offset -= sessionIdLen;
        buf [count++] = (byte) sessionIdLen;
        System.arraycopy(v2Msg, offset, buf, count, sessionIdLen);
        count += sessionIdLen;
        int j;
        offset -= cipherSpecLen;
        j = count + 2;
        for (i = 0; i < cipherSpecLen; i += 3) {
            if (v2Msg [offset + i] != 0)
                continue;
            buf [j++] = v2Msg [offset + i + 1];
            buf [j++] = v2Msg [offset + i + 2];
        }
        j -= count + 2;
        buf [count++] = (byte) (j >>> 8);
        buf [count++] = (byte) j;
        count += j;
        buf [count++] = 1;
        buf [count++] = 0;      
        buf [3] = (byte) (count - headerSize);
        buf [4] = (byte) ((count - headerSize) >>> 8);
        buf [headerSize + 1] = 0;
        buf [headerSize + 2] = (byte) (((count - headerSize) - 4) >>> 8);
        buf [headerSize + 3] = (byte) ((count - headerSize) - 4);
        pos = headerSize;
    }
    static String contentName(int contentType) {
        switch (contentType) {
        case ct_change_cipher_spec:
            return "Change Cipher Spec";
        case ct_alert:
            return "Alert";
        case ct_handshake:
            return "Handshake";
        case ct_application_data:
            return "Application Data";
        default:
            return "contentType = " + contentType;
        }
    }
}
