public class HtmlReporter implements Reporter {
    static final int PRECISION = 3;
    static final String[] PROPNAMES = { "os.name", "os.arch", "os.version",
        "java.home", "java.vm.version", "java.vm.vendor", "java.vm.name",
        "java.compiler", "java.class.path", "sun.boot.class.path" };
    OutputStream out;
    String title;
    public HtmlReporter(OutputStream out, String title) {
        this.out = out;
        this.title = title;
    }
    public void writeReport(BenchInfo[] binfo, Properties props)
        throws IOException
    {
        PrintStream p = new PrintStream(out);
        float total = 0.0f;
        p.println("<html>");
        p.println("<head>");
        p.println("<title>" + title + "</title>");
        p.println("</head>");
        p.println("<body bgcolor=\"#ffffff\">");
        p.println("<h3>" + title + "</h3>");
        p.println("<hr>");
        p.println("<table border=0>");
        for (int i = 0; i < PROPNAMES.length; i++) {
            p.println("<tr><td>" + PROPNAMES[i] + ": <td>" +
                    props.getProperty(PROPNAMES[i]));
        }
        p.println("</table>");
        p.println("<p>");
        p.println("<table border=1>");
        p.println("<tr><th># <th>Benchmark Name <th>Time (ms) <th>Score");
        for (int i = 0; i < binfo.length; i++) {
            BenchInfo b = binfo[i];
            p.print("<tr><td>" + i + " <td>" + b.getName());
            if (b.getTime() != -1) {
                float score = b.getTime() * b.getWeight();
                total += score;
                p.println(" <td>" + b.getTime() + " <td>" +
                        Util.floatToString(score, PRECISION));
            }
            else {
                p.println(" <td>-- <td>--");
            }
        }
        p.println("<tr><td colspan=3><b>Total score</b> <td><b>" +
                Util.floatToString(total, PRECISION) + "</b>");
        p.println("</table>");
        p.println("<p>");
        p.println("<hr>");
        p.println("<i>Report generated on " + new Date() + "</i>");
        p.println("</body>");
        p.println("</html>");
    }
}
