public class PrintWriterPrinter implements Printer {
    private final PrintWriter mPW;
    public PrintWriterPrinter(PrintWriter pw) {
        mPW = pw;
    }
    public void println(String x) {
        mPW.println(x);
    }
}
