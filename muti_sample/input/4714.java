public class LocalSeqNumber implements SeqNumber {
    private int lastSeqNumber;
    public LocalSeqNumber() {
        randInit();
    }
    public LocalSeqNumber(int start) {
        init(start);
    }
    public LocalSeqNumber(Integer start) {
        init(start.intValue());
    }
    public synchronized void randInit() {
        byte[] data = Confounder.bytes(4);
        data[0] = (byte)(data[0] & 0x3f);
        int result = ((data[3] & 0xff) |
                        ((data[2] & 0xff) << 8) |
                        ((data[1] & 0xff) << 16) |
                        ((data[0] & 0xff) << 24));
        if (result == 0) {
           result = 1;
        }
        lastSeqNumber = result;
    }
    public synchronized void init(int start) {
        lastSeqNumber = start;
    }
    public synchronized int current() {
        return lastSeqNumber;
    }
    public synchronized int next() {
        return lastSeqNumber + 1;
    }
    public synchronized int step() {
        return ++lastSeqNumber;
    }
}
