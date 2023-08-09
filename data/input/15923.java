final class EngineInputRecord extends InputRecord {
    private SSLEngineImpl engine;
    static private ByteBuffer tmpBB = ByteBuffer.allocate(0);
    private boolean internalData;
    EngineInputRecord(SSLEngineImpl engine) {
        super();
        this.engine = engine;
    }
    byte contentType() {
        if (internalData) {
            return super.contentType();
        } else {
            return ct_application_data;
        }
    }
    int bytesInCompletePacket(ByteBuffer buf) throws SSLException {
        if (buf.remaining() < 5) {
            return -1;
        }
        int pos = buf.position();
        byte byteZero = buf.get(pos);
        int len = 0;
        if (formatVerified ||
                (byteZero == ct_handshake) ||
                (byteZero == ct_alert)) {
            ProtocolVersion recordVersion =
                ProtocolVersion.valueOf(buf.get(pos + 1), buf.get(pos + 2));
            if ((recordVersion.v < ProtocolVersion.MIN.v)
                    || (recordVersion.major > ProtocolVersion.MAX.major)) {
                throw new SSLException(
                    "Unsupported record version " + recordVersion);
            }
            formatVerified = true;
            len = ((buf.get(pos + 3) & 0xff) << 8) +
                (buf.get(pos + 4) & 0xff) + headerSize;
        } else {
            boolean isShort = ((byteZero & 0x80) != 0);
            if (isShort &&
                    ((buf.get(pos + 2) == 1) || buf.get(pos + 2) == 4)) {
                ProtocolVersion recordVersion =
                    ProtocolVersion.valueOf(buf.get(pos + 3), buf.get(pos + 4));
                if ((recordVersion.v < ProtocolVersion.MIN.v)
                        || (recordVersion.major > ProtocolVersion.MAX.major)) {
                    if (recordVersion.v != ProtocolVersion.SSL20Hello.v) {
                        throw new SSLException(
                            "Unsupported record version " + recordVersion);
                    }
                }
                int mask = (isShort ? 0x7f : 0x3f);
                len = ((byteZero & mask) << 8) + (buf.get(pos + 1) & 0xff) +
                    (isShort ? 2 : 3);
            } else {
                throw new SSLException(
                    "Unrecognized SSL message, plaintext connection?");
            }
        }
        return len;
    }
    boolean checkMAC(MAC signer, ByteBuffer bb) {
        if (internalData) {
            return checkMAC(signer);
        }
        int len = signer.MAClen();
        if (len == 0) { 
            return true;
        }
        int lim = bb.limit();
        int macData = lim - len;
        bb.limit(macData);
        byte[] mac = signer.compute(contentType(), bb);
        if (len != mac.length) {
            throw new RuntimeException("Internal MAC error");
        }
        bb.position(macData);
        bb.limit(lim);
        try {
            for (int i = 0; i < len; i++) {
                if (bb.get() != mac[i]) {  
                    return false;
                }
            }
            return true;
        } finally {
            bb.rewind();
            bb.limit(macData);
        }
    }
    ByteBuffer decrypt(CipherBox box, ByteBuffer bb)
            throws BadPaddingException {
        if (internalData) {
            decrypt(box);
            return tmpBB;
        }
        box.decrypt(bb);
        bb.rewind();
        return bb.slice();
    }
    void writeBuffer(OutputStream s, byte [] buf, int off, int len)
            throws IOException {
        ByteBuffer netBB = (ByteBuffer)
            (ByteBuffer.allocate(len).put(buf, 0, len).flip());
        engine.writer.putOutboundDataSync(netBB);
    }
    ByteBuffer read(ByteBuffer srcBB) throws IOException {
        if (!formatVerified ||
                (srcBB.get(srcBB.position()) != ct_application_data)) {
            internalData = true;
            read(new ByteBufferInputStream(srcBB), (OutputStream) null);
            return tmpBB;
        }
        internalData = false;
        int srcPos = srcBB.position();
        int srcLim = srcBB.limit();
        ProtocolVersion recordVersion = ProtocolVersion.valueOf(
                srcBB.get(srcPos + 1), srcBB.get(srcPos + 2));
        if ((recordVersion.v < ProtocolVersion.MIN.v)
                || (recordVersion.major > ProtocolVersion.MAX.major)) {
            throw new SSLException(
                "Unsupported record version " + recordVersion);
        }
        int len = bytesInCompletePacket(srcBB);
        assert(len > 0);
        if (debug != null && Debug.isOn("packet")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                srcBB.limit(srcPos + len);
                ByteBuffer bb = srcBB.duplicate();  
                System.out.println("[Raw read (bb)]: length = " + len);
                hd.encodeBuffer(bb, System.out);
            } catch (IOException e) { }
        }
        srcBB.position(srcPos + headerSize);
        srcBB.limit(srcPos + len);
        ByteBuffer bb = srcBB.slice();
        srcBB.position(srcBB.limit());
        srcBB.limit(srcLim);
        return bb;
    }
}
