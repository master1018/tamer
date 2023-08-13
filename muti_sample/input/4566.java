public class ScriptException extends Exception {
    private String fileName;
    private int lineNumber;
    private int columnNumber;
    public ScriptException(String s) {
        super(s);
        fileName = null;
        lineNumber = -1;
        columnNumber = -1;
    }
    public ScriptException(Exception e) {
        super(e);
        fileName = null;
        lineNumber = -1;
        columnNumber = -1;
    }
    public ScriptException(String message, String fileName, int lineNumber) {
        super(message);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.columnNumber = -1;
    }
    public ScriptException(String message,
            String fileName,
            int lineNumber,
            int columnNumber) {
        super(message);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public String getMessage() {
        String ret = super.getMessage();
        if (fileName != null) {
            ret += (" in " + fileName);
            if (lineNumber != -1) {
                ret += " at line number " + lineNumber;
            }
            if (columnNumber != -1) {
                ret += " at column number " + columnNumber;
            }
        }
        return ret;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public int getColumnNumber() {
        return columnNumber;
    }
    public String getFileName() {
        return fileName;
    }
}
