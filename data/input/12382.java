public class CRC16 {
    public int value;
    public CRC16() {
        value = 0;
    }
    public void update(byte aByte) {
        int a, b;
        a = (int) aByte;
        for (int count = 7; count >=0; count--) {
            a = a << 1;
            b = (a >>> 8) & 1;
            if ((value & 0x8000) != 0) {
                value = ((value << 1) + b) ^ 0x1021;
            } else {
                value = (value << 1) + b;
            }
        }
        value = value & 0xffff;
        return;
    }
    public void reset() {
        value = 0;
    }
}
