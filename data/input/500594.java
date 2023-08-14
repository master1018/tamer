public class NumericNameFactory implements NameFactory
{
    private int index;
    public void reset()
    {
        index = 0;
    }
    public String nextName()
    {
        return Integer.toString(++index);
    }
}