public class SimTlv
{
    byte record[];
    int tlvOffset;
    int tlvLength;
    int curOffset;
    int curDataOffset;
    int curDataLength;
    boolean hasValidTlvObject;
    public SimTlv(byte[] record, int offset, int length) {
        this.record = record;
        this.tlvOffset = offset;
        this.tlvLength = length;
        curOffset = offset;
        hasValidTlvObject = parseCurrentTlvObject();
    }
    public boolean nextObject() {
        if (!hasValidTlvObject) return false;
        curOffset = curDataOffset + curDataLength;
        hasValidTlvObject = parseCurrentTlvObject();
        return hasValidTlvObject;
    }
    public boolean isValidObject() {
        return hasValidTlvObject;
    }
    public int getTag() {
        if (!hasValidTlvObject) return 0;
        return record[curOffset] & 0xff;
    }
    public byte[] getData() {
        if (!hasValidTlvObject) return null;
        byte[] ret = new byte[curDataLength];
        System.arraycopy(record, curDataOffset, ret, 0, curDataLength);
        return ret;
    }
    private boolean parseCurrentTlvObject() {
        try {
            if (record[curOffset] == 0 || (record[curOffset] & 0xff) == 0xff) {
                return false;
            }
            if ((record[curOffset + 1] & 0xff) < 0x80) {
                curDataLength = record[curOffset + 1] & 0xff;
                curDataOffset = curOffset + 2;
            } else if ((record[curOffset + 1] & 0xff) == 0x81) {
                curDataLength = record[curOffset + 2] & 0xff;
                curDataOffset = curOffset + 3;
            } else {
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        if (curDataLength + curDataOffset > tlvOffset + tlvLength) {
            return false;
        }
        return true;
    }
}
