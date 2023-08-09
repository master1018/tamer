public class FileMenu extends AbstractAction
{
    public FileMenu()
    {
        super("File");
        putValue("ActionCommandKey", "file-menu-command");
        putValue("ShortDescription", "File operations");
        putValue("LongDescription", "File operations");
        putValue("MnemonicKey", VALUE_MNEMONIC);
    }
    public void actionPerformed(ActionEvent actionevent)
    {
    }
    public static final String VALUE_COMMAND = "file-menu-command";
    public static final String VALUE_NAME = "File";
    public static final Integer VALUE_MNEMONIC = new Integer(70);
    public static final String VALUE_SHORT_DESCRIPTION = "File operations";
    public static final String VALUE_LONG_DESCRIPTION = "File operations";
}
