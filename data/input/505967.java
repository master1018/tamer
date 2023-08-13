public class PrintStreamPrinter implements Printer {
    private final PrintStream mPS;
    public PrintStreamPrinter(PrintStream pw) {
        mPS = pw;
    }
    public void println(String x) {
        mPS.println(x);
    }
}
