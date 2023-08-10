public class LoggerUtils {
    static Map<Thread, EnvironmentImpl> envMap = new ConcurrentHashMap<Thread, EnvironmentImpl>();
    static Map<Thread, Formatter> formatterMap = new ConcurrentHashMap<Thread, Formatter>();
    public static final String NO_ENV = ".noEnv";
    public static final String FIXED_PREFIX = ".fixedPrefix";
    private static final String PUSH_LEVEL = ".push.level";
    public static Logger getLogger(Class<?> cl) {
        Logger logger = createLogger(cl.getName());
        boolean hasConsoleHandler = false;
        boolean hasMemoryHandler = false;
        boolean hasFileHandler = false;
        Handler[] handlers = logger.getHandlers();
        if (handlers != null) {
            for (Handler h : handlers) {
                if (h instanceof java.util.logging.ConsoleHandler) {
                    hasConsoleHandler = true;
                }
                if (h instanceof MemoryRedirectHandler) {
                    hasMemoryHandler = true;
                }
                if (h instanceof FileRedirectHandler) {
                    hasFileHandler = true;
                }
            }
        }
        if (!hasConsoleHandler) {
            logger.addHandler(new ConsoleRedirectHandler());
        }
        if (!hasMemoryHandler) {
            logger.addHandler(new MemoryRedirectHandler());
        }
        if (!hasFileHandler) {
            logger.addHandler(new FileRedirectHandler());
        }
        return logger;
    }
    public static Logger getLoggerFormatterNeeded(Class<?> cl) {
        Logger logger = createLogger(cl.getName() + NO_ENV);
        if (!hasConsoleHandler(logger)) {
            logger.addHandler(new FormatterRedirectHandler());
        }
        return logger;
    }
    public static Logger getLoggerFixedPrefix(Class<?> cl, String prefix) {
        return getLoggerFixedPrefix(cl, prefix, null);
    }
    public static Logger getLoggerFixedPrefix(Class<?> cl, String prefix, EnvironmentImpl envImpl) {
        Logger logger = createLogger(cl.getName() + FIXED_PREFIX);
        if (!hasConsoleHandler(logger)) {
            logger.addHandler(new com.sleepycat.je.util.ConsoleHandler(new TracerFormatter(prefix), envImpl));
        }
        return logger;
    }
    private static boolean hasConsoleHandler(Logger logger) {
        Handler[] handlers = logger.getHandlers();
        if (handlers == null) {
            return false;
        }
        for (Handler h : handlers) {
            if (h instanceof java.util.logging.ConsoleHandler) {
                return true;
            }
        }
        return false;
    }
    private static Logger createLogger(String className) {
        Logger logger = Logger.getLogger(className);
        logger.setUseParentHandlers(false);
        return logger;
    }
    public static String getLoggerProperty(String property) {
        java.util.logging.LogManager mgr = java.util.logging.LogManager.getLogManager();
        return mgr.getProperty(property);
    }
    public static Level getPushLevel(String name) {
        String propertyValue = getLoggerProperty(name + PUSH_LEVEL);
        Level level = Level.OFF;
        if (propertyValue != null) {
            level = Level.parse(propertyValue);
        }
        return level;
    }
    public static void logMsg(Logger useLogger, EnvironmentImpl envImpl, Level logLevel, String msg) {
        if (envImpl != null) {
            envMap.put(Thread.currentThread(), envImpl);
        }
        try {
            useLogger.log(logLevel, msg);
        } finally {
            envMap.remove(Thread.currentThread());
        }
    }
    public static void envLogMsg(Level logLevel, EnvironmentImpl envImpl, String msg) {
        logMsg(envImpl.getLogger(), envImpl, logLevel, msg);
    }
    public static void logMsg(Logger useLogger, EnvironmentImpl envImpl, Formatter formatter, Level logLevel, String msg) {
        if (envImpl != null) {
            logMsg(useLogger, envImpl, logLevel, msg);
        } else {
            logMsg(useLogger, formatter, logLevel, msg);
        }
    }
    public static void severe(Logger useLogger, EnvironmentImpl envImpl, String msg) {
        logMsg(useLogger, envImpl, Level.SEVERE, msg);
    }
    public static void warning(Logger useLogger, EnvironmentImpl envImpl, String msg) {
        logMsg(useLogger, envImpl, Level.WARNING, msg);
    }
    public static void info(Logger useLogger, EnvironmentImpl envImpl, String msg) {
        logMsg(useLogger, envImpl, Level.INFO, msg);
    }
    public static void fine(Logger useLogger, EnvironmentImpl envImpl, String msg) {
        logMsg(useLogger, envImpl, Level.FINE, msg);
    }
    public static void finer(Logger useLogger, EnvironmentImpl envImpl, String msg) {
        logMsg(useLogger, envImpl, Level.FINER, msg);
    }
    public static void finest(Logger useLogger, EnvironmentImpl envImpl, String msg) {
        logMsg(useLogger, envImpl, Level.FINEST, msg);
    }
    public static void logMsg(Logger useLogger, Formatter formatter, Level logLevel, String msg) {
        if (formatter != null) {
            formatterMap.put(Thread.currentThread(), formatter);
        }
        try {
            useLogger.log(logLevel, msg);
        } finally {
            formatterMap.remove(Thread.currentThread());
        }
    }
    public static void traceAndLogException(EnvironmentImpl envImpl, String sourceClass, String sourceMethod, String msg, Throwable t) {
        String traceMsg = msg + "\n" + getStackTrace(t);
        envMap.put(Thread.currentThread(), envImpl);
        try {
            envImpl.getLogger().logp(Level.SEVERE, sourceClass, sourceMethod, traceMsg);
        } finally {
            envMap.remove(Thread.currentThread());
        }
        Trace.trace(envImpl, traceMsg);
    }
    public static void traceAndLog(Logger logger, EnvironmentImpl envImpl, Level logLevel, String msg) {
        logMsg(logger, envImpl, logLevel, msg);
        Trace.trace(envImpl, msg);
    }
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        stackTrace = stackTrace.replaceAll("&lt", "<");
        stackTrace = stackTrace.replaceAll("&gt", ">");
        return stackTrace;
    }
    public static String getStackTrace() {
        Exception e = new Exception();
        return getStackTrace(e);
    }
    public static Level getHandlerLevel(DbConfigManager configManager, ConfigParam param, String levelName) {
        boolean changed = false;
        String level = configManager.get(param);
        if (!param.getDefault().equals(level)) {
            changed = true;
        }
        String propertyLevel = getLoggerProperty(levelName);
        if (!changed && propertyLevel != null) {
            level = propertyLevel;
        }
        return Level.parse(level);
    }
}
