public abstract class BaseDumper
        implements ParseObserver {
    private final byte[] bytes;
    private final boolean rawBytes;
    private final PrintStream out;
    private final int width;
    private final String filePath;
    private final boolean strictParse;
    private final int hexCols;
    private int indent;
    private String separator;
    private int at;
    protected Args args;
    public BaseDumper(byte[] bytes, PrintStream out,
                      String filePath, Args args) {
        this.bytes = bytes;
        this.rawBytes = args.rawBytes;
        this.out = out;
        this.width = (args.width <= 0) ? 79 : args.width;
        this.filePath = filePath;
        this.strictParse = args.strictParse;
        this.indent = 0;
        this.separator = rawBytes ? "|" : "";
        this.at = 0;
        this.args = args;
        int hexCols = (((width - 5) / 15) + 1) & ~1;
        if (hexCols < 6) {
            hexCols = 6;
        } else if (hexCols > 10) {
            hexCols = 10;
        }
        this.hexCols = hexCols;
    }
    static int computeParamWidth(ConcreteMethod meth, boolean isStatic) {
        return meth.getEffectiveDescriptor().getParameterTypes().
            getWordCount();
    }
    public void changeIndent(int indentDelta) {
        indent += indentDelta;
        separator = rawBytes ? "|" : "";
        for (int i = 0; i < indent; i++) {
            separator += "  ";
        }
    }
    public void parsed(ByteArray bytes, int offset, int len, String human) {
        offset = bytes.underlyingOffset(offset, getBytes());
        boolean rawBytes = getRawBytes();
        if (offset < at) {
            println("<dump skipped backwards to " + Hex.u4(offset) + ">");
            at = offset;
        } else if (offset > at) {
            String hex = rawBytes ? hexDump(at, offset - at) : "";
            print(twoColumns(hex, "<skipped to " + Hex.u4(offset) + ">"));
            at = offset;
        }
        String hex = rawBytes ? hexDump(offset, len) : "";
        print(twoColumns(hex, human));
        at += len;
    }
    public void startParsingMember(ByteArray bytes, int offset, String name,
                                   String descriptor) {
    }
    public void endParsingMember(ByteArray bytes, int offset, String name,
                                 String descriptor, Member member) {
    }
    protected final int getAt() {
        return at;
    }
    protected final void setAt(ByteArray arr, int offset) {
        at = arr.underlyingOffset(offset, bytes);
    }
    protected final byte[] getBytes() {
        return bytes;
    }
    protected final String getFilePath() {
        return filePath;
    }
    protected final boolean getStrictParse() {
        return strictParse;
    }
    protected final void print(String s) {
        out.print(s);
    }
    protected final void println(String s) {
        out.println(s);
    }
    protected final boolean getRawBytes() {
        return rawBytes;
    }
    protected final int getWidth1() {
        if (rawBytes) {
            return 5 + (hexCols * 2) + (hexCols / 2);
        }
        return 0;
    }
    protected final int getWidth2() {
        int w1 = rawBytes ? (getWidth1() + 1) : 0;
        return width - w1 - (indent * 2);
    }
    protected final String hexDump(int offset, int len) {
        return Hex.dump(bytes, offset, len, offset, hexCols, 4);
    }
    protected final String twoColumns(String s1, String s2) {
        int w1 = getWidth1();
        int w2 = getWidth2();
        try {
            if (w1 == 0) {
                int len2 = s2.length();
                StringWriter sw = new StringWriter(len2 * 2);
                IndentingWriter iw = new IndentingWriter(sw, w2, separator);
                iw.write(s2);
                if ((len2 == 0) || (s2.charAt(len2 - 1) != '\n')) {
                    iw.write('\n');
                }
                iw.flush();
                return sw.toString();
            } else {
                return TwoColumnOutput.toString(s1, w1, separator, s2, w2);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
