public class FileLogger {
    private static FileLogger LOGGER = null;
    private static FileWriter sLogWriter = null;
    public static String LOG_FILE_NAME =
        Environment.getExternalStorageDirectory() + "/emaillog.txt";
    public synchronized static FileLogger getLogger (Context c) {
        LOGGER = new FileLogger();
        return LOGGER;
    }
    private FileLogger() {
        try {
            sLogWriter = new FileWriter(LOG_FILE_NAME, true);
        } catch (IOException e) {
        }
    }
    static public synchronized void close() {
        if (sLogWriter != null) {
            try {
                sLogWriter.close();
            } catch (IOException e) {
            }
            sLogWriter = null;
        }
    }
    static public synchronized void log(Exception e) {
        if (sLogWriter != null) {
            log("Exception", "Stack trace follows...");
            PrintWriter pw = new PrintWriter(sLogWriter);
            e.printStackTrace(pw);
            pw.flush();
        }
    }
    @SuppressWarnings("deprecation")
    static public synchronized void log(String prefix, String str) {
        if (LOGGER == null) {
            LOGGER = new FileLogger();
            log("Logger", "\r\n\r\n --- New Log ---");
        }
        Date d = new Date();
        int hr = d.getHours();
        int min = d.getMinutes();
        int sec = d.getSeconds();
        StringBuffer sb = new StringBuffer(256);
        sb.append('[');
        sb.append(hr);
        sb.append(':');
        if (min < 10)
            sb.append('0');
        sb.append(min);
        sb.append(':');
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        sb.append("] ");
        if (prefix != null) {
            sb.append(prefix);
            sb.append("| ");
        }
        sb.append(str);
        sb.append("\r\n");
        String s = sb.toString();
        if (sLogWriter != null) {
            try {
                sLogWriter.write(s);
                sLogWriter.flush();
            } catch (IOException e) {
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    LOGGER = new FileLogger();
                    if (sLogWriter != null) {
                        try {
                            log("FileLogger", "Exception writing log; recreating...");
                            log(prefix, str);
                        } catch (Exception e1) {
                        }
                    }
                }
            }
        }
    }
}
