public class XMLFormatter extends Formatter {
    private static final String lineSeperator = LogManager
            .getSystemLineSeparator();
    private static final String indent = "    "; 
    public XMLFormatter() {
        super();
    }
    @SuppressWarnings("nls")
    @Override
    public String format(LogRecord r) {
        long time = r.getMillis();
        String date = MessageFormat.format("{0, date} {0, time}",
                new Object[] { new Date(time) });
        StringBuilder sb = new StringBuilder();
        sb.append(("<record>")).append(lineSeperator);
        sb.append(indent).append(("<date>")).append(date).append(("</date>"))
                .append(lineSeperator);
        sb.append(indent).append(("<millis>")).append(time).append(
                ("</millis>")).append(lineSeperator);
        sb.append(indent).append(("<sequence>")).append(r.getSequenceNumber())
                .append(("</sequence>")).append(lineSeperator);
        if (null != r.getLoggerName()) {
            sb.append(indent).append(("<logger>")).append(r.getLoggerName())
                    .append(("</logger>")).append(lineSeperator);
        }
        sb.append(indent).append(("<level>")).append(r.getLevel().getName())
                .append(("</level>")).append(lineSeperator);
        if (null != r.getSourceClassName()) {
            sb.append(indent).append(("<class>"))
                    .append(r.getSourceClassName()).append(("</class>"))
                    .append(lineSeperator);
        }
        if (null != r.getSourceMethodName()) {
            sb.append(indent).append(("<method>")).append(
                    r.getSourceMethodName()).append(("</method>")).append(
                    lineSeperator);
        }
        sb.append(indent).append(("<thread>")).append(r.getThreadID()).append(
                ("</thread>")).append(lineSeperator);
        formatMessages(r, sb);
        Object[] params;
        if ((params = r.getParameters()) != null) {
            for (Object element : params) {
                sb.append(indent).append(("<param>")).append(element).append(
                        ("</param>")).append(lineSeperator);
            }
        }
        formatThrowable(r, sb);
        sb.append(("</record>")).append(lineSeperator);
        return sb.toString();
    }
    @SuppressWarnings("nls")
    private void formatMessages(LogRecord r, StringBuilder sb) {
        ResourceBundle rb = r.getResourceBundle();
        String pattern = r.getMessage();
        if (null != rb && null != pattern) {
            String message;
            try {
                message = rb.getString(pattern);
            } catch (Exception e) {
                message = null;
            }
            if (message == null) {
                message = pattern;
                sb.append(indent).append(("<message>")).append(message).append(
                        ("</message>")).append(lineSeperator);
            } else {
                sb.append(indent).append(("<message>")).append(message).append(
                        ("</message>")).append(lineSeperator);
                sb.append(indent).append(("<key>")).append(pattern).append(
                        ("</key>")).append(lineSeperator);
                sb.append(indent).append(("<catalog>")).append(
                        r.getResourceBundleName()).append(("</catalog>"))
                        .append(lineSeperator);
            }
        } else if (null != pattern) {
            sb.append(indent).append(("<message>")).append(pattern).append(
                    ("</message>")).append(lineSeperator);
        } else {
            sb.append(indent).append(("<message/>"));
        }
    }
    @SuppressWarnings("nls")
    private void formatThrowable(LogRecord r, StringBuilder sb) {
        Throwable t;
        if ((t = r.getThrown()) != null) {
            sb.append(indent).append("<exception>").append(lineSeperator);
            sb.append(indent).append(indent).append("<message>").append(
                    t.toString()).append("</message>").append(lineSeperator);
            StackTraceElement[] elements = t.getStackTrace();
            for (StackTraceElement e : elements) {
                sb.append(indent).append(indent).append("<frame>").append(
                        lineSeperator);
                sb.append(indent).append(indent).append(indent).append(
                        "<class>").append(e.getClassName()).append("</class>")
                        .append(lineSeperator);
                sb.append(indent).append(indent).append(indent).append(
                        "<method>").append(e.getMethodName()).append(
                        "</method>").append(lineSeperator);
                sb.append(indent).append(indent).append(indent)
                        .append("<line>").append(e.getLineNumber()).append(
                                "</line>").append(lineSeperator);
                sb.append(indent).append(indent).append("</frame>").append(
                        lineSeperator);
            }
            sb.append(indent).append("</exception>").append(lineSeperator);
        }
    }
    @SuppressWarnings("nls")
    @Override
    public String getHead(Handler h) {
        String encoding = null;
        if (null != h) {
            encoding = h.getEncoding();
        }
        if (null == encoding) {
            encoding = getSystemProperty("file.encoding");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"").append(encoding).append(
                "\" standalone=\"no\"?>").append(lineSeperator);
        sb.append("<!DOCTYPE log SYSTEM \"logger.dtd\">").append(lineSeperator);
        sb.append(("<log>"));
        return sb.toString();
    }
    @Override
    public String getTail(Handler h) {
        return "</log>"; 
    }
    private static String getSystemProperty(final String key) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key);
            }
        });
    }
}
