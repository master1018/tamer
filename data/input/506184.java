public class ConstantMatcher implements StringMatcher
{
    private boolean matches;
    public ConstantMatcher(boolean matches)
    {
        this.matches = matches;
    }
    public boolean matches(String string)
    {
        return matches;
    }
}