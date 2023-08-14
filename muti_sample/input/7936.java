public class NewAction extends DelegateAction
{
    public NewAction()
    {
        this("general/New16.gif");
    }
    public NewAction(String iconPath)
    {
        super("New", ActionManager.getIcon(iconPath));
        putValue("ActionCommandKey", "new-command");
        putValue("ShortDescription", "Create a new object.");
        putValue("LongDescription", "Create a new object.");
        putValue("MnemonicKey", VALUE_MNEMONIC);
        putValue("AcceleratorKey", VALUE_ACCELERATOR);
    }
    public static final String VALUE_COMMAND = "new-command";
    public static final String VALUE_NAME = "New";
    public static final String VALUE_SMALL_ICON = "general/New16.gif";
    public static final String VALUE_LARGE_ICON = "general/New24.gif";
    public static final Integer VALUE_MNEMONIC = new Integer(78);
    public static final KeyStroke VALUE_ACCELERATOR = KeyStroke.getKeyStroke(78, 2);
    public static final String VALUE_SHORT_DESCRIPTION = "Create a new object.";
    public static final String VALUE_LONG_DESCRIPTION = "Create a new object.";
}
