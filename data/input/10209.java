public class IndentingWriter extends BufferedWriter {
    private final int indentStep;
    private final int tabSize;
    private boolean beginningOfLine = true;
    private int currentIndent = 0;
    public IndentingWriter(Writer out) {
        this(out, 4);
    }
    public IndentingWriter(Writer out, int indentStep) {
        this(out, indentStep, 8);
    }
    public IndentingWriter(Writer out, int indentStep, int tabSize) {
        super(out);
        if (indentStep < 0) {
            throw new IllegalArgumentException("negative indent step");
        }
        if (tabSize < 0) {
            throw new IllegalArgumentException("negative tab size");
        }
        this.indentStep = indentStep;
        this.tabSize = tabSize;
    }
    public void write(int c) throws IOException {
        checkWrite();
        super.write(c);
    }
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len > 0) {
            checkWrite();
        }
        super.write(cbuf, off, len);
    }
    public void write(String s, int off, int len) throws IOException {
        if (len > 0) {
            checkWrite();
        }
        super.write(s, off, len);
    }
    public void newLine() throws IOException {
        super.newLine();
        beginningOfLine = true;
    }
    protected void checkWrite() throws IOException {
        if (beginningOfLine) {
            beginningOfLine = false;
            int i = currentIndent;
            while (i >= tabSize) {
                super.write('\t');
                i -= tabSize;
            }
            while (i > 0) {
                super.write(' ');
                i--;
            }
        }
    }
    protected void indentIn() {
        currentIndent += indentStep;
    }
    protected void indentOut() {
        currentIndent -= indentStep;
        if (currentIndent < 0)
            currentIndent = 0;
    }
    public void pI() {
        indentIn();
    }
    public void pO() {
        indentOut();
    }
    public void p(String s) throws IOException {
        write(s);
    }
    public void pln() throws IOException {
        newLine();
    }
    public void pln(String s) throws IOException {
        p(s);
        pln();
    }
    public void plnI(String s) throws IOException {
        p(s);
        pln();
        pI();
    }
    public void pO(String s) throws IOException {
        pO();
        p(s);
    }
    public void pOln(String s) throws IOException {
        pO(s);
        pln();
    }
    public void pOlnI(String s) throws IOException {
        pO(s);
        pln();
        pI();
    }
    public void p(Object o) throws IOException {
        write(o.toString());
    }
    public void pln(Object o) throws IOException {
        p(o.toString());
        pln();
    }
    public void plnI(Object o) throws IOException {
        p(o.toString());
        pln();
        pI();
    }
    public void pO(Object o) throws IOException {
        pO();
        p(o.toString());
    }
    public void pOln(Object o) throws IOException {
        pO(o.toString());
        pln();
    }
    public void pOlnI(Object o) throws IOException {
        pO(o.toString());
        pln();
        pI();
    }
}
