public class StringGenerator {
    private IntGenerator ig;
    private CharGenerator cg;
    private int limit;
    private int count = 0;
    public StringGenerator(IntGenerator ig, CharGenerator cg, int limit) {
        this.ig = ig;
        this.cg = cg;
        this.limit = limit;
    }
    public StringGenerator(IntGenerator ig, CharGenerator cg) {
        this(ig, cg, -1);
    }
    public StringGenerator(IntGenerator ig, int limit) {
        this(ig, new CharGenerator(ig), limit);
    }
    public StringGenerator(IntGenerator ig) {
        this(ig, -1);
    }
    public StringGenerator(int limit) {
        this(new IntGenerator(), limit);
    }
    public StringGenerator() {
        this(new IntGenerator());
    }
    public String next() {
        if ((count >= limit) && (limit >= 0))
            return null;
        int len = ig.next(80);
        StringBuffer s = new StringBuffer(len);
        for (int i = 0; i < len; i++)
            s.append(cg.next());
        count++;
        return s.toString();
    }
}
