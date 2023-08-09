public class CharGenerator {
    private IntGenerator r;
    int min = 0, max = 0xffff;
    public CharGenerator(IntGenerator r) {
        this.r = r;
    }
    public CharGenerator(IntGenerator r, int min, int max) {
        this.r = r;
        this.min = min;
        this.max = max;
    }
    public char next() {
        char c;
        do
            c = (char) (r.next(max - min) + min);
        while ((c == '\r') || (c == '\n')
               || ((c >= 0xd800) && (c <= 0xdfff))
               || (c == 0xfffe));
        return c;
    }
}
