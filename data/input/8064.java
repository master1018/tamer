class OQLHelp extends QueryHandler {
    public OQLHelp() {
    }
    public void run() {
        InputStream is = getClass().getResourceAsStream("/com/sun/tools/hat/resources/oqlhelp.html");
        int ch = -1;
        try {
            is = new BufferedInputStream(is);
            while ( (ch = is.read()) != -1) {
                out.print((char)ch);
            }
        } catch (Exception exp) {
            out.println(exp.getMessage());
            out.println("<pre>");
            exp.printStackTrace(out);
            out.println("</pre>");
        }
    }
}
