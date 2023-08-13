public class ListMatcher implements StringMatcher
{
    private final StringMatcher[] matchers;
    private final boolean[]       negate;
    public ListMatcher(StringMatcher[] matchers)
    {
        this(matchers, null);
    }
    public ListMatcher(StringMatcher[] matchers, boolean[] negate)
    {
        this.matchers = matchers;
        this.negate   = negate;
    }
    public boolean matches(String string)
    {
        for (int index = 0; index < matchers.length; index++)
        {
            StringMatcher matcher = matchers[index];
            if (matcher.matches(string))
            {
                return negate == null ||
                       !negate[index];
            }
        }
        return negate != null &&
               negate[negate.length - 1];
    }
}
