public class ParameterTagNotFoundException extends TLVNotFoundException {
    ParameterTagNotFoundException() {
        super();
    }
    ParameterTagNotFoundException(String message) {
        super(message);
    }
    ParameterTagNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    ParameterTagNotFoundException(Throwable cause) {
        super(cause);
    }
}
