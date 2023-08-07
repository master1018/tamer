public abstract class Status extends AbstractEntry implements ServiceControlled {
    private static final long serialVersionUID = -5193075846115040838L;
    protected Status() {
    }
    protected Status(StatusType severity) {
        this.severity = severity;
    }
    public StatusType severity;
}
