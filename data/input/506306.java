public class SourcePositionInfo implements Comparable
{
    public SourcePositionInfo() {
        this.file = "<unknown>";
        this.line = 0;
        this.column = 0;
    }
    public SourcePositionInfo(String file, int line, int column)
    {
        this.file = file;
        this.line = line;
        this.column = column;
    }
    public SourcePositionInfo(SourcePositionInfo that)
    {
        this.file = that.file;
        this.line = that.line;
        this.column = that.column;
    }
    public static SourcePositionInfo add(SourcePositionInfo that, String str, int index)
    {
        if (that == null) {
            return null;
        }
        int line = that.line;
        char prev = 0;
        for (int i=0; i<index; i++) {
            char c = str.charAt(i);
            if (c == '\r' || (c == '\n' && prev != '\r')) {
                line++;
            }
            prev = c;
        }
        return new SourcePositionInfo(that.file, line, 0);
    }
    public static SourcePositionInfo findBeginning(SourcePositionInfo that, String str)
    {
        if (that == null) {
            return null;
        }
        int line = that.line-1; 
        int prev = 0;
        for (int i=str.length()-1; i>=0; i--) {
            char c = str.charAt(i);
            if ((c == '\r' && prev != '\n') || (c == '\n')) {
                line--;
            }
            prev = c;
        }
        return new SourcePositionInfo(that.file, line, 0);
    }
    @Override
    public String toString()
    {
        return file + ':' + line;
    }
    public int compareTo(Object o) {
        SourcePositionInfo that = (SourcePositionInfo)o;
        int r = this.file.compareTo(that.file);
        if (r != 0) return r;
        return this.line - that.line;
    }
    public String file;
    public int line;
    public int column;
}
