public class BackAction extends DelegateAction
{
    public BackAction()
    {
        this(VALUE_SMALL_ICON);
    }
    public BackAction(String iconPath)
    {
        super("< Back", ActionManager.getIcon(iconPath));
        putValue("ActionCommandKey", "back-command");
        putValue("ShortDescription", "Select previous item");
        putValue("LongDescription", "Select previous item");
        putValue("MnemonicKey", VALUE_MNEMONIC);
        putValue("AcceleratorKey", VALUE_ACCELERATOR);
    }
    public static final String VALUE_COMMAND = "back-command";
    public static final String VALUE_NAME = "< Back";
    public static final String VALUE_SMALL_ICON = null;
    public static final String VALUE_LARGE_ICON = null;
    public static final Integer VALUE_MNEMONIC = new Integer(66);
    public static final KeyStroke VALUE_ACCELERATOR = null;
    public static final String VALUE_SHORT_DESCRIPTION = "Select previous item";
    public static final String VALUE_LONG_DESCRIPTION = "Select previous item";
}
