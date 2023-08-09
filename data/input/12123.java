public class SourceClassName {
    public static void main(String[] args) throws Exception {
        File dir = new File(System.getProperty("user.dir", "."));
        File log = new File(dir, "testlog.txt");
        PrintStream logps = new PrintStream(log);
        writeLogRecords(logps);
        checkLogRecords(log);
    }
    private static void writeLogRecords(PrintStream logps) throws Exception {
        PrintStream err = System.err;
        try {
            System.setErr(logps);
            Object[] params = new Object[] { new Long(1), "string"};
            PlatformLogger plog = PlatformLogger.getLogger("test.log.foo");
            plog.severe("Log message {0} {1}", (Object[]) params);
            Logger logger = Logger.getLogger("test.log.bar");
            logger.log(Level.SEVERE, "Log message {0} {1}", params);
            plog.severe("Log message {0} {1}", (Object[]) params);
        } finally {
            logps.flush();
            logps.close();
            System.setErr(err);
        }
    }
    private static void checkLogRecords(File log) throws Exception {
        System.out.println("Checking log records in file: " + log);
        FileInputStream in = new FileInputStream(log);
        String EXPECTED_LOG = "SEVERE: Log message 1 string";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            String[] record = new String[2];
            int count = 0;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                System.out.println(line);
                record[i++] = line;
                if (i == 2) {
                    i = 0;
                    count++;
                    String[] ss = record[0].split("\\s+");
                    int len = ss.length;
                    if (!ss[len-2].equals("SourceClassName") ||
                        !ss[len-1].equals("writeLogRecords")) {
                        throw new RuntimeException("Unexpected source: " +
                            ss[len-2] + " " + ss[len-1]);
                    }
                    if (!record[1].equals(EXPECTED_LOG)) {
                        throw new RuntimeException("Unexpected log: " + record[1]);
                    }
                }
            }
            if (count != 3) {
                throw new RuntimeException("Unexpected number of records: " + count);
            }
        } finally {
            in.close();
        }
    }
}
