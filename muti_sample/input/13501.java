public final class TestProperty extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <property name=\"owner\">\n"
            + "  <property name=\"message\">\n"
            + "   <string>message</string>\n"
            + "  </property>\n"
            + "  <property id=\"message\" name=\"message\"/>\n"
            + "  <property name=\"indexed\" index=\"1\">\n"
            + "   <string>indexed</string>\n"
            + "  </property>\n"
            + "  <property id=\"indexed\" name=\"indexed\" index=\"1\"/>\n"
            + " </property>\n"
            + " <var idref=\"message\"/>\n"
            + " <var idref=\"indexed\"/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestProperty().test(true);
    }
    private int index;
    private String message;
    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getIndexed(int index) {
        if (this.index != index) {
            throw new Error("unexpected index");
        }
        return this.message;
    }
    public void setIndexed(int index, String message) {
        this.index = index;
        this.message = message;
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        decoder.setOwner(this);
        validate(decoder, "message");
        validate(decoder, "indexed");
    }
    private static void validate(XMLDecoder decoder, String name) {
        if (!decoder.readObject().equals(name)) {
            throw new Error(name + " expected");
        }
    }
}
