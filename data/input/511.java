public final class JobOriginatingUserName extends TextSyntax
        implements PrintJobAttribute {
    private static final long serialVersionUID = -8052537926362933477L;
    public JobOriginatingUserName(String userName, Locale locale) {
        super (userName, locale);
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof JobOriginatingUserName);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobOriginatingUserName.class;
    }
    public final String getName() {
        return "job-originating-user-name";
    }
}
