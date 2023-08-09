public class ExtensionMatcher implements StringMatcher
{
    private final String extension;
    public ExtensionMatcher(String extension)
    {
        this.extension = extension;
    }
    public boolean matches(String string)
    {
        return endsWithIgnoreCase(string, extension);
    }
    private static boolean endsWithIgnoreCase(String string, String suffix)
    {
        int stringLength = string.length();
        int suffixLength = suffix.length();
        return string.regionMatches(true, stringLength - suffixLength, suffix, 0, suffixLength);
    }
}
