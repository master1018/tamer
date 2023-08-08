public class MapperRegistrationException extends DNotesException {
    private static final long serialVersionUID = 3257572801899870265L;
    public MapperRegistrationException(final String message) {
        super(message);
    }
    public MapperRegistrationException(final Throwable throwable) {
        super(throwable);
    }
    public MapperRegistrationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
