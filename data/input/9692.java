public class ApplicationException extends Exception {
    public ApplicationException(String id,
                                InputStream ins) {
        this.id = id;
        this.ins = ins;
    }
    public String getId() {
        return id;
    }
    public InputStream getInputStream() {
        return ins;
    }
    private String id;
    private InputStream ins;
}
