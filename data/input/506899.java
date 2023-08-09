final class CUIOutputStream {
    private static PrintStream sOutput = System.out;
    public static final String CTS_PROMPT_SIGN = "cts_host > ";
    static public void print(final String msg) {
        sOutput.print(msg);
        Log.log(msg);
    }
    static public void println(final String msg) {
        sOutput.println(msg);
        Log.log(msg);
    }
    static public void write(byte[] buf, int off, int len) {
        sOutput.write(buf, off, len);
    }
    static public void write(int c) {
        sOutput.write(c);
    }
    static public void flush() {
        sOutput.flush();
    }
    static public void printPrompt() {
        print(CTS_PROMPT_SIGN);
        Log.log(CTS_PROMPT_SIGN);
    }
}
