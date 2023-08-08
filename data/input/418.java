public class TriggerType {
    private static final String DEFAULT_EXPRESSION = "*/1 * * * * ?";
    private static final String DEFAULT_EXPRESSION_TIMEZONE = TimeZone.getDefault().getDisplayName();
    @XmlElement(required = true)
    private String expression;
    @XmlElement
    private String timezone;
    public TriggerType() {
        this.expression = TriggerType.DEFAULT_EXPRESSION;
        this.timezone = TriggerType.DEFAULT_EXPRESSION_TIMEZONE;
    }
    public String getExpression() {
        return this.expression;
    }
    public void setExpression(final String expression) {
        this.expression = expression;
    }
    public String getTimezone() {
        return this.timezone;
    }
    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }
}
