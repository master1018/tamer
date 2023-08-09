public class PlatformLoggerTest {
    private static final int defaultEffectiveLevel = 0;
    public static void main(String[] args) throws Exception {
        final String FOO_PLATFORM_LOGGER = "test.platformlogger.foo";
        final String BAR_PLATFORM_LOGGER = "test.platformlogger.bar";
        final String GOO_PLATFORM_LOGGER = "test.platformlogger.goo";
        final String BAR_LOGGER = "test.logger.bar";
        PlatformLogger goo = PlatformLogger.getLogger(GOO_PLATFORM_LOGGER);
        testLogMethods(goo);
        PlatformLogger foo = PlatformLogger.getLogger(FOO_PLATFORM_LOGGER);
        checkPlatformLogger(foo, FOO_PLATFORM_LOGGER);
        Logger logger = Logger.getLogger(BAR_LOGGER);
        logger.setLevel(Level.WARNING);
        PlatformLogger bar = PlatformLogger.getLogger(BAR_PLATFORM_LOGGER);
        checkPlatformLogger(bar, BAR_PLATFORM_LOGGER);
        testLogMethods(goo);
        testLogMethods(bar);
        checkLogger(FOO_PLATFORM_LOGGER, Level.FINER);
        checkLogger(BAR_PLATFORM_LOGGER, Level.FINER);
        checkLogger(GOO_PLATFORM_LOGGER, null);
        checkLogger(BAR_LOGGER, Level.WARNING);
        foo.setLevel(PlatformLogger.SEVERE);
        checkLogger(FOO_PLATFORM_LOGGER, Level.SEVERE);
    }
    private static void checkPlatformLogger(PlatformLogger logger, String name) {
        if (!logger.getName().equals(name)) {
            throw new RuntimeException("Invalid logger's name " +
                logger.getName() + " but expected " + name);
        }
        if (logger.getLevel() != defaultEffectiveLevel) {
            throw new RuntimeException("Invalid default level for logger " +
                logger.getName());
        }
        if (logger.isLoggable(PlatformLogger.FINE) != false) {
            throw new RuntimeException("isLoggerable(FINE) returns true for logger " +
                logger.getName() + " but expected false");
        }
        logger.setLevel(PlatformLogger.FINER);
        if (logger.getLevel() != Level.FINER.intValue()) {
            throw new RuntimeException("Invalid level for logger " +
                logger.getName() + " " + logger.getLevel());
        }
        if (logger.isLoggable(PlatformLogger.FINE) != true) {
            throw new RuntimeException("isLoggerable(FINE) returns false for logger " +
                logger.getName() + " but expected true");
        }
        logger.info("OK: Testing log message");
    }
    private static void checkLogger(String name, Level level) {
        Logger logger = LogManager.getLogManager().getLogger(name);
        if (logger == null) {
            throw new RuntimeException("Logger " + name +
                " does not exist");
        }
        if (logger.getLevel() != level) {
            throw new RuntimeException("Invalid level for logger " +
                logger.getName() + " " + logger.getLevel());
        }
    }
    private static void testLogMethods(PlatformLogger logger) {
        logger.severe("Test severe(String, Object...) {0} {1}", new Long(1), "string");
        logger.severe("Test severe(String, Object...) {0}", (Object[]) getPoints());
        logger.warning("Test warning(String, Throwable)", new Throwable("Testing"));
        logger.info("Test info(String)");
    }
    static Point[] getPoints() {
        Point[] res = new Point[3];
        res[0] = new Point(0,0);
        res[1] = new Point(1,1);
        res[2] = new Point(2,2);
        return res;
    }
    static class Point {
        final int x;
        final int y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public String toString() {
            return "{x="+x + ", y=" + y + "}";
        }
    }
}
