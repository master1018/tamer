public class StringBuilderPrinter implements Printer {
    private final StringBuilder mBuilder;
    public StringBuilderPrinter(StringBuilder builder) {
        mBuilder = builder;
    }
    public void println(String x) {
        mBuilder.append(x);
        int len = x.length();
        if (len <= 0 || x.charAt(len-1) != '\n') {
            mBuilder.append('\n');
        }
    }
}
