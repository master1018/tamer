public class VariableStringMatcher implements StringMatcher
{
    private final char[]        allowedCharacters;
    private final char[]        disallowedCharacters;
    private final int           minimumLength;
    private final int           maximumLength;
    private final StringMatcher nextMatcher;
    public VariableStringMatcher(char[]        allowedCharacters,
                                 char[]        disallowedCharacters,
                                 int           minimumLength,
                                 int           maximumLength,
                                 StringMatcher nextMatcher)
    {
        this.allowedCharacters    = allowedCharacters;
        this.disallowedCharacters = disallowedCharacters;
        this.minimumLength        = minimumLength;
        this.maximumLength        = maximumLength;
        this.nextMatcher          = nextMatcher;
    }
    public boolean matches(String string)
    {
        if (string.length() < minimumLength)
        {
            return false;
        }
        for (int index = 0; index < minimumLength; index++)
        {
            if (!isAllowedCharacter(string.charAt(index)))
            {
                return false;
            }
        }
        int maximumLength = Math.min(this.maximumLength, string.length());
        for (int index = minimumLength; index < maximumLength; index++)
        {
            if (nextMatcher.matches(string.substring(index)))
            {
                return true;
            }
            if (!isAllowedCharacter(string.charAt(index)))
            {
                return false;
            }
        }
        return nextMatcher.matches(string.substring(maximumLength));
    }
    private boolean isAllowedCharacter(char character)
    {
        if (allowedCharacters != null)
        {
            for (int index = 0; index < allowedCharacters.length; index++)
            {
                if (allowedCharacters[index] == character)
                {
                    return true;
                }
            }
            return false;
        }
        if (disallowedCharacters != null)
        {
            for (int index = 0; index < disallowedCharacters.length; index++)
            {
                if (disallowedCharacters[index] == character)
                {
                    return false;
                }
            }
        }
        return true;
    }
}
