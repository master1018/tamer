public class LoggingMXBeanTest
{
    static String LOGGER_NAME_1 = "com.sun.management.Logger";
    static String LOGGER_NAME_2 = "com.sun.management.Logger.Logger2";
    static String UNKNOWN_LOGGER_NAME = "com.sun.management.Unknown";
    public static void main(String[] argv) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        LoggingMXBean proxy =
            ManagementFactory.newPlatformMXBeanProxy(mbs,
                LogManager.LOGGING_MXBEAN_NAME,
                LoggingMXBean.class);
        LoggingMXBeanTest p = new LoggingMXBeanTest(proxy);
        PlatformLoggingMXBean mxbean =
            ManagementFactory.getPlatformMXBean(mbs, PlatformLoggingMXBean.class);
        checkAttributes(proxy, mxbean);
    }
    public LoggingMXBeanTest(LoggingMXBean mbean) throws Exception {
        Logger logger1 = Logger.getLogger( LOGGER_NAME_1 );
        logger1.setLevel(Level.FINE);
        Logger logger2 = Logger.getLogger( LOGGER_NAME_2 );
        logger2.setLevel(null);
        System.out.println("Test Logger Name retrieval (getLoggerNames)");
        boolean log1 = false, log2 = false;
        List<String> loggers = mbean.getLoggerNames();
        if (loggers == null || loggers.size() < 2) {
            throw new RuntimeException(
                "Could not Detect the presense of the new Loggers");
        }
        for (String logger : loggers) {
            if (logger.equals(LOGGER_NAME_1)) {
                log1 = true;
                System.out.println("  : Found new Logger : " + logger);
            }
            if (logger.equals(LOGGER_NAME_2)) {
                log2 = true;
                System.out.println("  : Found new Logger : " + logger);
            }
        }
        if ( log1 && log2 )
            System.out.println("  : PASSED." );
        else {
            System.out.println("  : FAILED.  Could not Detect the new Loggers." );
            throw new RuntimeException(
                "Could not Detect the presense of the new Loggers");
        }
        System.out.println("Test getLoggerLevel");
        String l1 = mbean.getLoggerLevel(LOGGER_NAME_1);
        System.out.println("  : Level for Logger " + LOGGER_NAME_1 + " : " + l1);
        if (!l1.equals(Level.FINE.getName())) {
            throw new RuntimeException(
                "Expected level for " + LOGGER_NAME_1 + " = " +
                 Level.FINE.getName() + " but got " + l1);
        }
        String l2 = mbean.getLoggerLevel(LOGGER_NAME_2);
        System.out.println("  : Level for Logger " + LOGGER_NAME_2 + " : " + l2);
        if (!l2.equals("")) {
            throw new RuntimeException(
                "Expected level for " + LOGGER_NAME_2 + " = \"\"" +
                 " but got " + l2);
        }
        String l3 = mbean.getLoggerLevel(UNKNOWN_LOGGER_NAME);
        System.out.println("  : Level for unknown logger : " + l3);
        if (l3 != null) {
            throw new RuntimeException(
                "Expected level for " + UNKNOWN_LOGGER_NAME + " = null" +
                 " but got " + l3);
        }
        System.out.println("Test setLoggerLevel");
        mbean.setLoggerLevel(LOGGER_NAME_1, "INFO");
        System.out.println("  : Set Level for Logger " + LOGGER_NAME_1 + " to: INFO");
        Level l = logger1.getLevel();
        if (l != Level.INFO) {
            throw new RuntimeException(
                "Expected level for " + LOGGER_NAME_1 + " = " +
                 Level.INFO + " but got " + l);
        }
        mbean.setLoggerLevel(LOGGER_NAME_2, "SEVERE");
        System.out.println("  : Set Level for Logger " + LOGGER_NAME_2 + " to: SERVER");
        l = logger2.getLevel();
        if (l != Level.SEVERE) {
            throw new RuntimeException(
                "Expected level for " + LOGGER_NAME_2 + " = " +
                 Level.SEVERE+ " but got " + l);
        }
        mbean.setLoggerLevel(LOGGER_NAME_1, null);
        System.out.println("  : Set Level for Logger " + LOGGER_NAME_1 + " to: null");
        l = logger1.getLevel();
        if (l != null) {
            throw new RuntimeException(
                "Expected level for " + LOGGER_NAME_1 + " = null " +
                 " but got " + l);
        }
        boolean iaeCaught = false;
        System.out.println("  : Set Level for unknown Logger to: FINE");
        try {
            mbean.setLoggerLevel(UNKNOWN_LOGGER_NAME, "FINE");
        } catch (IllegalArgumentException e) {
            iaeCaught = true;
            System.out.println("      : IllegalArgumentException caught as expected");
        }
        if (!iaeCaught) {
            throw new RuntimeException(
                "Expected IllegalArgumentException for setting level for " +
                UNKNOWN_LOGGER_NAME + " not thrown");
        }
        iaeCaught = false;
        System.out.println("  : Set Level for Logger " + LOGGER_NAME_1 + " to: DUMMY");
        try {
            mbean.setLoggerLevel(LOGGER_NAME_1, "DUMMY");
        } catch (IllegalArgumentException e) {
            iaeCaught = true;
            System.out.println("      : IllegalArgumentException caught as expected");
        }
        if (!iaeCaught) {
            throw new RuntimeException(
                "Expected IllegalArgumentException for invalid level.");
        }
        System.out.println("Test getParentLoggerName");
        String p1 = mbean.getParentLoggerName(LOGGER_NAME_2);
        System.out.println("  : Parent Logger for " + LOGGER_NAME_2 + " : " + p1);
        if (!p1.equals(LOGGER_NAME_1)) {
            throw new RuntimeException(
                "Expected parent for " + LOGGER_NAME_2 + " = " +
                LOGGER_NAME_1 + " but got " + p1);
        }
        String p2 = mbean.getParentLoggerName("");
        System.out.println("  : Parent Logger for \"\" : " + p2);
        if (!p2.equals("")) {
            throw new RuntimeException(
                "Expected parent for root logger \"\" = \"\"" +
                " but got " + p2);
        }
        String p3 = mbean.getParentLoggerName(UNKNOWN_LOGGER_NAME);
        System.out.println("  : Parent Logger for unknown logger : " + p3);
        if (p3 != null) {
            throw new RuntimeException(
                "Expected level for " + UNKNOWN_LOGGER_NAME + " = null" +
                 " but got " + p3);
        }
    }
    private static void checkAttributes(LoggingMXBean mxbean1,
                                        PlatformLoggingMXBean mxbean2) {
        List<String> loggers1 = mxbean1.getLoggerNames();
        List<String> loggers2 = mxbean2.getLoggerNames();
        if (loggers1.size() != loggers2.size())
            throw new RuntimeException("LoggerNames: unmatched number of entries");
        List<String> loggers3 = new ArrayList<>(loggers1);
        loggers3.removeAll(loggers2);
        if (loggers3.size() != 0)
            throw new RuntimeException("LoggerNames: unmatched loggers");
        for (String logger : loggers1) {
            if (!mxbean1.getLoggerLevel(logger)
                    .equals(mxbean2.getLoggerLevel(logger)))
                throw new RuntimeException(
                    "LoggerLevel: unmatched level for " + logger);
            if (!mxbean1.getParentLoggerName(logger)
                    .equals(mxbean2.getParentLoggerName(logger)))
                throw new RuntimeException(
                    "ParentLoggerName: unmatched parent logger's name for " + logger);
        }
    }
}
