import proguard.classfile.ClassConstants;
public class ClassNameParser implements StringParser
{
    private static final char[] INTERNAL_PRIMITIVE_TYPES = new char[]
    {
        ClassConstants.INTERNAL_TYPE_VOID,
        ClassConstants.INTERNAL_TYPE_BOOLEAN,
        ClassConstants.INTERNAL_TYPE_BYTE,
        ClassConstants.INTERNAL_TYPE_CHAR,
        ClassConstants.INTERNAL_TYPE_SHORT,
        ClassConstants.INTERNAL_TYPE_INT,
        ClassConstants.INTERNAL_TYPE_LONG,
        ClassConstants.INTERNAL_TYPE_FLOAT,
        ClassConstants.INTERNAL_TYPE_DOUBLE,
    };
    public StringMatcher parse(String regularExpression)
    {
        int           index;
        StringMatcher nextMatcher = new EmptyStringMatcher();
        for (index = 0; index < regularExpression.length(); index++)
        {
            if (regularExpression.regionMatches(index, "L
            {
                SettableMatcher settableMatcher = new SettableMatcher();
                nextMatcher =
                    new OrMatcher(parse(regularExpression.substring(index + 5)),
                                  createAnyTypeMatcher(settableMatcher));
                settableMatcher.setMatcher(nextMatcher);
                break;
            }
            if (regularExpression.regionMatches(index, "L***;", 0, 5))
            {
                nextMatcher =
                    createAnyTypeMatcher(parse(regularExpression.substring(index + 5)));
                break;
            }
            if (regularExpression.regionMatches(index, "**", 0, 2))
            {
                nextMatcher =
                    new VariableStringMatcher(null,
                                              new char[] { ClassConstants.INTERNAL_TYPE_CLASS_END },
                                              0,
                                              Integer.MAX_VALUE,
                                              parse(regularExpression.substring(index + 2)));
                break;
            }
            else if (regularExpression.charAt(index) == '*')
            {
                nextMatcher =
                    new VariableStringMatcher(null,
                                              new char[] { ClassConstants.INTERNAL_TYPE_CLASS_END, ClassConstants.INTERNAL_PACKAGE_SEPARATOR },
                                              0,
                                              Integer.MAX_VALUE,
                                              parse(regularExpression.substring(index + 1)));
                break;
            }
            else if (regularExpression.charAt(index) == '?')
            {
                nextMatcher =
                    new VariableStringMatcher(null,
                                              new char[] { ClassConstants.INTERNAL_TYPE_CLASS_END, ClassConstants.INTERNAL_PACKAGE_SEPARATOR },
                                              1,
                                              1,
                                              parse(regularExpression.substring(index + 1)));
                break;
            }
            else if (regularExpression.charAt(index) == '%')
            {
                nextMatcher =
                    new VariableStringMatcher(INTERNAL_PRIMITIVE_TYPES,
                                              null,
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
    private VariableStringMatcher createAnyTypeMatcher(StringMatcher nextMatcher)
    {
        return new VariableStringMatcher(new char[] { ClassConstants.INTERNAL_TYPE_ARRAY },
                                  null,
                                  0,
                                  255,
        new OrMatcher(
        new VariableStringMatcher(INTERNAL_PRIMITIVE_TYPES,
                                  null,
                                  1,
                                  1,
                                  nextMatcher),
        new VariableStringMatcher(new char[] { ClassConstants.INTERNAL_TYPE_CLASS_START },
                                  null,
                                  1,
                                  1,
        new VariableStringMatcher(null,
                                  new char[] { ClassConstants.INTERNAL_TYPE_CLASS_END },
                                  0,
                                  Integer.MAX_VALUE,
        new VariableStringMatcher(new char[] { ClassConstants.INTERNAL_TYPE_CLASS_END },
                                  null,
                                  1,
                                  1,
                                  nextMatcher)))));
    }
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            ClassNameParser parser  = new ClassNameParser();
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
