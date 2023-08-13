public class SimpleFormatterFormat {
    private static final String key = "java.util.logging.SimpleFormatter.format";
    private static final String origFormat = System.getProperty(key);
    private static final PrintStream err = System.err;
    public static void main(String[] args) throws Exception {
        try {
            File dir = new File(System.getProperty("user.dir", "."));
            File log = new File(dir, "simpleformat.txt");
            java.nio.file.Files.deleteIfExists(log.toPath());
            PrintStream logps = new PrintStream(log);
            System.setProperty(key, "%3$s:%4$s: %5$s [%1$tc] source: %2$s%6$s%n");
            writeLogRecords(logps);
            checkLogRecords(log);
        } finally {
            if (origFormat == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, origFormat);
            }
            System.setErr(err);
       }
    }
    private static String[] loggers = new String[] {
        "test.foo",
        "test.foo",
        "test.bar",
        "test.bar"
    };
    private static String[] messages = new String[] {
        "severe hello world",
        "warning lost connection",
        "info welcome",
        "warning exception thrown",
    };
    private static void writeLogRecords(PrintStream logps) throws Exception {
        try {
            System.setErr(logps);
            Logger foo = Logger.getLogger("test.foo");
            foo.log(Level.SEVERE, "{0} {1} {2}", new Object[] {"severe", "hello", "world"});
            foo.warning(messages[1]);
            Logger bar = Logger.getLogger("test.bar");
            bar.finest("Dummy message");
            bar.info(messages[2]);
            bar.log(Level.WARNING, messages[3], new IllegalArgumentException());
        } finally {
            logps.flush();
            logps.close();
            System.setErr(err);
        }
    }
    private static void checkLogRecords(File log) throws Exception {
        System.out.println("Checking log records in file: " + log);
        Pattern p = Pattern.compile("([\\.a-zA-Z:]+) (.*) \\[.*\\] source: (.*)");
        try (FileInputStream in = new FileInputStream(log)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            int i = 0;
            while (i < messages.length &&
                      (line = reader.readLine()) != null) {
                String expectedLogger = loggers[i];
                String expectedMsg = messages[i];
                i++;
                line = line.trim();
                System.out.println(line);
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new RuntimeException("Unexpected output format");
                }
                if (m.groupCount() != 3) {
                    throw new RuntimeException("Unexpected group count = " +
                        m.groupCount());
                }
                String[] ss = m.group(1).split(":");
                int len = ss.length;
                if (len != 2) {
                    throw new RuntimeException("Unexpected logger name and level" +
                        m.group(1));
                }
                verify(expectedLogger, expectedMsg, ss[0], ss[1], m.group(2), m.group(3));
            }
            line = reader.readLine().trim();
            if (!line.equals("java.lang.IllegalArgumentException")) {
                throw new RuntimeException("Invalid line: " + line);
            }
        }
    }
    private static void verify(String expectedLogger, String expectedMsg,
                               String logger, String level,
                               String msg, String source) {
        if (!logger.equals(expectedLogger)) {
            throw new RuntimeException("Unexpected logger: " + logger);
        }
        if (!msg.equals(expectedMsg)) {
            throw new RuntimeException("Unexpected message: " + msg);
        }
        String[] ss = expectedMsg.split("\\s+");
        String expectedLevel = ss[0].toUpperCase();
        if (!level.equals(expectedLevel)) {
            throw new RuntimeException("Unexpected level: " + level);
        }
        ss = source.split("\\s+");
        int len = ss.length;
        if (!(len == 2 &&
              ss[0].equals("SimpleFormatterFormat") &&
              ss[1].equals("writeLogRecords"))) {
            throw new RuntimeException("Unexpected source: " + source);
        }
    }
}
