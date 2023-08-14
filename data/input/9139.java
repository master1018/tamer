public final class TestVar extends AbstractTest {
    public static final String XML
            = "<java id=\"decoder\">\n"
            + " <var idref=\"decoder\"/>\n"
            + " <var id=\"another\" idref=\"decoder\"/>\n"
            + " <var idref=\"another\"/>\n"
            + " <var id=\"decoder\" idref=\"another\"/>\n"
            + " <var idref=\"decoder\"/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestVar().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        for (int i = 0; i < 3; i++) {
            if (decoder != decoder.readObject()) {
                throw new Error("decoder instance expected");
            }
        }
    }
}
