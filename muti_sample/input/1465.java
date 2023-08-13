public class TestCompoundTest {
    public static void main(String args[]) throws Exception
    {
        if (System.getProperty("os.name").startsWith("Windows"))
            return;
        Charset cs = Charset.forName("COMPOUND_TEXT");
        if (!cs.name().startsWith("x-"))
            throw new RuntimeException("FAILED: name does not start with x-");
        Set<String> aliases = cs.aliases();
        if (!aliases.contains("COMPOUND_TEXT") ||
            !aliases.contains("x-compound-text") ||
            !aliases.contains("x11-compound_text"))
            throw new RuntimeException("FAILED: alias name is missing");
    }
}
