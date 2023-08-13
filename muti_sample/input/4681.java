class WindowsException extends Exception {
    static final long serialVersionUID = 2765039493083748820L;
    private int lastError;
    private String msg;
    WindowsException(int lastError) {
        this.lastError = lastError;
        this.msg = null;
    }
    WindowsException(String msg) {
        this.lastError = 0;
        this.msg = msg;
    }
    int lastError() {
        return lastError;
    }
    String errorString() {
        if (msg == null) {
            msg = WindowsNativeDispatcher.FormatMessage(lastError);
            if (msg == null) {
                msg = "Unknown error: 0x" + Integer.toHexString(lastError);
            }
        }
        return msg;
    }
    @Override
    public String getMessage() {
        return errorString();
    }
    private IOException translateToIOException(String file, String other) {
        if (lastError() == 0)
            return new IOException(errorString());
        if (lastError() == ERROR_FILE_NOT_FOUND || lastError() == ERROR_PATH_NOT_FOUND)
            return new NoSuchFileException(file, other, null);
        if (lastError() == ERROR_FILE_EXISTS || lastError() == ERROR_ALREADY_EXISTS)
            return new FileAlreadyExistsException(file, other, null);
        if (lastError() == ERROR_ACCESS_DENIED)
            return new AccessDeniedException(file, other, null);
        return new FileSystemException(file, other, errorString());
    }
    void rethrowAsIOException(String file) throws IOException {
        IOException x = translateToIOException(file, null);
        throw x;
    }
    void rethrowAsIOException(WindowsPath file, WindowsPath other) throws IOException {
        String a = (file == null) ? null : file.getPathForExceptionMessage();
        String b = (other == null) ? null : other.getPathForExceptionMessage();
        IOException x = translateToIOException(a, b);
        throw x;
    }
    void rethrowAsIOException(WindowsPath file) throws IOException {
        rethrowAsIOException(file, null);
    }
    IOException asIOException(WindowsPath file) {
        return translateToIOException(file.getPathForExceptionMessage(), null);
    }
}
