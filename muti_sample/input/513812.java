public class FileNameParser implements StringParser
{
    public StringMatcher parse(String regularExpression)
    {
        int           index;
        StringMatcher nextMatcher = new EmptyStringMatcher();
        for (index = 0; index < regularExpression.length(); index++)
        {
            if (regularExpression.regionMatches(index, "**", 0, 2))
            {
                nextMatcher =
                    new VariableStringMatcher(null,
                                              null,
                                              0,
                                              Integer.MAX_VALUE,
                                              parse(regularExpression.substring(index + 2)));
                break;
            }
            else if (regularExpression.charAt(index) == '*')
            {
                nextMatcher =
                    new VariableStringMatcher(null,
                                              new char[] { File.pathSeparatorChar, '/' },
                                              0,
                                              Integer.MAX_VALUE,
                                              parse(regularExpression.substring(index + 1)));
                break;
            }
            else if (regularExpression.charAt(index) == '?')
            {
                nextMatcher =
                    new VariableStringMatcher(null,
                                              new char[] { File.pathSeparatorChar, '/' },
                                              1,
                                              1,
                                              parse(regularExpression.substring(index + 1)));
                break;
            }
        }
        return index != 0 ?
            (StringMatcher)new FixedStringMatcher(regularExpression.substring(0, index), nextMatcher) :
            (StringMatcher)nextMatcher;
    }
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            FileNameParser parser  = new FileNameParser();
            StringMatcher  matcher = parser.parse(args[0]);
            for (int index = 1; index < args.length; index++)
            {
                String string = args[index];
                System.out.print("String             ["+string+"]");
                System.out.println(" -> match = "+matcher.matches(args[index]));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
