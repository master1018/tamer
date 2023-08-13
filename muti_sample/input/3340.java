class JavacSourcePosition {
    final JavaFileObject sourcefile;
    final int pos;
    final Position.LineMap lineMap;
    JavacSourcePosition(JavaFileObject sourcefile,
                        int pos,
                        Position.LineMap lineMap) {
        this.sourcefile = sourcefile;
        this.pos = pos;
        this.lineMap = (pos != Position.NOPOS) ? lineMap : null;
    }
    public JavaFileObject getFile() {
        return sourcefile;
    }
    public int getOffset() {
        return pos;     
    }
    public int getLine() {
        return (lineMap != null) ? lineMap.getLineNumber(pos) : -1;
    }
    public int getColumn() {
        return (lineMap != null) ? lineMap.getColumnNumber(pos) : -1;
    }
    public String toString() {
        int line = getLine();
        return (line > 0)
              ? sourcefile + ":" + line
              : sourcefile.toString();
    }
}
