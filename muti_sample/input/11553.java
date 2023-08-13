public class PKCS11RuntimeException extends RuntimeException {
    public PKCS11RuntimeException() {
        super();
    }
    public PKCS11RuntimeException(String message) {
        super(message);
    }
    public PKCS11RuntimeException(Exception encapsulatedException) {
        super(encapsulatedException);
    }
    public PKCS11RuntimeException(String message, Exception encapsulatedException) {
        super(message, encapsulatedException);
    }
}
