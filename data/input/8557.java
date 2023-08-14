class UnixException extends Exception {
    static final long serialVersionUID = 7227016794320723218L;
    private int errno;
    private String msg;
    UnixException(int errno) {
        this.errno = errno;
        this.msg = null;
    }
    UnixException(String msg) {
        this.errno = 0;
        this.msg = msg;
    }
    int errno() {
        return errno;
    }
    void setError(int errno) {
        this.errno = errno;
        this.msg = null;
    }
    String errorString() {
        if (msg != null) {
            return msg;
        } else {
            return new String(UnixNativeDispatcher.strerror(errno()));
        }
    }
    @Override
    public String getMessage() {
        return errorString();
    }
    private IOException translateToIOException(String file, String other) {
        if (msg != null)
            return new IOException(msg);
        if (errno() == UnixConstants.EACCES)
            return new AccessDeniedException(file, other, null);
        if (errno() == UnixConstants.ENOENT)
            return new NoSuchFileException(file, other, null);
        if (errno() == UnixConstants.EEXIST)
            return new FileAlreadyExistsException(file, other, null);
        return new FileSystemException(file, other, errorString());
    }
    void rethrowAsIOException(String file) throws IOException {
        IOException x = translateToIOException(file, null);
        throw x;
    }
    void rethrowAsIOException(UnixPath file, UnixPath other) throws IOException {
        String a = (file == null) ? null : file.getPathForExecptionMessage();
        String b = (other == null) ? null : other.getPathForExecptionMessage();
        IOException x = translateToIOException(a, b);
        throw x;
    }
    void rethrowAsIOException(UnixPath file) throws IOException {
        rethrowAsIOException(file, null);
    }
    IOException asIOException(UnixPath file) {
        return translateToIOException(file.getPathForExecptionMessage(), null);
    }
}
