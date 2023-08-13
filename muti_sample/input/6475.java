public class ExitAction extends DelegateAction
{
    public ExitAction()
    {
        super("Exit", ActionManager.getIcon(VALUE_SMALL_ICON));
        putValue("ActionCommandKey", "exit-command");
        putValue("ShortDescription", "Exits the application");
        putValue("LongDescription", "Exits the application");
        putValue("MnemonicKey", VALUE_MNEMONIC);
        putValue("AcceleratorKey", VALUE_ACCELERATOR);
    }
    public static final String VALUE_COMMAND = "exit-command";
    public static final String VALUE_NAME = "Exit";
    public static final String VALUE_SMALL_ICON = null;
    public static final String VALUE_LARGE_ICON = null;
    public static final Integer VALUE_MNEMONIC = new Integer(88);
    public static final KeyStroke VALUE_ACCELERATOR = null;
    public static final String VALUE_SHORT_DESCRIPTION = "Exits the application";
    public static final String VALUE_LONG_DESCRIPTION = "Exits the application";
}
