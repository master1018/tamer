public class InvalidDnDOperationException extends IllegalStateException {
    static private String dft_msg = "The operation requested cannot be performed by the DnD system since it is not in the appropriate state";
    public InvalidDnDOperationException() { super(dft_msg); }
    public InvalidDnDOperationException(String msg) { super(msg); }
}
