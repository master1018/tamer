public class BasicHeader implements Header, Cloneable {
    private final String name;
    private final String value;
    public BasicHeader(final String name, final String value) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return this.name;
    }
    public String getValue() {
        return this.value;
    }
    public String toString() {
        return BasicLineFormatter.DEFAULT.formatHeader(null, this).toString();
    }
    public HeaderElement[] getElements() throws ParseException {
        if (this.value != null) {
            return BasicHeaderValueParser.parseElements(this.value, null);
        } else {
            return new HeaderElement[] {}; 
        }
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
