public class SimpleFormatter extends Formatter {
    public SimpleFormatter() {
        super();
    }
    @Override
    public String format(LogRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format("{0, date} {0, time} ", 
                new Object[] { new Date(r.getMillis()) }));
        sb.append(r.getSourceClassName()).append(" "); 
        sb.append(r.getSourceMethodName()).append(
                LogManager.getSystemLineSeparator());
        sb.append(r.getLevel().getName()).append(": "); 
        sb.append(formatMessage(r)).append(LogManager.getSystemLineSeparator());
        if (null != r.getThrown()) {
            sb.append("Throwable occurred: "); 
            Throwable t = r.getThrown();
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                sb.append(sw.toString());
            } finally {
                if (pw != null) {
                    try {
                        pw.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return sb.toString();
    }
}
