public class ListParser implements StringParser
{
    private final StringParser stringParser;
    public ListParser(StringParser stringParser)
    {
        this.stringParser = stringParser;
    }
    public StringMatcher parse(String regularExpression)
    {
        return parse(ListUtil.commaSeparatedList(regularExpression));
    }
    public StringMatcher parse(List regularExpressions)
    {
        StringMatcher listMatcher = null;
        for (int index = regularExpressions.size()-1; index >=0; index--)
        {
            String regularExpression = (String)regularExpressions.get(index);
            StringMatcher entryMatcher = parseEntry(regularExpression);
            listMatcher =
                listMatcher == null ?
                    (StringMatcher)entryMatcher :
                isNegated(regularExpression) ?
                    (StringMatcher)new AndMatcher(entryMatcher, listMatcher) :
                    (StringMatcher)new OrMatcher(entryMatcher, listMatcher);
        }
        return listMatcher != null ? listMatcher : new ConstantMatcher(true);
    }
    private StringMatcher parseEntry(String regularExpression)
    {
        return isNegated(regularExpression) ?
          new NotMatcher(stringParser.parse(regularExpression.substring(1))) :
          stringParser.parse(regularExpression);
    }
    private boolean isNegated(String regularExpression)
    {
        return regularExpression.length() > 0 &&
               regularExpression.charAt(0) == '!';
    }
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            ListParser parser  = new ListParser(new NameParser());
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
