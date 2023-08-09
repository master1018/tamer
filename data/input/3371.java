public class RetroLexer extends antlr.CharScanner
{
    public RetroLexer() {
        literals.put("Foo", new Integer(5));
    }
}
