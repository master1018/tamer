public class CommandListener implements ActionListener {
    private final Command command;
    private final String commandID;
    public CommandListener(Command command) {
        this.command = command;
        this.commandID = null;
    }
    public CommandListener(String commandID) {
        this.commandID = commandID;
        this.command = null;
    }
    @Override
    public void actionPerformed(ActionEvent arg0) {
        VScoreDoc doc = App.getInstance().getScoreDocument();
        if (command != null) doc.getCommandPerformer().execute(command); else doc.getCommandPerformer().execute(commandID);
    }
}
