public final class RequestingUserName   extends TextSyntax
    implements PrintRequestAttribute {
    private static final long serialVersionUID = -2683049894310331454L;
    public RequestingUserName(String userName, Locale locale) {
        super (userName, locale);
    }
    public boolean equals(Object object) {
        return (super.equals(object) &&
                object instanceof RequestingUserName);
    }
    public final Class<? extends Attribute> getCategory() {
        return RequestingUserName.class;
    }
    public final String getName() {
        return "requesting-user-name";
    }
}
