class Logging implements LoggingMXBean {
    private static LogManager logManager = LogManager.getLogManager();
    Logging() {
    }
    public List<String> getLoggerNames() {
        Enumeration loggers = logManager.getLoggerNames();
        ArrayList<String> array = new ArrayList<>();
        for (; loggers.hasMoreElements();) {
            array.add((String) loggers.nextElement());
        }
        return array;
    }
    private static String EMPTY_STRING = "";
    public String getLoggerLevel(String loggerName) {
        Logger l = logManager.getLogger(loggerName);
        if (l == null) {
            return null;
        }
        Level level = l.getLevel();
        if (level == null) {
            return EMPTY_STRING;
        } else {
            return level.getName();
        }
    }
    public void setLoggerLevel(String loggerName, String levelName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }
        Logger logger = logManager.getLogger(loggerName);
        if (logger == null) {
            throw new IllegalArgumentException("Logger " + loggerName +
                "does not exist");
        }
        Level level = null;
        if (levelName != null) {
            level = Level.parse(levelName);
        }
        logger.setLevel(level);
    }
    public String getParentLoggerName( String loggerName ) {
        Logger l = logManager.getLogger( loggerName );
        if (l == null) {
            return null;
        }
        Logger p = l.getParent();
        if (p == null) {
            return EMPTY_STRING;
        } else {
            return p.getName();
        }
    }
}
