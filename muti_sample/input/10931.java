final class EngineWriter {
    private LinkedList<Object> outboundList;
    private boolean outboundClosed = false;
    private static final Debug debug = Debug.getInstance("ssl");
    EngineWriter() {
        outboundList = new LinkedList<Object>();
    }
    private HandshakeStatus getOutboundData(ByteBuffer dstBB) {
        Object msg = outboundList.removeFirst();
        assert(msg instanceof ByteBuffer);
        ByteBuffer bbIn = (ByteBuffer) msg;
        assert(dstBB.remaining() >= bbIn.remaining());
        dstBB.put(bbIn);
        if (hasOutboundDataInternal()) {
            msg = outboundList.getFirst();
            if (msg == HandshakeStatus.FINISHED) {
                outboundList.removeFirst();     
                return HandshakeStatus.FINISHED;
            } else {
                return HandshakeStatus.NEED_WRAP;
            }
        } else {
            return null;
        }
    }
    synchronized void writeRecord(EngineOutputRecord outputRecord,
            MAC writeMAC, CipherBox writeCipher) throws IOException {
        if (outboundClosed) {
            throw new IOException("writer side was already closed.");
        }
        outputRecord.write(writeMAC, writeCipher);
        if (outputRecord.isFinishedMsg()) {
            outboundList.addLast(HandshakeStatus.FINISHED);
        }
    }
    private void dumpPacket(EngineArgs ea, boolean hsData) {
        try {
            HexDumpEncoder hd = new HexDumpEncoder();
            ByteBuffer bb = ea.netData.duplicate();
            int pos = bb.position();
            bb.position(pos - ea.deltaNet());
            bb.limit(pos);
            System.out.println("[Raw write" +
                (hsData ? "" : " (bb)") + "]: length = " +
                bb.remaining());
            hd.encodeBuffer(bb, System.out);
        } catch (IOException e) { }
    }
    synchronized HandshakeStatus writeRecord(
            EngineOutputRecord outputRecord, EngineArgs ea, MAC writeMAC,
            CipherBox writeCipher) throws IOException {
        if (hasOutboundDataInternal()) {
            HandshakeStatus hss = getOutboundData(ea.netData);
            if (debug != null && Debug.isOn("packet")) {
                dumpPacket(ea, true);
            }
            return hss;
        }
        if (outboundClosed) {
            throw new IOException("The write side was already closed");
        }
        outputRecord.write(ea, writeMAC, writeCipher);
        if (debug != null && Debug.isOn("packet")) {
            dumpPacket(ea, false);
        }
        return null;
    }
    void putOutboundData(ByteBuffer bytes) {
        outboundList.addLast(bytes);
    }
    synchronized void putOutboundDataSync(ByteBuffer bytes)
            throws IOException {
        if (outboundClosed) {
            throw new IOException("Write side already closed");
        }
        outboundList.addLast(bytes);
    }
    private boolean hasOutboundDataInternal() {
        return (outboundList.size() != 0);
    }
    synchronized boolean hasOutboundData() {
        return hasOutboundDataInternal();
    }
    synchronized boolean isOutboundDone() {
        return outboundClosed && !hasOutboundDataInternal();
    }
    synchronized void closeOutbound() {
        outboundClosed = true;
    }
}
