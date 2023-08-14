public class TestFontClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Font.class);
        test.testValue(new Font("Helvetica", Font.BOLD | Font.ITALIC, 20), "Helvetica BOLDITALIC 20");
        test.testValue(null, null);
        test.testText("Helvetica 12", new Font("Helvetica", Font.PLAIN, 12));
        test.testText(null, null);
    }
}
