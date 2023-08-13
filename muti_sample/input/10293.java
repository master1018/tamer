public final class DateTimeAtProcessing extends DateTimeSyntax
        implements PrintJobAttribute {
    private static final long serialVersionUID = -3710068197278263244L;
    public DateTimeAtProcessing(Date dateTime) {
        super (dateTime);
    }
    public boolean equals(Object object) {
        return(super.equals (object) &&
               object instanceof DateTimeAtProcessing);
    }
    public final Class<? extends Attribute> getCategory() {
        return DateTimeAtProcessing.class;
    }
    public final String getName() {
        return "date-time-at-processing";
    }
}
