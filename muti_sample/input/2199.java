class OutputRecord extends ByteArrayOutputStream implements Record {
    private HandshakeHash       handshakeHash;
    private int                 lastHashed;
    private boolean             firstMessage;
    final private byte          contentType;
    ProtocolVersion     protocolVersion;
    private ProtocolVersion     helloVersion;
    static final Debug debug = Debug.getInstance("ssl");
    OutputRecord(byte type, int size) {
        super(size);
        this.protocolVersion = ProtocolVersion.DEFAULT;
        this.helloVersion = ProtocolVersion.DEFAULT_HELLO;
        firstMessage = true;
        count = headerSize;
        contentType = type;
        lastHashed = count;
    }
    OutputRecord(byte type) {
        this(type, recordSize(type));
    }
    private static int recordSize(byte type) {
        if ((type == ct_change_cipher_spec) || (type == ct_alert)) {
            return maxAlertRecordSize;
        } else {
            return maxRecordSize;
        }
    }
    synchronized void setVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    synchronized void setHelloVersion(ProtocolVersion helloVersion) {
        this.helloVersion = helloVersion;
    }
    public synchronized void reset() {
        super.reset();
        count = headerSize;
        lastHashed = count;
    }
    void setHandshakeHash(HandshakeHash handshakeHash) {
        assert(contentType == ct_handshake);
        this.handshakeHash = handshakeHash;
    }
    void doHashes() {
        int len = count - lastHashed;
        if (len > 0) {
            hashInternal(buf, lastHashed, len);
            lastHashed = count;
        }
    }
    private void hashInternal(byte buf [], int offset, int len) {
        if (debug != null && Debug.isOn("data")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                System.out.println("[write] MD5 and SHA1 hashes:  len = "
                    + len);
                hd.encodeBuffer(new ByteArrayInputStream(buf,
                    lastHashed, len), System.out);
            } catch (IOException e) { }
        }
        handshakeHash.update(buf, lastHashed, len);
        lastHashed = count;
    }
    boolean isEmpty() {
        return count == headerSize;
    }
    boolean isAlert(byte description) {
        if (count > (headerSize + 1) && contentType == ct_alert) {
            return buf[headerSize + 1] == description;
        }
        return false;
    }
    void addMAC(MAC signer) throws IOException {
        if (contentType == ct_handshake) {
            doHashes();
        }
        if (signer.MAClen() != 0) {
            byte[] hash = signer.compute(contentType, buf,
                    headerSize, count - headerSize);
            write(hash);
        }
    }
    void encrypt(CipherBox box) {
        int len = count - headerSize;
        count = headerSize + box.encrypt(buf, headerSize, len);
    }
    final int availableDataBytes() {
        int dataSize = count - headerSize;
        return maxDataSize - dataSize;
    }
    final byte contentType() {
        return contentType;
    }
    void write(OutputStream s) throws IOException {
        if (count == headerSize) {
            return;
        }
        int length = count - headerSize;
        if (length < 0) {
            throw new SSLException("output record size too small: "
                + length);
        }
        if (debug != null
                && (Debug.isOn("record") || Debug.isOn("handshake"))) {
            if ((debug != null && Debug.isOn("record"))
                    || contentType() == ct_change_cipher_spec)
                System.out.println(Thread.currentThread().getName()
                    + ", WRITE: " + protocolVersion
                    + " " + InputRecord.contentName(contentType())
                    + ", length = " + length);
        }
         if (firstMessage && useV2Hello()) {
            byte[] v3Msg = new byte[length - 4];
            System.arraycopy(buf, headerSize + 4, v3Msg, 0, v3Msg.length);
            V3toV2ClientHello(v3Msg);
            handshakeHash.reset();
            lastHashed = 2;
            doHashes();
            if (debug != null && Debug.isOn("record"))  {
                System.out.println(
                    Thread.currentThread().getName()
                    + ", WRITE: SSLv2 client hello message"
                    + ", length = " + (count - 2)); 
            }
        } else {
            buf[0] = contentType;
            buf[1] = protocolVersion.major;
            buf[2] = protocolVersion.minor;
            buf[3] = (byte)(length >> 8);
            buf[4] = (byte)(length);
        }
        firstMessage = false;
        writeBuffer(s, buf, 0, count);
        reset();
    }
    void writeBuffer(OutputStream s, byte [] buf, int off, int len)
            throws IOException {
        s.write(buf, off, len);
        s.flush();
        if (debug != null && Debug.isOn("packet")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
                System.out.println("[Raw write]: length = " +
                    bb.remaining());
                hd.encodeBuffer(bb, System.out);
            } catch (IOException e) { }
        }
    }
    private boolean useV2Hello() {
        return firstMessage
            && (helloVersion == ProtocolVersion.SSL20Hello)
            && (contentType == ct_handshake)
            && (buf[5] == HandshakeMessage.ht_client_hello)
            && (buf[headerSize + 4+2+32] == 0); 
    }
    private void V3toV2ClientHello(byte v3Msg []) throws SSLException {
        int v3SessionIdLenOffset = 2 + 32; 
        int v3SessionIdLen = v3Msg[v3SessionIdLenOffset];
        int v3CipherSpecLenOffset = v3SessionIdLenOffset + 1 + v3SessionIdLen;
        int v3CipherSpecLen = ((v3Msg[v3CipherSpecLenOffset] & 0xff) << 8) +
          (v3Msg[v3CipherSpecLenOffset + 1] & 0xff);
        int cipherSpecs = v3CipherSpecLen / 2; 
        int v3CipherSpecOffset = v3CipherSpecLenOffset + 2; 
        int v2CipherSpecLen = 0;
        count = 11;
        boolean containsRenegoInfoSCSV = false;
        for (int i = 0; i < cipherSpecs; i++) {
            byte byte1, byte2;
            byte1 = v3Msg[v3CipherSpecOffset++];
            byte2 = v3Msg[v3CipherSpecOffset++];
            v2CipherSpecLen += V3toV2CipherSuite(byte1, byte2);
            if (!containsRenegoInfoSCSV &&
                        byte1 == (byte)0x00 && byte2 == (byte)0xFF) {
                containsRenegoInfoSCSV = true;
            }
        }
        if (!containsRenegoInfoSCSV) {
            v2CipherSpecLen += V3toV2CipherSuite((byte)0x00, (byte)0xFF);
        }
        buf[2] = HandshakeMessage.ht_client_hello;
        buf[3] = v3Msg[0];      
        buf[4] = v3Msg[1];      
        buf[5] = (byte)(v2CipherSpecLen >>> 8);
        buf[6] = (byte)v2CipherSpecLen;
        buf[7] = 0;
        buf[8] = 0;             
        buf[9] = 0;
        buf[10] = 32;           
        System.arraycopy(v3Msg, 2, buf, count, 32);
        count += 32;
        count -= 2; 
        buf[0] = (byte)(count >>> 8);
        buf[0] |= 0x80;
        buf[1] = (byte)(count);
        count += 2;
    }
    private static int[] V3toV2CipherMap1 =
        {-1, -1, -1, 0x02, 0x01, -1, 0x04, 0x05, -1, 0x06, 0x07};
    private static int[] V3toV2CipherMap3 =
        {-1, -1, -1, 0x80, 0x80, -1, 0x80, 0x80, -1, 0x40, 0xC0};
    private int V3toV2CipherSuite(byte byte1, byte byte2) {
        buf[count++] = 0;
        buf[count++] = byte1;
        buf[count++] = byte2;
        if (((byte2 & 0xff) > 0xA) ||
                (V3toV2CipherMap1[byte2] == -1)) {
            return 3;
        }
        buf[count++] = (byte)V3toV2CipherMap1[byte2];
        buf[count++] = 0;
        buf[count++] = (byte)V3toV2CipherMap3[byte2];
        return 6;
    }
}
