public class EmptyStringMatcher implements StringMatcher
{
    public boolean matches(String string)
    {
        return string.length() == 0;
    }
}
