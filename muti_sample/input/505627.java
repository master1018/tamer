public class LiteralTagInfo extends TagInfo
{
    private static String encode(String t)
    {
        t = t.replace("&", "&amp;");
        t = t.replace("<", "&lt;");
        t = t.replace(">", "&gt;");
        return t;
    }
    public LiteralTagInfo(String n, String k, String t, SourcePositionInfo sp)
    {
        super("Text", "Text", encode(t), sp);
    }
}
