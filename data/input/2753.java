public class XmlReporter implements Reporter {
    OutputStream out;
    String title;
    public XmlReporter(OutputStream out, String title) {
        this.out = out;
        this.title = title;
    }
    public void writeReport(BenchInfo[] binfo, Properties props)
        throws IOException
    {
        PrintStream p = new PrintStream(out);
        p.println("<REPORT>");
        p.println("<NAME>" + title + "</NAME>");
        p.println("<DATE>" + new Date() + "</DATE>");
        p.println("<VERSION>" + props.getProperty("java.version") +
                "</VERSION>");
        p.println("<VENDOR>" + props.getProperty("java.vendor") + "</VENDOR>");
        p.println("<DIRECTORY>" + props.getProperty("java.home") +
                "</DIRECTORY>");
        String vmName = props.getProperty("java.vm.name");
        String vmInfo = props.getProperty("java.vm.info");
        String vmString = (vmName != null && vmInfo != null) ?
            vmName + " " + vmInfo : "Undefined";
        p.println("<VM_INFO>" + vmString + "</VM_INFO>");
        p.println("<OS>" + props.getProperty("os.name") +
                " version " + props.getProperty("os.version") + "</OS>");
        p.println("<BIT_DEPTH>" +
                Toolkit.getDefaultToolkit().getColorModel().getPixelSize() +
                "</BIT_DEPTH>");
        p.println();
        p.println("<DATA RUNS=\"" + 1 + "\" TESTS=\"" + binfo.length + "\">");
        for (int i = 0; i < binfo.length; i++) {
            BenchInfo b = binfo[i];
            String score = (b.getTime() != -1) ?
                Double.toString(b.getTime() * b.getWeight()) : "-1";
            p.println(b.getName() + "\t" + score);
        }
        p.println("</DATA>");
        p.println("</REPORT>");
    }
}
