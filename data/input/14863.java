public class HttpLogFormatter extends java.util.logging.SimpleFormatter {
    private static volatile Pattern pattern = null;
    private static volatile Pattern cpattern = null;
    public HttpLogFormatter() {
        if (pattern == null) {
            pattern = Pattern.compile("\\{[^\\}]*\\}");
            cpattern = Pattern.compile("[^,\\] ]{2,}");
        }
    }
    @Override
    public String format(LogRecord record) {
        String sourceClassName = record.getSourceClassName();
        if (sourceClassName == null ||
            !(sourceClassName.startsWith("sun.net.www.protocol.http") ||
              sourceClassName.startsWith("sun.net.www.http"))) {
            return super.format(record);
        }
        String src = record.getMessage();
        StringBuilder buf = new StringBuilder("HTTP: ");
        if (src.startsWith("sun.net.www.MessageHeader@")) {
            Matcher match = pattern.matcher(src);
            while (match.find()) {
                int i = match.start();
                int j = match.end();
                String s = src.substring(i + 1, j - 1);
                if (s.startsWith("null: ")) {
                    s = s.substring(6);
                }
                if (s.endsWith(": null")) {
                    s = s.substring(0, s.length() - 6);
                }
                buf.append("\t").append(s).append("\n");
            }
        } else if (src.startsWith("Cookies retrieved: {")) {
            String s = src.substring(20);
            buf.append("Cookies from handler:\n");
            while (s.length() >= 7) {
                if (s.startsWith("Cookie=[")) {
                    String s2 = s.substring(8);
                    int c = s2.indexOf("Cookie2=[");
                    if (c > 0) {
                        s2 = s2.substring(0, c-1);
                        s = s2.substring(c);
                    } else {
                        s = "";
                    }
                    if (s2.length() < 4) {
                        continue;
                    }
                    Matcher m = cpattern.matcher(s2);
                    while (m.find()) {
                        int i = m.start();
                        int j = m.end();
                        if (i >= 0) {
                            String cookie = s2.substring(i + 1, j > 0 ? j - 1 : s2.length() - 1);
                            buf.append("\t").append(cookie).append("\n");
                        }
                    }
                }
                if (s.startsWith("Cookie2=[")) {
                    String s2 = s.substring(9);
                    int c = s2.indexOf("Cookie=[");
                    if (c > 0) {
                        s2 = s2.substring(0, c-1);
                        s = s2.substring(c);
                    } else {
                        s = "";
                    }
                    Matcher m = cpattern.matcher(s2);
                    while (m.find()) {
                        int i = m.start();
                        int j = m.end();
                        if (i >= 0) {
                            String cookie = s2.substring(i+1, j > 0 ? j-1 : s2.length() - 1);
                            buf.append("\t").append(cookie).append("\n");
                        }
                    }
                }
            }
        } else {
            buf.append(src).append("\n");
        }
        return buf.toString();
    }
}
