public final class TestNew extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <new class=\"TestNew\"/>\n"
            + " <new class=\"TestNew\">\n"
            + "  <null/>\n"
            + " </new>\n"
            + " <new class=\"TestNew\">\n"
            + "  <string>single</string>\n"
            + " </new>\n"
            + " <new class=\"TestNew\">\n"
            + "  <string>first</string>\n"
            + "  <string>second</string>\n"
            + "  <string>third</string>\n"
            + " </new>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestNew().test(true);
    }
    private List<String> list;
    public TestNew(String...messages) {
        if (messages != null) {
            this.list = new ArrayList<String>();
            for (String message : messages) {
                this.list.add(message);
            }
        }
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof TestNew) {
            TestNew test = (TestNew) object;
            return (test.list == null)
                    ? this.list == null
                    : test.list.equals(this.list);
        }
        return false;
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        validate(decoder.readObject());
        validate(decoder.readObject(), null);
        validate(decoder.readObject(), "single");
        validate(decoder.readObject(), "first", "second", "third");
    }
    private static void validate(Object object, String...messages) {
        if (!object.equals(new TestNew(messages))) {
            throw new Error("expected object");
        }
    }
}
