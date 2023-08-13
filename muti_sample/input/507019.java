public abstract class AtCommandHandler {
    public AtCommandResult handleBasicCommand(String arg) {
        return new AtCommandResult(AtCommandResult.ERROR);
    }
    public AtCommandResult handleActionCommand() {
        return new AtCommandResult(AtCommandResult.ERROR);
    }
    public AtCommandResult handleReadCommand() {
        return new AtCommandResult(AtCommandResult.ERROR);
    }
    public AtCommandResult handleSetCommand(Object[] args) {
        return new AtCommandResult(AtCommandResult.ERROR);
    }
    public AtCommandResult handleTestCommand() {
        return new AtCommandResult(AtCommandResult.OK);
    }
}
