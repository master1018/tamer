public class OkAction extends DelegateAction
{
    public OkAction()
    {
        this(VALUE_SMALL_ICON);
    }
    public OkAction(String iconPath)
    {
        super("OK", ActionManager.getIcon(iconPath));
        putValue("ActionCommandKey", "ok-command");
        putValue("ShortDescription", "Acknowleges the action");
        putValue("LongDescription", "Acknowleges the action");
        putValue("MnemonicKey", VALUE_MNEMONIC);
        putValue("AcceleratorKey", VALUE_ACCELERATOR);
    }
    public static final String VALUE_COMMAND = "ok-command";
    public static final String VALUE_NAME = "OK";
    public static final String VALUE_SMALL_ICON = null;
    public static final String VALUE_LARGE_ICON = null;
    public static final Integer VALUE_MNEMONIC = new Integer(79);
    public static final KeyStroke VALUE_ACCELERATOR = null;
    public static final String VALUE_SHORT_DESCRIPTION = "Acknowleges the action";
    public static final String VALUE_LONG_DESCRIPTION = "Acknowleges the action";
}
