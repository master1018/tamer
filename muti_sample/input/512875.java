final class IdentifiedIntegerValue extends SpecificIntegerValue
{
    private final ValueFactory valuefactory;
    private final int          id;
    public IdentifiedIntegerValue(ValueFactory valuefactory, int id)
    {
        this.valuefactory = valuefactory;
        this.id           = id;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.valuefactory.equals(((IdentifiedIntegerValue)object).valuefactory) &&
               this.id == ((IdentifiedIntegerValue)object).id;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               valuefactory.hashCode() ^
               id;
    }
    public String toString()
    {
        return "i"+id;
    }
}