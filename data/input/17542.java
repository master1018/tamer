public class HSDBActionManager extends ActionManager {
    public static ActionManager getInstance() {
        if (manager == null) {
            manager = new HSDBActionManager();
        }
        return manager;
    }
    protected void addActions() {
        addAction(FindAction.VALUE_COMMAND, new FindAction());
        addAction(ShowAction.VALUE_COMMAND, new ShowAction());
        addAction(InspectAction.VALUE_COMMAND, new InspectAction());
        addAction(MemoryAction.VALUE_COMMAND, new MemoryAction());
        addAction(ThreadInfoAction.VALUE_COMMAND, new ThreadInfoAction());
        addAction(FindCrashesAction.VALUE_COMMAND, new FindCrashesAction());
        addAction(JavaStackTraceAction.VALUE_COMMAND, new JavaStackTraceAction());
        addAction(FindClassesAction.VALUE_COMMAND, new FindClassesAction());
    }
}
