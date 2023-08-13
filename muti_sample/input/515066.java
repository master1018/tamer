public class FixedStringMatcher implements StringMatcher
{
    private final String        fixedString;
    private final StringMatcher nextMatcher;
    public FixedStringMatcher(String fixedString)
    {
        this(fixedString, null);
    }
    public FixedStringMatcher(String fixedString, StringMatcher nextMatcher)
    {
        this.fixedString = fixedString;
        this.nextMatcher = nextMatcher;
    }
    public boolean matches(String string)
    {
        return string.startsWith(fixedString) &&
               (nextMatcher == null ||
                nextMatcher.matches(string.substring(fixedString.length())));
    }
}
