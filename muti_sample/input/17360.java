public class FindClassesAction extends DelegateAction {
    public static final String VALUE_COMMAND = "find-classes-command";
    public static final String VALUE_NAME = "Find classes";
    public static final String VALUE_SMALL_ICON = "general/Find16.gif";
    public static final Integer VALUE_MNEMONIC = new Integer('C');
    public static final String VALUE_SHORT_DESCRIPTION = "Find classes with given name part";
    public static final String VALUE_LONG_DESCRIPTION = VALUE_SHORT_DESCRIPTION;
    public FindClassesAction() {
        super(VALUE_NAME, ActionManager.getIcon(VALUE_SMALL_ICON));
        putValue(Action.ACTION_COMMAND_KEY, VALUE_COMMAND);
        putValue(Action.SHORT_DESCRIPTION, VALUE_SHORT_DESCRIPTION);
        putValue(Action.LONG_DESCRIPTION, VALUE_LONG_DESCRIPTION);
        putValue(Action.MNEMONIC_KEY, VALUE_MNEMONIC);
    }
}
