public class ParameterTypeDateFormat extends ParameterTypeStringCategory {
    private static final long serialVersionUID = 1L;
    private transient InputPort inPort;
    private ParameterTypeAttribute attributeParameter;
    public static final String[] PREDEFINED_DATE_FORMATS = new String[] { "", "yyyy.MM.dd G 'at' HH:mm:ss z", "EEE, MMM d, ''yy", "h:mm a", "hh 'o''clock' a, zzzz", "K:mm a, z", "yyyy.MMMMM.dd GGG hh:mm aaa", "EEE, d MMM yyyy HH:mm:ss Z", "yyMMddHHmmssZ", "yyyy-MM-dd'T'HH:mm:ss.SSSZ" };
    public ParameterTypeDateFormat(String key, String description, boolean expert) {
        this(null, key, description, null, expert);
    }
    public ParameterTypeDateFormat(ParameterTypeAttribute attributeParameter, String key, String description, InputPort inPort, boolean expert) {
        super(key, description, PREDEFINED_DATE_FORMATS, "", true);
        setExpert(expert);
        this.inPort = inPort;
        this.attributeParameter = attributeParameter;
    }
    public InputPort getInputPort() {
        return inPort;
    }
    public ParameterTypeAttribute getAttributeParameterType() {
        return attributeParameter;
    }
}
