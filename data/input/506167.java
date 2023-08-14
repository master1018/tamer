public class AndMatcher implements StringMatcher
{
    private final StringMatcher matcher1;
    private final StringMatcher matcher2;
    public AndMatcher(StringMatcher matcher1, StringMatcher matcher2)
    {
        this.matcher1 = matcher1;
        this.matcher2 = matcher2;
    }
    public boolean matches(String string)
    {
        return matcher1.matches(string) &&
               matcher2.matches(string);
    }
}
