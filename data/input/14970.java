public final class UnknownUserException extends UserException {
    public Any except;
    public UnknownUserException() {
        super();
    }
    public UnknownUserException(Any a) {
        super();
        except = a;
    }
}
