public class ApplyAction extends DelegateAction
{
    public ApplyAction()
    {
        this(VALUE_SMALL_ICON);
    }
    public ApplyAction(String iconPath)
    {
        super("Apply", ActionManager.getIcon(iconPath));
        putValue("ActionCommandKey", "apply-command");
        putValue("ShortDescription", "Apply the activity");
        putValue("LongDescription", "Apply the activity");
        putValue("MnemonicKey", VALUE_MNEMONIC);
        putValue("AcceleratorKey", VALUE_ACCELERATOR);
    }
    public static final String VALUE_COMMAND = "apply-command";
    public static final String VALUE_NAME = "Apply";
    public static final String VALUE_SMALL_ICON = null;
    public static final String VALUE_LARGE_ICON = null;
    public static final Integer VALUE_MNEMONIC = new Integer(65);
    public static final KeyStroke VALUE_ACCELERATOR = null;
    public static final String VALUE_SHORT_DESCRIPTION = "Apply the activity";
    public static final String VALUE_LONG_DESCRIPTION = "Apply the activity";
}
