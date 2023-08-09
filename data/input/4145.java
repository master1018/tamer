public final class TestObject extends AbstractTest {
    public static final String XML 
            = "<java>\n"
            + " <object class=\"javax.swing.JPanel\">\n"
            + "  <void method=\"add\">\n"
            + "   <object id=\"button\" class=\"javax.swing.JButton\">\n"
            + "    <string>button</string>\n"
            + "    <void property=\"verticalAlignment\">\n"
            + "     <object field=\"CENTER\" class=\"javax.swing.SwingConstants\"/>\n"
            + "    </void>\n"
            + "   </object>\n"
            + "  </void>\n"
            + "  <void method=\"add\">\n"
            + "   <object class=\"javax.swing.JLabel\">\n"
            + "    <string>label</string>\n"
            + "    <void property=\"labelFor\">\n"
            + "     <object idref=\"button\"/>\n"
            + "    </void>\n"
            + "   </object>\n"
            + "  </void>\n"
            + " </object>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestObject().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        JPanel panel = (JPanel) decoder.readObject();
        if (2 != panel.getComponents().length) {
            throw new Error("unexpected component count");
        }
        JButton button = (JButton) panel.getComponents()[0];
        if (!button.getText().equals("button")) { 
            throw new Error("unexpected button text");
        }
        if (SwingConstants.CENTER != button.getVerticalAlignment()) {
            throw new Error("unexpected vertical alignment");
        }
        JLabel label = (JLabel) panel.getComponents()[1];
        if (!label.getText().equals("label")) { 
            throw new Error("unexpected label text");
        }
        if (button != label.getLabelFor()) {
            throw new Error("unexpected component");
        }
    }
}
