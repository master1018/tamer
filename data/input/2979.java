class AppletIOException extends IOException {
    private String key = null;
    private Object msgobj = null;
    public AppletIOException(String key) {
        super(key);
        this.key = key;
    }
    public AppletIOException(String key, Object arg) {
        this(key);
        msgobj = arg;
    }
    public String getLocalizedMessage() {
        if( msgobj != null)
            return amh.getMessage(key, msgobj);
        else
            return amh.getMessage(key);
    }
    private static AppletMessageHandler amh = new AppletMessageHandler("appletioexception");
}
