public class HelpMenu extends AbstractAction
{
    public HelpMenu()
    {
        super("Help");
        putValue("ActionCommandKey", "help-menu-command");
        putValue("ShortDescription", "Help operations");
        putValue("LongDescription", "Help operations");
        putValue("MnemonicKey", VALUE_MNEMONIC);
    }
    public void actionPerformed(ActionEvent actionevent)
    {
    }
    public static final String VALUE_COMMAND = "help-menu-command";
    public static final String VALUE_NAME = "Help";
    public static final Integer VALUE_MNEMONIC = new Integer(72);
    public static final String VALUE_SHORT_DESCRIPTION = "Help operations";
    public static final String VALUE_LONG_DESCRIPTION = "Help operations";
}
