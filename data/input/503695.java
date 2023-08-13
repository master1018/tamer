final class JdwpPacket {
    public static final int JDWP_HEADER_LEN = 11;
    public static final int HANDSHAKE_GOOD = 1;
    public static final int HANDSHAKE_NOTYET = 2;
    public static final int HANDSHAKE_BAD = 3;
    private static final int DDMS_CMD_SET = 0xc7;       
    private static final int DDMS_CMD = 0x01;
    private static final int REPLY_PACKET = 0x80;
    private static final byte[] mHandshake = {
        'J', 'D', 'W', 'P', '-', 'H', 'a', 'n', 'd', 's', 'h', 'a', 'k', 'e'
    };
    public static final int HANDSHAKE_LEN = mHandshake.length;
    private ByteBuffer mBuffer;
    private int mLength, mId, mFlags, mCmdSet, mCmd, mErrCode;
    private boolean mIsNew;
    private static int mSerialId = 0x40000000;
    JdwpPacket(ByteBuffer buf) {
        mBuffer = buf;
        mIsNew = true;
    }
    void finishPacket(int payloadLength) {
        assert mIsNew;
        ByteOrder oldOrder = mBuffer.order();
        mBuffer.order(ChunkHandler.CHUNK_ORDER);
        mLength = JDWP_HEADER_LEN + payloadLength;
        mId = getNextSerial();
        mFlags = 0;
        mCmdSet = DDMS_CMD_SET;
        mCmd = DDMS_CMD;
        mBuffer.putInt(0x00, mLength);
        mBuffer.putInt(0x04, mId);
        mBuffer.put(0x08, (byte) mFlags);
        mBuffer.put(0x09, (byte) mCmdSet);
        mBuffer.put(0x0a, (byte) mCmd);
        mBuffer.order(oldOrder);
        mBuffer.position(mLength);
    }
    private static synchronized int getNextSerial() {
        return mSerialId++;
    }
    ByteBuffer getPayload() {
        ByteBuffer buf;
        int oldPosn = mBuffer.position();
        mBuffer.position(JDWP_HEADER_LEN);
        buf = mBuffer.slice();     
        mBuffer.position(oldPosn);
        if (mLength > 0)
            buf.limit(mLength - JDWP_HEADER_LEN);
        else
            assert mIsNew;
        buf.order(ChunkHandler.CHUNK_ORDER);
        return buf;
    }
    boolean isDdmPacket() {
        return (mFlags & REPLY_PACKET) == 0 &&
               mCmdSet == DDMS_CMD_SET &&
               mCmd == DDMS_CMD;
    }
    boolean isReply() {
        return (mFlags & REPLY_PACKET) != 0;
    }
    boolean isError() {
        return isReply() && mErrCode != 0;
    }
    boolean isEmpty() {
        return (mLength == JDWP_HEADER_LEN);
    }
    int getId() {
        return mId;
    }
    int getLength() {
        return mLength;
    }
    void writeAndConsume(SocketChannel chan) throws IOException {
        int oldLimit;
        assert mLength > 0;
        mBuffer.flip();         
        oldLimit = mBuffer.limit();
        mBuffer.limit(mLength);
        while (mBuffer.position() != mBuffer.limit()) {
            chan.write(mBuffer);
        }
        assert mBuffer.position() == mLength;
        mBuffer.limit(oldLimit);
        mBuffer.compact();      
    }
    void movePacket(ByteBuffer buf) {
        Log.v("ddms", "moving " + mLength + " bytes");
        int oldPosn = mBuffer.position();
        mBuffer.position(0);
        mBuffer.limit(mLength);
        buf.put(mBuffer);
        mBuffer.position(mLength);
        mBuffer.limit(oldPosn);
        mBuffer.compact();      
    }
    void consume()
    {
        mBuffer.flip();         
        mBuffer.position(mLength);
        mBuffer.compact();      
        mLength = 0;
    }
    static JdwpPacket findPacket(ByteBuffer buf) {
        int count = buf.position();
        int length, id, flags, cmdSet, cmd;
        if (count < JDWP_HEADER_LEN)
            return null;
        ByteOrder oldOrder = buf.order();
        buf.order(ChunkHandler.CHUNK_ORDER);
        length = buf.getInt(0x00);
        id = buf.getInt(0x04);
        flags = buf.get(0x08) & 0xff;
        cmdSet = buf.get(0x09) & 0xff;
        cmd = buf.get(0x0a) & 0xff;
        buf.order(oldOrder);
        if (length < JDWP_HEADER_LEN)
            throw new BadPacketException();
        if (count < length)
            return null;
        JdwpPacket pkt = new JdwpPacket(buf);
        pkt.mLength = length;
        pkt.mId = id;
        pkt.mFlags = flags;
        if ((flags & REPLY_PACKET) == 0) {
            pkt.mCmdSet = cmdSet;
            pkt.mCmd = cmd;
            pkt.mErrCode = -1;
        } else {
            pkt.mCmdSet = -1;
            pkt.mCmd = -1;
            pkt.mErrCode = cmdSet | (cmd << 8);
        }
        return pkt;
    }
    static int findHandshake(ByteBuffer buf) {
        int count = buf.position();
        int i;
        if (count < mHandshake.length)
            return HANDSHAKE_NOTYET;
        for (i = mHandshake.length -1; i >= 0; --i) {
            if (buf.get(i) != mHandshake[i])
                return HANDSHAKE_BAD;
        }
        return HANDSHAKE_GOOD;
    }
    static void consumeHandshake(ByteBuffer buf) {
        buf.flip();         
        buf.position(mHandshake.length);
        buf.compact();      
    }
    static void putHandshake(ByteBuffer buf) {
        buf.put(mHandshake);
    }
}
