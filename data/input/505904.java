class Socks4Message {
    static final int COMMAND_CONNECT = 1;
    static final int COMMAND_BIND = 2;
    static final int RETURN_SUCCESS = 90;
    static final int RETURN_FAILURE = 91;
    static final int RETURN_CANNOT_CONNECT_TO_IDENTD = 92;
    static final int RETURN_DIFFERENT_USER_IDS = 93;
    static final int REPLY_LENGTH = 8;
    static final int INDEX_VERSION = 0;
    private static final int SOCKS_VERSION = 4;
    private static final int INDEX_COMMAND = 1;
    private static final int INDEX_PORT = 2;
    private static final int INDEX_IP = 4;
    private static final int INDEX_USER_ID = 8;
    private static final int BUFFER_LENGTH = 256;
    private static final int MAX_USER_ID_LENGTH = BUFFER_LENGTH - INDEX_USER_ID;
    protected byte[] buffer;
    public Socks4Message() {
        super();
        buffer = new byte[BUFFER_LENGTH];
        setVersionNumber(SOCKS_VERSION);
    }
    public int getCommandOrResult() {
        return buffer[INDEX_COMMAND];
    }
    public void setCommandOrResult(int command) {
        buffer[INDEX_COMMAND] = (byte) command;
    }
    public int getPort() {
        return getInt16(INDEX_PORT);
    }
    public void setPort(int port) {
        setInt16(INDEX_PORT, port);
    }
    public int getIP() {
        return getInt32(INDEX_IP);
    }
    public void setIP(byte[] ip) {
        buffer[INDEX_IP] = ip[0];
        buffer[INDEX_IP + 1] = ip[1];
        buffer[INDEX_IP + 2] = ip[2];
        buffer[INDEX_IP + 3] = ip[3];
    }
    public String getUserId() {
        return getString(INDEX_USER_ID, MAX_USER_ID_LENGTH);
    }
    public void setUserId(String id) {
        setString(INDEX_USER_ID, MAX_USER_ID_LENGTH, id);
    }
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(50);
        buf.append("Version: ");
        buf.append(Integer.toHexString(getVersionNumber()));
        buf.append(" Command: ");
        buf.append(Integer.toHexString(getCommandOrResult()));
        buf.append(" Port: ");
        buf.append(getPort());
        buf.append(" IP: ");
        buf.append(Integer.toHexString(getIP()));
        buf.append(" User ID: ");
        buf.append(getUserId());
        return buf.toString();
    }
    public int getLength() {
        int index = 0;
        for (index = INDEX_USER_ID; buffer[index] != 0; index++) {
        }
        index++;
        return index;
    }
    public String getErrorString(int error) {
        switch (error) {
            case RETURN_FAILURE:
                return Msg.getString("K00cd"); 
            case RETURN_CANNOT_CONNECT_TO_IDENTD:
                return Msg.getString("K00ce"); 
            case RETURN_DIFFERENT_USER_IDS:
                return Msg.getString("K00cf"); 
            default:
                return Msg.getString("K00d0"); 
        }
    }
    public byte[] getBytes() {
        return buffer;
    }
    private int getInt16(int offset) {
        return (((buffer[offset] & 0xFF) << 8) + (buffer[offset + 1] & 0xFF));
    }
    private int getInt32(int offset) {
        return ((buffer[offset + 3] & 0xFF)
                + ((buffer[offset + 2] & 0xFF) << 8)
                + ((buffer[offset + 1] & 0xFF) << 16) + ((buffer[offset + 0] & 0xFF) << 24));
    }
    private String getString(int offset, int maxLength) {
        int index = offset;
        int lastIndex = index + maxLength;
        String result;
        while (index < lastIndex && (buffer[index] != 0)) {
            index++;
        }
        try {
            result = new String(buffer, offset, index - offset, "ISO8859_1"); 
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
        return result;
    }
    private int getVersionNumber() {
        return buffer[INDEX_VERSION];
    }
    private void setInt16(int offset, int value) {
        buffer[offset] = (byte) (value >>> 8 & 0xFF);
        buffer[offset + 1] = (byte) (value & 0xFF);
    }
    private void setString(int offset, int maxLength, String theString) {
        byte[] stringBytes;
        try {
            stringBytes = theString.getBytes("ISO8859_1"); 
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
        int length = Math.min(stringBytes.length, maxLength);
        System.arraycopy(stringBytes, 0, buffer, offset, length);
        buffer[offset + length] = 0;
    }
    private void setVersionNumber(int number) {
        buffer[INDEX_VERSION] = (byte) number;
    }
}
