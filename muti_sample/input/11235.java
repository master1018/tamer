public class FindCrashesAction extends DelegateAction {
    public static final String VALUE_COMMAND = "find-crashes-command";
    public static final String VALUE_NAME = "Find Crashes...";
    public static final String VALUE_SMALL_ICON = "general/Delete16.gif";
    public static final String VALUE_LARGE_ICON = "general/Delete24.gif";
    public static final Integer VALUE_MNEMONIC = new Integer('H');
    public static final String VALUE_SHORT_DESCRIPTION = "Find Crashes";
    public static final String VALUE_LONG_DESCRIPTION = "Searches the threads for potential crashes";
    public FindCrashesAction() {
        super(VALUE_NAME, ActionManager.getIcon(VALUE_SMALL_ICON));
        putValue(Action.ACTION_COMMAND_KEY, VALUE_COMMAND);
        putValue(Action.SHORT_DESCRIPTION, VALUE_SHORT_DESCRIPTION);
        putValue(Action.LONG_DESCRIPTION, VALUE_LONG_DESCRIPTION);
        putValue(Action.MNEMONIC_KEY, VALUE_MNEMONIC);
    }
}
