public class SimpleNameFactory implements NameFactory
{
    private static final int CHARACTER_COUNT = 26;
    private static final List cachedMixedCaseNames = new ArrayList();
    private static final List cachedLowerCaseNames = new ArrayList();
    private final boolean generateMixedCaseNames;
    private int     index = 0;
    public SimpleNameFactory()
    {
        this(true);
    }
    public SimpleNameFactory(boolean generateMixedCaseNames)
    {
        this.generateMixedCaseNames = generateMixedCaseNames;
    }
    public void reset()
    {
        index = 0;
    }
    public String nextName()
    {
        return name(index++);
    }
    private String name(int index)
    {
        List cachedNames = generateMixedCaseNames ?
            cachedMixedCaseNames :
            cachedLowerCaseNames;
        if (index < cachedNames.size())
        {
            return (String)cachedNames.get(index);
        }
        String name = newName(index);
        cachedNames.add(index, name);
        return name;
    }
    private String newName(int index)
    {
        int totalCharacterCount = generateMixedCaseNames ?
            2 * CHARACTER_COUNT :
            CHARACTER_COUNT;
        int baseIndex = index / totalCharacterCount;
        int offset    = index % totalCharacterCount;
        char newChar = charAt(offset);
        String newName = baseIndex == 0 ?
            new String(new char[] { newChar }) :
            (name(baseIndex-1) + newChar);
        return newName;
    }
    private char charAt(int index)
    {
        return (char)((index < CHARACTER_COUNT ? 'a' - 0               :
                                                 'A' - CHARACTER_COUNT) + index);
    }
    public static void main(String[] args)
    {
        System.out.println("Some mixed-case names:");
        printNameSamples(new SimpleNameFactory(true), 60);
        System.out.println("Some lower-case names:");
        printNameSamples(new SimpleNameFactory(false), 60);
        System.out.println("Some more mixed-case names:");
        printNameSamples(new SimpleNameFactory(true), 80);
        System.out.println("Some more lower-case names:");
        printNameSamples(new SimpleNameFactory(false), 80);
    }
    private static void printNameSamples(SimpleNameFactory factory, int count)
    {
        for (int counter = 0; counter < count; counter++)
        {
            System.out.println("  ["+factory.nextName()+"]");
        }
    }
}
