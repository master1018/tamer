public final class TextRecord {
    public char[] text;
    public int start;
    public int limit;
    public int min;
    public int max;
    public void init(char[] text, int start, int limit, int min, int max) {
        this.text = text;
        this.start = start;
        this.limit = limit;
        this.min = min;
        this.max = max;
    }
}
