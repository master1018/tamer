public class LiteralTaglet implements Taglet {
    private static final String NAME = "literal";
    public static void register(Map<String,Taglet> map) {
           map.remove(NAME);
           map.put(NAME, new LiteralTaglet());
    }
    public String getName() {
        return NAME;
    }
    public String toString(Tag tag) {
        return textToString(tag.text());
    }
    public String toString(Tag[] tags) { return null; }
    public boolean inField() { return false; }
    public boolean inConstructor() { return false; }
    public boolean inMethod() { return false; }
    public boolean inOverview() { return false; }
    public boolean inPackage() { return false; }
    public boolean inType() { return false; }
    public boolean isInlineTag() { return true; }
    protected static String textToString(String text) {
           StringBuffer buf = new StringBuffer();
           for (int i = 0; i < text.length(); i++) {
               char c = text.charAt(i);
               switch (c) {
                   case '<':
                          buf.append("&lt;");
                          break;
                   case '>':
                          buf.append("&gt;");
                          break;
                   case '&':
                          buf.append("&amp;");
                          break;
                   default:
                          buf.append(c);
               }
           }
           return buf.toString();
    }
}
