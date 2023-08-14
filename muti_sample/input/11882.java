public class SyntaxException extends Exception {
    int lineno;
    public SyntaxException(int lineno) {
        this.lineno = lineno;
    }
    public String getMessage() {
        return "syntax error at line " + lineno;
    }
}
