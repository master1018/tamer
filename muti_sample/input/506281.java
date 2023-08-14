public class TopValue extends Category1Value
{
    public boolean isSpecific()
    {
        return true;
    }
    public boolean isParticular()
    {
        return true;
    }
    public final Value generalize(Value other)
    {
        return this.getClass() == other.getClass() ? this : null;
    }
    public final int computationalType()
    {
        return TYPE_TOP;
    }
    public final String internalType()
    {
        return null;
    }
    public boolean equals(Object object)
    {
        return object != null &&
               this.getClass() == object.getClass();
    }
    public int hashCode()
    {
        return this.getClass().hashCode();
    }
    public String toString()
    {
        return "T";
    }
}
