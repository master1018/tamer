public class JavaStackTraceAction extends DelegateAction {
    public static final String VALUE_COMMAND = "jstack-command";
    public static final String VALUE_NAME = "Show Java stack trace";
    public static final String VALUE_SMALL_ICON = "general/History16.gif";
    public static final String VALUE_LARGE_ICON = "general/History24.gif";
    public static final Integer VALUE_MNEMONIC = new Integer('J');
    public static final String VALUE_SHORT_DESCRIPTION = "Show Java stack trace for selected thread";
    public static final String VALUE_LONG_DESCRIPTION = VALUE_SHORT_DESCRIPTION;
    public JavaStackTraceAction() {
        super(VALUE_NAME, ActionManager.getIcon(VALUE_SMALL_ICON));
        putValue(Action.ACTION_COMMAND_KEY, VALUE_COMMAND);
        putValue(Action.SHORT_DESCRIPTION, VALUE_SHORT_DESCRIPTION);
        putValue(Action.LONG_DESCRIPTION, VALUE_LONG_DESCRIPTION);
        putValue(Action.MNEMONIC_KEY, VALUE_MNEMONIC);
    }
}
