public class ReplaceableString implements Replaceable {
    private StringBuffer buf;
    public ReplaceableString(String str) {
        buf = new StringBuffer(str);
    }
    public ReplaceableString(StringBuffer buf) {
        this.buf = buf;
    }
    public int length() {
        return buf.length();
    }
    public char charAt(int offset) {
        return buf.charAt(offset);
    }
    public void getChars(int srcStart, int srcLimit, char dst[], int dstStart) {
        Utility.getChars(buf, srcStart, srcLimit, dst, dstStart);
    }
}
