class AppletIllegalArgumentException extends IllegalArgumentException {
    private String key = null;
    public AppletIllegalArgumentException(String key) {
        super(key);
        this.key = key;
    }
    public String getLocalizedMessage() {
        return amh.getMessage(key);
    }
    private static AppletMessageHandler amh = new AppletMessageHandler("appletillegalargumentexception");
}
