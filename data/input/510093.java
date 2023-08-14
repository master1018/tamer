public class SpecialNameFactory implements NameFactory
{
    private static final char SPECIAL_SUFFIX = '_';
    private final NameFactory nameFactory;
    public SpecialNameFactory(NameFactory nameFactory)
    {
        this.nameFactory = nameFactory;
    }
    public void reset()
    {
        nameFactory.reset();
    }
    public String nextName()
    {
        return nameFactory.nextName() + SPECIAL_SUFFIX;
    }
    static boolean isSpecialName(String name)
    {
        return name != null &&
               name.charAt(name.length()-1) == SPECIAL_SUFFIX;
    }
    public static void main(String[] args)
    {
        SpecialNameFactory factory = new SpecialNameFactory(new SimpleNameFactory());
        for (int counter = 0; counter < 50; counter++)
        {
            System.out.println("["+factory.nextName()+"]");
        }
    }
}
