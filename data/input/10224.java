public class LineGenerator {
    IntGenerator ig;
    StringGenerator sg;
    int limit;
    public LineGenerator(IntGenerator ig, StringGenerator sg, int limit) {
        this.ig = ig;
        this.sg = sg;
        this.limit = limit;
    }
    public LineGenerator(IntGenerator ig) {
        this.ig = ig;
        this.sg = new StringGenerator(ig);
        this.limit = -1;
    }
    private char prevTerminator = 0;
    private int count = 0;
    public String lineTerminator;
    public String next() {
        if ((count >= limit) && (limit >= 0))
            return null;
        String l = sg.next();
        int len = l.length();
        int t;
        do
            t = ig.next(2);
        while ((prevTerminator == '\r') && (len == 0) && (t == 0));
        String ts;
        switch (t) {
        case 0:
            ts = "\n";
            prevTerminator = '\n';
            break;
        case 1:
            ts = "\r";
            prevTerminator = '\r';
            break;
        case 2:
        default:
            ts = "\r\n";
            prevTerminator = '\n';
            break;
        }
        count++;
        lineTerminator = ts;
        return l;
    }
}
