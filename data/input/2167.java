public class MemoryAction extends DelegateAction {
    public static final String VALUE_COMMAND = "memory-command";
    public static final String VALUE_NAME = "Stack Memory...";
    public static final String VALUE_SMALL_ICON = "development/Server16.gif";
    public static final String VALUE_LARGE_ICON = "development/Server24.gif";
    public static final Integer VALUE_MNEMONIC = new Integer('M');
    public static final String VALUE_SHORT_DESCRIPTION = "Show Stack Memory";
    public static final String VALUE_LONG_DESCRIPTION = "Show the stack memory for the current thread";
    public MemoryAction() {
        super(VALUE_NAME, ActionManager.getIcon(VALUE_SMALL_ICON));
        putValue(Action.ACTION_COMMAND_KEY, VALUE_COMMAND);
        putValue(Action.SHORT_DESCRIPTION, VALUE_SHORT_DESCRIPTION);
        putValue(Action.LONG_DESCRIPTION, VALUE_LONG_DESCRIPTION);
        putValue(Action.MNEMONIC_KEY, VALUE_MNEMONIC);
    }
}
