final class IdentifiedFloatValue extends SpecificFloatValue
{
    private final ValueFactory valuefactory;
    private final int          id;
    public IdentifiedFloatValue(ValueFactory valuefactory, int id)
    {
        this.valuefactory = valuefactory;
        this.id           = id;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.valuefactory.equals(((IdentifiedFloatValue)object).valuefactory) &&
               this.id == ((IdentifiedFloatValue)object).id;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               valuefactory.hashCode() ^
               id;
    }
    public String toString()
    {
        return "f"+id;
    }
}