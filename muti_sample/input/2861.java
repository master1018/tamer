public class SourcePositionImpl implements SourcePosition {
    FileObject filename;
    int position;
    Position.LineMap lineMap;
    public File file() {
        return (filename == null) ? null : new File(filename.getName());
    }
    public FileObject fileObject() {
        return filename;
    }
    public int line() {
        if (lineMap == null) {
            return 0;
        } else {
            return lineMap.getLineNumber(position);
        }
    }
    public int column() {
        if (lineMap == null) {
            return 0;
        }else {
            return lineMap.getColumnNumber(position);
        }
    }
    private SourcePositionImpl(FileObject file, int position,
                               Position.LineMap lineMap) {
        super();
        this.filename = file;
        this.position = position;
        this.lineMap = lineMap;
    }
    public static SourcePosition make(FileObject file, int pos,
                                      Position.LineMap lineMap) {
        if (file == null) return null;
        return new SourcePositionImpl(file, pos, lineMap);
    }
    public String toString() {
        String fn = filename.getName();
        if (fn.endsWith(")")) {
            int paren = fn.lastIndexOf("(");
            if (paren != -1)
                fn = fn.substring(0, paren)
                        + File.separatorChar
                        + fn.substring(paren + 1, fn.length() - 1);
        }
        if (position == Position.NOPOS)
            return fn;
        else
            return fn + ":" + line();
    }
}
