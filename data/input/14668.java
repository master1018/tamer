public class MessageRetriever {
    private final Configuration configuration;
    private final String resourcelocation;
    private ResourceBundle messageRB;
    public MessageRetriever(ResourceBundle rb) {
        this.configuration = null;
        this.messageRB = rb;
        this.resourcelocation = null;
    }
    public MessageRetriever(Configuration configuration,
                            String resourcelocation) {
        this.configuration = configuration;
        this.resourcelocation = resourcelocation;
    }
    public String getText(String key, Object... args) throws MissingResourceException {
        if (messageRB == null) {
            try {
                messageRB = ResourceBundle.getBundle(resourcelocation);
            } catch (MissingResourceException e) {
                throw new Error("Fatal: Resource (" + resourcelocation +
                                    ") for javadoc doclets is missing.");
            }
        }
        String message = messageRB.getString(key);
        return MessageFormat.format(message, args);
    }
    private void printError(SourcePosition pos, String msg) {
        configuration.root.printError(pos, msg);
    }
    private void printError(String msg) {
        configuration.root.printError(msg);
    }
    private void printWarning(SourcePosition pos, String msg) {
        configuration.root.printWarning(pos, msg);
    }
    private void printWarning(String msg) {
        configuration.root.printWarning(msg);
    }
    private void printNotice(SourcePosition pos, String msg) {
        configuration.root.printNotice(pos, msg);
    }
    private void printNotice(String msg) {
        configuration.root.printNotice(msg);
    }
    public void error(SourcePosition pos, String key, Object... args) {
        printError(pos, getText(key, args));
    }
    public void error(String key, Object... args) {
        printError(getText(key, args));
    }
    public void warning(SourcePosition pos, String key, Object... args) {
        printWarning(pos, getText(key, args));
    }
    public void warning(String key, Object... args) {
        printWarning(getText(key, args));
    }
    public void notice(SourcePosition pos, String key, Object... args) {
        printNotice(pos, getText(key, args));
    }
    public void notice(String key, Object... args) {
        printNotice(getText(key, args));
    }
}
