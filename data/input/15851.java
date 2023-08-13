public final class TestField extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <field name=\"FIELD\" class=\"TestField\"/>\n"
            + " <field name=\"FIELD\" class=\"TestField\">\n"
            + "  <string>static postfix</string>\n"
            + " </field>\n"
            + " <field name=\"FIELD\" class=\"TestField\"/>\n"
            + " <property name=\"owner\">\n"
            + "  <field id=\"prefix\" name=\"field\"/>\n"
            + "  <field name=\"field\">\n"
            + "   <string>postfix</string>\n"
            + "  </field>\n"
            + "  <field id=\"postfix\" name=\"field\"/>\n"
            + " </property>\n"
            + " <var idref=\"prefix\"/>\n"
            + " <var idref=\"postfix\"/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestField().test(true);
    }
    public static String FIELD;
    public String field;
    @Override
    protected void validate(XMLDecoder decoder) {
        FIELD = "static prefix";
        field = "prefix";
        decoder.setOwner(this);
        validate(decoder, "static prefix");
        validate(decoder, "static postfix");
        validate(decoder, "prefix");
        validate(decoder, "postfix");
    }
    private static void validate(XMLDecoder decoder, String name) {
        if (!decoder.readObject().equals(name)) {
            throw new Error(name + " expected");
        }
    }
}
