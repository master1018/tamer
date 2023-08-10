public abstract class BinaryDataSource {
    abstract byte getByte(int offset);
    abstract int getWord(int offset);
    public final String getString0(int address) {
        return getTerminatedString(address, StringTerminator.TERMINATE_0);
    }
    final String getTerminatedString(int address, StringTerminator terminator) {
        return getTerminatedString(address, terminator, Integer.MAX_VALUE);
    }
    abstract String getTerminatedString(int address, StringTerminator stringTerminator, int maxLength);
    public final String getString0_10_13(int address) {
        return getTerminatedString(address, StringTerminator.TERMINATE_0_10_13);
    }
    public final String getStringControlTerminated(int offset) {
        return getTerminatedString(offset, StringTerminator.TERMINATE_CONTROL);
    }
    public final String getStringControlTerminated(int offset, int length) {
        return getTerminatedString(offset, StringTerminator.TERMINATE_CONTROL, length);
    }
    abstract Rectangle getRectangle(int offset);
    abstract Point getPoint(int offset);
    public final String getStringN(int offset, int maxLength) {
        return getTerminatedString(offset, StringTerminator.TERMINATE_0, maxLength);
    }
    public abstract ByteArray getByteArray(int address, int count, boolean create);
    public abstract byte[] getBytes(int address, int count);
    public abstract long get5ByteValue(int address);
    public abstract int[] getWords(int address, int count);
}
