public class DeferredLintHandler {
    protected static final Context.Key<DeferredLintHandler> deferredLintHandlerKey =
        new Context.Key<DeferredLintHandler>();
    public static DeferredLintHandler instance(Context context) {
        DeferredLintHandler instance = context.get(deferredLintHandlerKey);
        if (instance == null)
            instance = new DeferredLintHandler(context);
        return instance;
    }
    protected DeferredLintHandler(Context context) {
        context.put(deferredLintHandlerKey, this);
    }
    private DeferredLintHandler() {}
    public interface LintLogger {
        void report();
    }
    private DiagnosticPosition currentPos;
    private Map<DiagnosticPosition, ListBuffer<LintLogger>> loggersQueue = new HashMap<DiagnosticPosition, ListBuffer<LintLogger>>();
    public void report(LintLogger logger) {
        ListBuffer<LintLogger> loggers = loggersQueue.get(currentPos);
        Assert.checkNonNull(loggers);
        loggers.append(logger);
    }
    public void flush(DiagnosticPosition pos) {
        ListBuffer<LintLogger> loggers = loggersQueue.get(pos);
        if (loggers != null) {
            for (LintLogger lintLogger : loggers) {
                lintLogger.report();
            }
            loggersQueue.remove(pos);
        }
    }
    public DeferredLintHandler setPos(DiagnosticPosition currentPos) {
        this.currentPos = currentPos;
        loggersQueue.put(currentPos, ListBuffer.<LintLogger>lb());
        return this;
    }
    public static final DeferredLintHandler immediateHandler = new DeferredLintHandler() {
        @Override
        public void report(LintLogger logger) {
            logger.report();
        }
    };
}
