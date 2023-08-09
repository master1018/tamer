class ErrorMessage {
    long where;
    String message;
    ErrorMessage next;
    ErrorMessage(long where, String message) {
        this.where = where;
        this.message = message;
    }
}
