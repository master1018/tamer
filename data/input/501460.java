public class InternalError extends VirtualMachineError {
    private static final long serialVersionUID = -9062593416125562365L;
    public InternalError() {
        super();
    }
    public InternalError(String detailMessage) {
        super(detailMessage);
    }
}
